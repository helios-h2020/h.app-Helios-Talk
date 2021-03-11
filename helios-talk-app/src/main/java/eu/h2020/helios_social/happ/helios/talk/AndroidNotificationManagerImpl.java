package eu.h2020.helios_social.happ.helios.talk;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.PrivateGroupConversationActivity;
import eu.h2020.helios_social.modules.groupcommunications_utils.Multiset;
import eu.h2020.helios_social.modules.groupcommunications_utils.contact.event.ContactAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.contact.event.PendingContactAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.context.ContextInvitationAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupInvitationAddedEvent;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.PendingContactListActivity;
import eu.h2020.helios_social.happ.helios.talk.context.invites.InvitationListActivity;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactType;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.Service;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.ServiceException;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.Settings;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.event.SettingsUpdatedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.system.Clock;
import eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils;

import eu.h2020.helios_social.happ.helios.talk.android.system.AndroidExecutor;
import eu.h2020.helios_social.happ.helios.talk.login.SignInReminderReceiver;
import eu.h2020.helios_social.happ.helios.talk.splash.SplashScreenActivity;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkNotificationBuilder;
import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.utils.Pair;
import eu.h2020.helios_social.modules.groupcommunications.messaging.event.GroupMessageReceivedEvent;
import eu.h2020.helios_social.modules.groupcommunications.messaging.event.PrivateMessageReceivedEvent;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import static android.app.Notification.DEFAULT_LIGHTS;
import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;
import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.app.NotificationManager.IMPORTANCE_LOW;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION.SDK_INT;
import static androidx.core.app.NotificationCompat.CATEGORY_MESSAGE;
import static androidx.core.app.NotificationCompat.CATEGORY_SERVICE;
import static androidx.core.app.NotificationCompat.CATEGORY_SOCIAL;
import static androidx.core.app.NotificationCompat.PRIORITY_LOW;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static androidx.core.app.NotificationCompat.VISIBILITY_SECRET;
import static androidx.core.content.ContextCompat.getColor;
import static eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsConsts.SETTINGS_NAMESPACE;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_ID;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

