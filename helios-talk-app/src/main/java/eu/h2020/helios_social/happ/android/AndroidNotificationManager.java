package eu.h2020.helios_social.happ.android;

import android.app.Notification;

/**
 * Manages notifications for private messages, forum posts and group posts
 */
public interface AndroidNotificationManager {

	// Keys for notification preferences
	String PREF_NOTIFY_PRIVATE = "notifyPrivateMessages";
	String PREF_NOTIFY_GROUP = "notifyGroupMessages";
	String PREF_NOTIFY_FORUM = "notifyForumPosts";

	String PREF_NOTIFY_SOUND = "notifySound";
	String PREF_NOTIFY_RINGTONE_NAME = "notifyRingtoneName";
	String PREF_NOTIFY_RINGTONE_URI = "notifyRingtoneUri";
	String PREF_NOTIFY_VIBRATION = "notifyVibration";

	// Notification IDs
	int ONGOING_NOTIFICATION_ID = 1;
	int FAILURE_NOTIFICATION_ID = 2;
	int REMINDER_NOTIFICATION_ID = 3;
	int PRIVATE_MESSAGE_NOTIFICATION_ID = 4;
	int GROUP_MESSAGE_NOTIFICATION_ID = 5;
	int FORUM_POST_NOTIFICATION_ID = 6;
	int CONTACT_ADDED_NOTIFICATION_ID = 8;

	// Channel IDs
	String CONTACT_CHANNEL_ID = "contacts";
	String GROUP_CHANNEL_ID = "groups";
	String FORUM_CHANNEL_ID = "forums";
	// Channels are sorted by channel ID in the Settings app, so use IDs
	// that will sort below the main channels such as contacts
	String ONGOING_CHANNEL_ID = "zForegroundService";
	String FAILURE_CHANNEL_ID = "zStartupFailure";
	String REMINDER_CHANNEL_ID = "zSignInReminder";

	// Actions for pending intents
	String ACTION_DISMISS_REMINDER = "dismissReminder";

	Notification getForegroundNotification();

	void updateForegroundNotification(boolean locked);

	//void clearContactNotification(ContactId c);

	//void clearAllContactNotifications();

	//void clearGroupMessageNotification(GroupId g);

	//void clearAllGroupMessageNotifications();

	//void clearForumPostNotification(GroupId g);

	//void clearAllForumPostNotifications();

	//void clearAllContactAddedNotifications();

	void showSignInNotification();

	void clearSignInNotification();

	void blockSignInNotification();

	//void blockContactNotification(ContactId c);

	//void unblockContactNotification(ContactId c);

	//void blockNotification(GroupId g);

	//void unblockNotification(GroupId g);
}