@ThreadSafe
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class AndroidNotificationManagerImpl implements AndroidNotificationManager,
        Service, EventListener {

    public static Uri CONTACT_URI =
            Uri.parse("helios-content://eu.h2020.helios.talk/contact");
    public static Uri GROUP_URI =
            Uri.parse("helios-content://eu.h2020.helios.talk/group");
    public static Uri CONNECTIONS_URI =
            Uri.parse("helios-content://eu.h2020.helios.talk/connections");
    public static Uri CONTACT_ADDED_URI =
            Uri.parse("helios-content://eu.h2020.helios.talk/contact/added");
    public static Uri INVITATION_URI =
            Uri.parse("helios-content://eu.h2020.helios.talk/invitation");

    private static final long SOUND_DELAY = TimeUnit.SECONDS.toMillis(2);

    private final SettingsManager settingsManager;
    private final AndroidExecutor androidExecutor;
    private final Clock clock;
    private final Context appContext;
    private final NotificationManager notificationManager;
    private final AtomicBoolean used = new AtomicBoolean(false);

    // The following must only be accessed on the main UI thread
    private final Multiset<Pair<String, String>> contactCounts = new Multiset<>();
    private final Multiset<String> groupCounts = new Multiset<>();
    private final Multiset<String> pendingContactsCounts = new Multiset<>();
    private final Multiset<String> forumCounts = new Multiset<>();
    private final Multiset<String> invitationsCounts = new Multiset<>();
    private int contactAddedTotal = 0;
    private int nextRequestId = 0;
    @Nullable
    private Pair<String, String> blockedContact = null;
    @Nullable
    private String blockedGroup = null;
    private boolean blockSignInReminder = false;
    private long lastSound = 0;

    private volatile Settings settings = new Settings();

    @Inject
    AndroidNotificationManagerImpl(SettingsManager settingsManager,
                                   AndroidExecutor androidExecutor, Application app, Clock clock) {
        this.settingsManager = settingsManager;
        this.androidExecutor = androidExecutor;
        this.clock = clock;
        appContext = app.getApplicationContext();
        notificationManager = (NotificationManager)
                appContext.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void startService() throws ServiceException {
        if (used.getAndSet(true)) throw new IllegalStateException();
        // Load settings
        try {
            settings = settingsManager.getSettings(SETTINGS_NAMESPACE);
        } catch (DbException e) {
            throw new ServiceException(e);
        }
        if (SDK_INT >= 26) {
            // Create notification channels
            Callable<Void> task = () -> {
                createNotificationChannel(CONTACT_CHANNEL_ID,
                        R.string.contact_list_button);
                createNotificationChannel(GROUP_CHANNEL_ID,
                        R.string.groups_button);
                createNotificationChannel(FORUM_CHANNEL_ID,
                        R.string.communities_button);
                createNotificationChannel(INVITATIONS_CHANNEL_ID,
                        R.string.context_group_invitations);
                createNotificationChannel(CONNECTIONS_CHANNEL_ID,
                        R.string.connections);
                return null;
            };
            try {
                androidExecutor.runOnUiThread(task).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new ServiceException(e);
            }
        }
    }

    @TargetApi(26)
    private void createNotificationChannel(String channelId,
                                           @StringRes int name) {
        NotificationChannel nc =
                new NotificationChannel(channelId, appContext.getString(name),
                        IMPORTANCE_DEFAULT);
        nc.setLockscreenVisibility(VISIBILITY_SECRET);
        nc.enableVibration(true);
        nc.enableLights(true);
        nc.setLightColor(getColor(appContext, R.color.green_light));
        notificationManager.createNotificationChannel(nc);
    }

    @Override
    public void stopService() throws ServiceException {
        // Clear all notifications
        Future<Void> f = androidExecutor.runOnUiThread(() -> {
            clearContactNotification();
            clearGroupMessageNotification();
            clearForumPostNotification();
            clearConnectionRequestsNotification();
            clearInvitationNotifications();
            clearContactAddedNotification();
            return null;
        });
        try {
            f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException(e);
        }
    }

    @UiThread
    private void clearContactNotification() {
        contactCounts.clear();
        notificationManager.cancel(PRIVATE_MESSAGE_NOTIFICATION_ID);
    }

    @UiThread
    private void clearGroupMessageNotification() {
        groupCounts.clear();
        notificationManager.cancel(GROUP_MESSAGE_NOTIFICATION_ID);
    }

    @UiThread
    private void clearForumPostNotification() {
        forumCounts.clear();
        notificationManager.cancel(FORUM_POST_NOTIFICATION_ID);
    }

    @UiThread
    private void clearContactAddedNotification() {
        contactAddedTotal = 0;
        notificationManager.cancel(CONTACT_ADDED_NOTIFICATION_ID);
    }

    @Override
    public void eventOccurred(Event e) {
        if (e instanceof SettingsUpdatedEvent) {
            SettingsUpdatedEvent s = (SettingsUpdatedEvent) e;
            if (s.getNamespace().equals(SETTINGS_NAMESPACE))
                settings = s.getSettings();
        } else if (e instanceof PrivateMessageReceivedEvent) {
            PrivateMessageReceivedEvent p =
                    (PrivateMessageReceivedEvent) e;
            showContactNotification(p.getContactId(), p.getGroupId());
        } else if (e instanceof GroupMessageReceivedEvent) {
            GroupMessageReceivedEvent g = (GroupMessageReceivedEvent) e;
            if (g.getMessageHeader().isIncoming())
                showGroupMessageNotification(g.getMessageHeader().getGroupId());
        } else if (e instanceof PendingContactAddedEvent) {
            PendingContactAddedEvent p = (PendingContactAddedEvent) e;
            if (p.getPendingContact().getPendingContactType() == PendingContactType.INCOMING)
                showConnectionRequestNotification(p.getPendingContact().getId().getId(),
                        p.getPendingContact().getAlias());
        }/*else if (e instanceof ForumPostReceivedEvent) {
			ForumPostReceivedEvent f = (ForumPostReceivedEvent) e;
			showForumPostNotification(f.getGroupId());
		}*/ else if (e instanceof ContactAddedEvent) {
            // Don't show notifications for contacts added in person
            showContactAddedNotification();
        } else if (e instanceof GroupInvitationAddedEvent && ((GroupInvitationAddedEvent) e).getInvite().isIncoming()) {
            showInvitationNotification(((GroupInvitationAddedEvent) e).getInvite().getGroupId());
        } else if (e instanceof ContextInvitationAddedEvent && ((ContextInvitationAddedEvent) e).getInvite().isIncoming()) {
            showInvitationNotification(((ContextInvitationAddedEvent) e).getInvite().getContextId());
        }
    }

    @UiThread
    @Override
    public Notification getForegroundNotification() {
        return getForegroundNotification(false);
    }

    @UiThread
    private Notification getForegroundNotification(boolean locked) {
        int title = locked ? R.string.lock_is_locked :
                R.string.ongoing_notification_title;
        int text = locked ? R.string.lock_tap_to_unlock :
                R.string.ongoing_notification_text;
        int icon = locked ? R.drawable.startup_lock :
                R.drawable.ic_notificatio_ongoing;
        // Ongoing foreground notification that shows HeliosTalkService is running
        NotificationCompat.Builder b =
                new NotificationCompat.Builder(appContext, ONGOING_CHANNEL_ID);
        b.setSmallIcon(icon);
        b.setColor(getColor(appContext, R.color.helios_primary));
        b.setContentTitle(appContext.getText(title));
        b.setContentText(appContext.getText(text));
        b.setWhen(0); // Don't show the time
        b.setOngoing(true);
        Intent i = new Intent(appContext, SplashScreenActivity.class);
        b.setContentIntent(PendingIntent.getActivity(appContext, 0, i, 0));
        if (SDK_INT >= 21) {
            b.setCategory(CATEGORY_SERVICE);
            b.setVisibility(VISIBILITY_SECRET);
        }
        b.setPriority(PRIORITY_MIN);
        return b.build();
    }

    @UiThread
    @Override
    public void updateForegroundNotification(boolean locked) {
        Notification n = getForegroundNotification(locked);
        notificationManager.notify(ONGOING_NOTIFICATION_ID, n);
    }

    @UiThread
    private void showContactNotification(ContactId c, String groupId) {
        if (blockedContact != null
                && c.getId().equals(blockedContact.getFirst())
                && groupId.equals(blockedContact.getSecond())) return;
        contactCounts.add(new Pair<>(c.getId(), groupId));
        updateContactNotification(true);
    }

    @Override
    public void clearContactNotification(ContactId c, String groupId) {
        androidExecutor.runOnUiThread(() -> {
            if (contactCounts.removeAll(new Pair(c.getId(), groupId)) > 0)
                updateContactNotification(false);
        });
    }

    @UiThread
    private void updateContactNotification(boolean mayAlertAgain) {
        int contactTotal = contactCounts.getTotal();
        if (contactTotal == 0) {
            clearContactNotification();
        } else if (settings.getBoolean(PREF_NOTIFY_PRIVATE, true)) {
            HeliosTalkNotificationBuilder b = new HeliosTalkNotificationBuilder(
                    appContext, CONTACT_CHANNEL_ID);
            b.setSmallIcon(R.drawable.ic_notificatio_ongoing);
            b.setColorRes(R.color.helios_primary);
            b.setContentTitle(appContext.getText(R.string.app_name));
            b.setContentText(appContext.getResources().getQuantityString(
                    R.plurals.private_message_notification_text, contactTotal,
                    contactTotal));
            b.setNumber(contactTotal);
            b.setNotificationCategory(CATEGORY_MESSAGE);
            if (mayAlertAgain) setAlertProperties(b);
            setDeleteIntent(b, CONTACT_URI);
            Set<Pair<String, String>> contacts = contactCounts.keySet();
            if (contacts.size() == 1) {
                // Touching the notification shows the relevant conversation
                Intent i = new Intent(appContext, ConversationActivity.class);
                Pair<String, String> cInfo = contacts.iterator().next();
                ContactId c = new ContactId(cInfo.getFirst());
                i.putExtra(CONTACT_ID, c.getId());
                i.putExtra(GROUP_ID, cInfo.getSecond());
                i.setData(Uri.parse(CONTACT_URI + "/" + c.getId()));
                i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                TaskStackBuilder t = TaskStackBuilder.create(appContext);
                t.addParentStack(ConversationActivity.class);
                t.addNextIntent(i);
                b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));
            } else {
                // Touching the notification shows the contact list
                Intent i = new Intent(appContext, NavDrawerActivity.class);
                i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                i.setData(CONTACT_URI);
                TaskStackBuilder t = TaskStackBuilder.create(appContext);
                t.addParentStack(NavDrawerActivity.class);
                t.addNextIntent(i);
                b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));
            }
            notificationManager.notify(PRIVATE_MESSAGE_NOTIFICATION_ID,
                    b.build());
        }
    }

    @UiThread
    private void showInvitationNotification(String id) {
        invitationsCounts.add(id);
        updateInvitationNotification(true);
    }

    @Override
    public void clearInvitationNotifications() {
        androidExecutor.runOnUiThread(() -> {
            if (invitationsCounts.keySet().size() > 0) {
                invitationsCounts.clear();
                updateInvitationNotification(false);
            }
        });
    }

    @UiThread
    private void updateInvitationNotification(boolean mayAlertAgain) {
        int invitationsCountsTotal = invitationsCounts.getTotal();
        if (invitationsCountsTotal == 0) {
            clearContactNotification();
        } else if (settings.getBoolean(PREF_NOTIFY_INVITATIONS, true)) {
            HeliosTalkNotificationBuilder b = new HeliosTalkNotificationBuilder(
                    appContext, INVITATIONS_CHANNEL_ID);
            b.setSmallIcon(R.drawable.ic_nearby);
            b.setColorRes(R.color.helios_primary);
            b.setContentTitle(appContext.getText(R.string.app_name));
            b.setContentText(appContext.getResources().getQuantityString(
                    R.plurals.context_group_invitations_notification_text, invitationsCountsTotal,
                    invitationsCountsTotal));
            b.setNumber(invitationsCountsTotal);
            b.setNotificationCategory(CATEGORY_SOCIAL);
            if (mayAlertAgain) setAlertProperties(b);
            setDeleteIntent(b, INVITATION_URI);
            // Touching the notification shows the relevant conversation
            Intent i = new Intent(appContext, InvitationListActivity.class);
            i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            TaskStackBuilder t = TaskStackBuilder.create(appContext);
            t.addParentStack(NavDrawerActivity.class);
            t.addNextIntent(i);
            b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));

            notificationManager.notify(INVITATIONS_NOTIFICATION_ID,
                    b.build());
        }
    }

    @UiThread
    private void setAlertProperties(HeliosTalkNotificationBuilder b) {
        long currentTime = clock.currentTimeMillis();
        if (currentTime - lastSound > SOUND_DELAY) {
            boolean sound = settings.getBoolean(PREF_NOTIFY_SOUND, true);
            String ringtoneUri = settings.get(PREF_NOTIFY_RINGTONE_URI);
            if (sound && !StringUtils.isNullOrEmpty(ringtoneUri)) {
                Uri uri = Uri.parse(ringtoneUri);
                if (!"file".equals(uri.getScheme())) b.setSound(uri);
            }
            b.setDefaults(getDefaults());
            lastSound = currentTime;
        }
    }

    @UiThread
    private int getDefaults() {
        int defaults = DEFAULT_LIGHTS;
        boolean sound = settings.getBoolean(PREF_NOTIFY_SOUND, true);
        String ringtoneUri = settings.get(PREF_NOTIFY_RINGTONE_URI);
        if (sound && (StringUtils.isNullOrEmpty(ringtoneUri) ||
                "file".equals(Uri.parse(ringtoneUri).getScheme())))
            defaults |= DEFAULT_SOUND;
        if (settings.getBoolean(PREF_NOTIFY_VIBRATION, true))
            defaults |= DEFAULT_VIBRATE;
        return defaults;
    }

    private void setDeleteIntent(HeliosTalkNotificationBuilder b, Uri uri) {
        Intent i = new Intent(appContext, NotificationCleanupService.class);
        i.setData(uri);
        b.setDeleteIntent(PendingIntent.getService(appContext, nextRequestId++,
                i, 0));
    }

	/*@Override
	public void clearAllContactNotifications() {
		androidExecutor.runOnUiThread(
				(Runnable) this::clearContactNotification);
	}*/

    @UiThread
    private void showGroupMessageNotification(String g) {
        if (g.equals(blockedGroup)) return;
        groupCounts.add(g);
        updateGroupMessageNotification(true);
    }

    @UiThread
    private void showConnectionRequestNotification(String pid, String alias) {
        pendingContactsCounts.add(pid);
        updateConnectionRequestNotification(true, alias);
    }

    @UiThread
    private void updateConnectionRequestNotification(boolean mayAlertAgain, String alias) {
        int groupTotal = pendingContactsCounts.getTotal();
        if (groupTotal == 0) {
            clearGroupMessageNotification();
        } else if (settings.getBoolean(PREF_NOTIFY_CONNECTIONS, true)) {
            HeliosTalkNotificationBuilder b =
                    new HeliosTalkNotificationBuilder(appContext,
                            CONNECTIONS_CHANNEL_ID);
            b.setSmallIcon(R.drawable.ic_contacts);
            b.setColorRes(R.color.helios_primary);
            b.setContentTitle(appContext.getText(R.string.app_name));
            b.setContentText(appContext.getResources().getQuantityString(
                    R.plurals.connections_message_notification_text, groupTotal,
                    alias));
            b.setNumber(groupTotal);
            b.setNotificationCategory(CATEGORY_SOCIAL);
            if (mayAlertAgain) setAlertProperties(b);
            setDeleteIntent(b, CONNECTIONS_URI);
            // Touching the notification shows the relevant group
            Intent i = new Intent(appContext, PendingContactListActivity.class);
            i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            TaskStackBuilder t = TaskStackBuilder.create(appContext);
            t.addParentStack(PrivateGroupConversationActivity.class);
            t.addNextIntent(i);
            b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));
            notificationManager.notify(CONNECTION_REQUESTS_NOTIFICATION_ID,
                    b.build());
        }
    }

    @Override
    public void clearConnectionRequestsNotification() {
        androidExecutor.runOnUiThread(() -> {
            if (pendingContactsCounts.keySet().size() > 0) {
                pendingContactsCounts.clear();
                updateConnectionRequestNotification(false, "");
            }
        });
    }

    @Override
    public void clearGroupMessageNotification(String g) {
        androidExecutor.runOnUiThread(() -> {
            if (groupCounts.removeAll(g) > 0)
                updateGroupMessageNotification(false);
        });
    }

    @UiThread
    private void updateGroupMessageNotification(boolean mayAlertAgain) {
        int groupTotal = groupCounts.getTotal();
        if (groupTotal == 0) {
            clearGroupMessageNotification();
        } else if (settings.getBoolean(PREF_NOTIFY_GROUP, true)) {
            HeliosTalkNotificationBuilder b =
                    new HeliosTalkNotificationBuilder(appContext,
                            GROUP_CHANNEL_ID);
            b.setSmallIcon(R.drawable.notification_private_group);
            b.setColorRes(R.color.helios_primary);
            b.setContentTitle(appContext.getText(R.string.app_name));
            b.setContentText(appContext.getResources().getQuantityString(
                    R.plurals.group_message_notification_text, groupTotal,
                    groupTotal));
            b.setNumber(groupTotal);
            b.setNotificationCategory(CATEGORY_SOCIAL);
            if (mayAlertAgain) setAlertProperties(b);
            setDeleteIntent(b, GROUP_URI);
            Set<String> groups = groupCounts.keySet();
            if (groups.size() == 1) {
                // Touching the notification shows the relevant group
                Intent i = new Intent(appContext, PrivateGroupConversationActivity.class);
                String g = groups.iterator().next();
                i.putExtra(GROUP_ID, g);
                i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                TaskStackBuilder t = TaskStackBuilder.create(appContext);
                t.addParentStack(PrivateGroupConversationActivity.class);
                t.addNextIntent(i);
                b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));
            } else {
                // Touching the notification shows the group list
                Intent i = new Intent(appContext, NavDrawerActivity.class);
                i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                i.setData(GROUP_URI);
                TaskStackBuilder t = TaskStackBuilder.create(appContext);
                t.addParentStack(NavDrawerActivity.class);
                t.addNextIntent(i);
                b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));
            }
            notificationManager.notify(GROUP_MESSAGE_NOTIFICATION_ID,
                    b.build());
        }
    }

	/*@Override
	public void clearAllGroupMessageNotifications() {
		androidExecutor.runOnUiThread(
				(Runnable) this::clearGroupMessageNotification);
	}

	@UiThread
	private void showForumPostNotification(GroupId g) {
		if (g.equals(blockedGroup)) return;
		forumCounts.add(g);
		updateForumPostNotification(true);
	}

	@Override
	public void clearForumPostNotification(GroupId g) {
		androidExecutor.runOnUiThread(() -> {
			if (forumCounts.removeAll(g) > 0)
				updateForumPostNotification(false);
		});
	}

	@UiThread
	private void updateForumPostNotification(boolean mayAlertAgain) {
		int forumTotal = forumCounts.getTotal();
		if (forumTotal == 0) {
			clearForumPostNotification();
		} else if (settings.getBoolean(PREF_NOTIFY_FORUM, true)) {
			HeliosTalkNotificationBuilder b =
					new HeliosTalkNotificationBuilder(appContext,
							FORUM_CHANNEL_ID);
			b.setSmallIcon(R.drawable.notification_forum);
			b.setColorRes(R.color.helios_primary);
			b.setContentTitle(appContext.getText(R.string.app_name));
			b.setContentText(appContext.getResources().getQuantityString(
					R.plurals.community_post_notification_text, forumTotal,
					forumTotal));
			b.setNumber(forumTotal);
			b.setNotificationCategory(CATEGORY_SOCIAL);
			if (mayAlertAgain) setAlertProperties(b);
			setDeleteIntent(b, FORUM_URI);
			Set<GroupId> forums = forumCounts.keySet();
			if (forums.size() == 1) {
				// Touching the notification shows the relevant forum
				Intent i = new Intent(appContext, ForumActivity.class);
				GroupId g = forums.iterator().next();
				i.putExtra(GROUP_ID, g.getBytes());
				String idHex = StringUtils.toHexString(g.getBytes());
				i.setData(Uri.parse(FORUM_URI + "/" + idHex));
				i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
				TaskStackBuilder t = TaskStackBuilder.create(appContext);
				t.addParentStack(ForumActivity.class);
				t.addNextIntent(i);
				b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));
			} else {
				// Touching the notification shows the forum list
				Intent i = new Intent(appContext, NavDrawerActivity.class);
				i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
				i.setData(FORUM_URI);
				TaskStackBuilder t = TaskStackBuilder.create(appContext);
				t.addParentStack(NavDrawerActivity.class);
				t.addNextIntent(i);
				b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));
			}
			notificationManager.notify(FORUM_POST_NOTIFICATION_ID, b.build());
		}
	}

	@Override
	public void clearAllForumPostNotifications() {
		androidExecutor.runOnUiThread(
				(Runnable) this::clearForumPostNotification);
	}*/

    @UiThread
    private void showContactAddedNotification() {
        contactAddedTotal++;
        updateContactAddedNotification();
    }

    @UiThread
    private void updateContactAddedNotification() {
        HeliosTalkNotificationBuilder b =
                new HeliosTalkNotificationBuilder(appContext,
                        CONTACT_CHANNEL_ID);
        b.setSmallIcon(R.drawable.notification_contact_added);
        b.setColorRes(R.color.helios_primary);
        b.setContentTitle(appContext.getText(R.string.app_name));
        b.setContentText(appContext.getResources().getQuantityString(
                R.plurals.contact_added_notification_text, contactAddedTotal,
                contactAddedTotal));
        b.setNotificationCategory(CATEGORY_MESSAGE);
        setAlertProperties(b);
        setDeleteIntent(b, CONTACT_ADDED_URI);
        // Touching the notification shows the contact list
        Intent i = new Intent(appContext, NavDrawerActivity.class);
        i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        i.setData(CONTACT_URI);
        TaskStackBuilder t = TaskStackBuilder.create(appContext);
        t.addParentStack(NavDrawerActivity.class);
        t.addNextIntent(i);
        b.setContentIntent(t.getPendingIntent(nextRequestId++, 0));

        notificationManager.notify(CONTACT_ADDED_NOTIFICATION_ID,
                b.build());
    }

    @Override
    public void clearAllContactAddedNotifications() {
        androidExecutor.runOnUiThread(this::clearContactAddedNotification);
    }

    @Override
    public void showSignInNotification() {
        if (blockSignInReminder) return;
        if (SDK_INT >= 26) {
            NotificationChannel channel =
                    new NotificationChannel(REMINDER_CHANNEL_ID, appContext
                            .getString(
                                    R.string.reminder_notification_channel_title),
                            IMPORTANCE_LOW);
            channel.setLockscreenVisibility(
                    NotificationCompat.VISIBILITY_SECRET);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder b =
                new NotificationCompat.Builder(appContext, REMINDER_CHANNEL_ID);
        b.setSmallIcon(R.drawable.ic_signout);
        b.setColor(getColor(appContext, R.color.helios_primary));
        b.setContentTitle(
                appContext.getText(R.string.reminder_notification_title));
        b.setContentText(
                appContext.getText(R.string.reminder_notification_text));
        b.setAutoCancel(true);
        b.setWhen(0); // Don't show the time
        b.setPriority(PRIORITY_LOW);

        // Add a 'Dismiss' action
        String actionTitle =
                appContext.getString(R.string.reminder_notification_dismiss);
        Intent i1 = new Intent(appContext, SignInReminderReceiver.class);
        i1.setAction(ACTION_DISMISS_REMINDER);
        PendingIntent actionIntent =
                PendingIntent.getBroadcast(appContext, 0, i1, 0);
        b.addAction(0, actionTitle, actionIntent);

        Intent i = new Intent(appContext, SplashScreenActivity.class);
        i.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
        b.setContentIntent(PendingIntent.getActivity(appContext, 0, i, 0));

        notificationManager.notify(REMINDER_NOTIFICATION_ID, b.build());
    }

    @Override
    public void clearSignInNotification() {
        notificationManager.cancel(REMINDER_NOTIFICATION_ID);
    }

    @Override
    public void blockSignInNotification() {
        blockSignInReminder = true;
    }

    @Override
    public void blockNotification(String g) {
        androidExecutor.runOnUiThread((Runnable) () -> blockedGroup = g);
    }

    @Override
    public void unblockNotification(String g) {
        androidExecutor.runOnUiThread(() -> {
            if (g.equals(blockedGroup)) blockedGroup = null;
        });
    }

    @Override
    public void blockContactNotification(ContactId c, String groupId) {
        androidExecutor.runOnUiThread((Runnable) () -> blockedContact = new Pair(c.getId(),
                groupId));
    }

    @Override
    public void unblockContactNotification(ContactId c, String groupId) {
        androidExecutor.runOnUiThread(() -> {
            if (c.getId().equals(blockedContact.getFirst())
                    && groupId.equals(blockedContact.getSecond()))
                blockedContact = null;
        });
    }
}
