package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.SelectionTracker.SelectionObserver;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.api.Pair;
import eu.h2020.helios_social.happ.helios.talk.api.contact.event.ContactRemovedEvent;
import eu.h2020.helios_social.happ.helios.talk.api.db.NoSuchContactException;
import eu.h2020.helios_social.happ.helios.talk.api.event.Event;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventListener;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.happ.helios.talk.profile.ContactProfileActivity;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.happ.helios.talk.view.TextInputView;
import eu.h2020.helios_social.happ.helios.talk.view.TextSendController;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.messaging.event.PrivateMessageReceivedEvent;
import eu.h2020.helios_social.modules.videocall.VideoCallActivity;

import static android.widget.Toast.LENGTH_SHORT;
import static androidx.core.view.ViewCompat.setTransitionName;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logDuration;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logException;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.now;
import static eu.h2020.helios_social.happ.helios.talk.api.util.StringUtils.isNullOrEmpty;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingConstants.MAX_MESSAGE_TEXT_LENGTH;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ConversationActivity extends HeliosTalkActivity
        implements EventListener, ConversationListener,
        ConversationVisitor.TextCache, ActionMode.Callback,
        TextSendController.SendListener {

    public static final String CONTACT_ID = "helios.talk.CONTACT_ID";
    public static final String GROUP_ID = "helios.talk.GROUP_ID";

    private static final Logger LOG =
            getLogger(ConversationActivity.class.getName());

    @Inject
    AndroidNotificationManager notificationManager;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    volatile ContextualEgoNetwork egoNetwork;

    // Fields that are accessed from background threads must be volatile
    @Inject
    volatile ContactManager contactManager;
    @Inject
    volatile ConversationManager conversationManager;
    @Inject
    volatile EventBus eventBus;

    private final Map<String, String> textCache = new ConcurrentHashMap<>();

    private final Observer<String> contactNameObserver = name -> {
        requireNonNull(name);
        loadMessages();
    };

    private ConversationViewModel viewModel;
    private ConversationVisitor visitor;
    private ConversationAdapter adapter;
    private Toolbar toolbar;
    private CircleImageView toolbarAvatar;
    private ImageView toolbarStatus;
    private TextView toolbarTitle;
    private HeliosTalkRecyclerView list;
    private LinearLayoutManager layoutManager;
    private TextInputView textInputView;
    private TextSendController sendController;
    private SelectionTracker<String> tracker;
    @Nullable
    private Parcelable layoutManagerState;
    @Nullable
    private ActionMode actionMode;

    private volatile ContactId contactId;
    private volatile String groupId;

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        Intent i = getIntent();
        String id = i.getStringExtra(CONTACT_ID);
        groupId = i.getStringExtra(GROUP_ID);
        if (id == null) throw new IllegalStateException();
        contactId = new ContactId(id);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(ConversationViewModel.class);

        setContentView(R.layout.activity_conversation);

        // Custom Toolbar
        toolbar = requireNonNull(setUpCustomToolbar(true));
        toolbarAvatar = toolbar.findViewById(R.id.contactAvatar);
        toolbarStatus = toolbar.findViewById(R.id.contactStatus);
        toolbarTitle = toolbar.findViewById(R.id.contactName);
        toolbarAvatar.setOnClickListener(v -> {
            Intent contactProfileActivity =
                    new Intent(this, ContactProfileActivity.class);
            contactProfileActivity.putExtra(CONTACT_ID, contactId.getId());
            startActivity(contactProfileActivity);
        });

        toolbarAvatar.setImageDrawable(getDrawable(R.drawable.ic_person));
        viewModel.getContactDisplayName().observe(this, contactName -> {
            requireNonNull(contactName);
            toolbarTitle.setText(contactName);
        });
        viewModel.isContactDeleted().observe(this, deleted -> {
            requireNonNull(deleted);
            if (deleted) finish();
        });
        viewModel.getAddedPrivateMessage().observeEvent(this,
                this::onAddedPrivateMessage);

        setTransitionName(toolbarAvatar,
                UiUtils.getAvatarTransitionName(contactId));
        setTransitionName(toolbarStatus,
                UiUtils.getBulbTransitionName(contactId));

        visitor = new ConversationVisitor(this, this,/* this,*/
                viewModel.getContactDisplayName());
        adapter = new ConversationAdapter(this, this);
        list = findViewById(R.id.conversationView);
        layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        list.setEmptyText(getString(R.string.no_private_messages));
        ConversationScrollListener scrollListener =
                new ConversationScrollListener(adapter, viewModel);
        list.getRecyclerView().addOnScrollListener(scrollListener);
        addSelectionTracker();

        textInputView = findViewById(R.id.text_input_container);

        sendController = new TextSendController(textInputView, this, false);

        textInputView.setSendController(sendController);
        textInputView.setMaxTextLength(MAX_MESSAGE_TEXT_LENGTH);
        textInputView.setReady(false);
        textInputView.setOnKeyboardShownListener(this::scrollToBottom);
    }

    private void scrollToBottom() {
        int items = adapter.getItemCount();
        if (items > 0) list.scrollToPosition(items - 1);
    }

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.addListener(this);
        notificationManager.blockContactNotification(contactId, groupId);
        notificationManager.clearContactNotification(contactId, groupId);
        displayContactOnlineStatus();
        viewModel.getContactDisplayName().observe(this, contactNameObserver);
        list.startPeriodicUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Trigger loading of contact data, noop if data was loaded already.
        //
        // We can only start loading data *after* we are sure
        // the user has signed in. After sign-in, onCreate() isn't run again.
        if (signedIn()) {
            viewModel.setContactId(contactId);
            viewModel.setGroupId(groupId);
            viewModel.setContextId(
                    egoNetwork.getCurrentContext().getData().toString()
                            .split("%")[1]);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.removeListener(this);
        notificationManager.unblockContactNotification(contactId, groupId);
        viewModel.getContactDisplayName().removeObserver(contactNameObserver);
        list.stopPeriodicUpdate();
        egoNetwork.save();
        LOG.info("Conversation Activity Stopped with contact: " + contactId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (layoutManager != null) {
            layoutManagerState = layoutManager.onSaveInstanceState();
            outState.putParcelable("layoutManager", layoutManagerState);
        }
        if (tracker != null) tracker.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        layoutManagerState = savedInstanceState.getParcelable("layoutManager");
        if (tracker != null) tracker.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_actions, menu);

        // enable alias action if available
        UiUtils.observeOnce(viewModel.getContact(), this, contact ->
                menu.findItem(R.id.action_set_alias).setEnabled(true));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, NavDrawerActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_set_alias:
				/*AliasDialogFragment.newInstance().show(
						getSupportFragmentManager(), AliasDialogFragment.TAG);*/
                return true;
            case R.id.action_delete_all_messages:
                //askToDeleteAllMessages();
                return true;
            case R.id.action_social_remove_person:
                //askToRemoveContact();
                return true;
            case R.id.start_video_call:
                startVideoCall();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.conversation_message_actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // no update needed
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            //deleteSelectedMessages();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        tracker.clearSelection();
        actionMode = null;
    }

    private void addSelectionTracker() {
        RecyclerView recyclerView = list.getRecyclerView();
        if (recyclerView.getAdapter() != adapter)
            throw new IllegalStateException();

        tracker = new SelectionTracker.Builder<>(
                "conversationSelection",
                recyclerView,
                new ConversationItemKeyProvider(adapter),
                new ConversationItemDetailsLookup(recyclerView),
                StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
        ).build();

        SelectionObserver<String> observer = new SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(String key, boolean selected) {
                if (selected && actionMode == null) {
                    actionMode = startActionMode(ConversationActivity.this);
                    updateActionModeTitle();
                } else if (actionMode != null) {
                    if (selected || tracker.hasSelection()) {
                        updateActionModeTitle();
                    } else {
                        actionMode.finish();
                    }
                }
            }
        };
        tracker.addObserver(observer);
        adapter.setSelectionTracker(tracker);
    }

    private void updateActionModeTitle() {
        if (actionMode == null) throw new IllegalStateException();
        String title = String.valueOf(tracker.getSelection().size());
        actionMode.setTitle(title);
    }

    private Collection<String> getSelection() {
        Selection<String> selection = tracker.getSelection();
        List<String> messages = new ArrayList<>(selection.size());
        for (String str : selection) {
            String id = str;
            messages.add(id);
        }
        return messages;
    }

    @UiThread
    private void displayContactOnlineStatus() {
		/*if (connectionRegistry.isConnected(contactId)) {
			toolbarStatus.setImageDrawable(ContextCompat.getDrawable(
					ConversationActivity.this, R.drawable.contact_online));
			toolbarStatus.setContentDescription(getString(R.string.online));
		} else {
			toolbarStatus.setImageDrawable(ContextCompat.getDrawable(
					ConversationActivity.this, R.drawable.contact_offline));
			toolbarStatus.setContentDescription(getString(R.string.offline));
		}*/
    }

    private void loadMessages() {
        int revision = adapter.getRevision();
        runOnDbThread(() -> {
            try {
                long start = now();
                Collection<MessageHeader> headers =
                        conversationManager.getMessageHeaders(groupId);
                logDuration(LOG, "Loading messages", start);
                // Sort headers by timestamp in *descending* order
                List<MessageHeader> sorted =
                        new ArrayList<>(headers);
                sort(sorted, (a, b) ->
                        Long.compare(b.getTimestamp(), a.getTimestamp()));
                if (!sorted.isEmpty()) {
                    // If the latest header is a private message, eagerly load
                    // its size so we can set the scroll position correctly
                    MessageHeader latest = sorted.get(0);
                    eagerlyLoadMessageSize((MessageHeader) latest);
                }
                displayMessages(revision, sorted);
            } catch (NoSuchContactException e) {
                finishOnUiThread();
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    private void eagerlyLoadMessageSize(MessageHeader h) {
        try {
            String id = h.getMessageId();
            String text = textCache.get(id);
            if (text == null) {
                LOG.info("Eagerly loading text for latest message");
                text = conversationManager.getMessageText(id);
                textCache.put(id, requireNonNull(text));
            }

        } catch (DbException e) {
            logException(LOG, WARNING, e);
        }
    }

    private void displayMessages(int revision,
                                 Collection<MessageHeader> headers) {
        runOnUiThreadUnlessDestroyed(() -> {
            if (revision == adapter.getRevision()) {
                adapter.incrementRevision();
                textInputView.setReady(true);

                List<ConversationItem> items = createItems(headers);
                adapter.addAll(items);
                list.showData();
                if (layoutManagerState == null) {
                    scrollToBottom();
                } else {
                    // Restore the previous scroll position
                    layoutManager.onRestoreInstanceState(layoutManagerState);
                }
            } else {
                LOG.info("Concurrent update, reloading");
                loadMessages();
            }
        });
    }

    /**
     * Creates ConversationItems from headers loaded from the database.
     * <p>
     * Attention: Call this only after contactName has been initialized.
     */
    private List<ConversationItem> createItems(
            Collection<MessageHeader> headers) {
        List<ConversationItem> items = new ArrayList<>(headers.size());
        for (MessageHeader h : headers) {
            ConversationItem item = h.accept(visitor);
            if (item instanceof VideoCallConversationItem &&
                    item.getText() == null) {
                ((VideoCallConversationItem) item)
                        .setRoomId(getText(item.getId()));
            }
            items.add(item);
        }
        return items;
    }

    private void loadMessageText(String messageId) {
        runOnDbThread(() -> {
            try {
                long start = now();
                String text = conversationManager.getMessageText(messageId);
                logDuration(LOG, "Loading text", start);
                displayMessageText(messageId, requireNonNull(text));
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    private void displayMessageText(String messageId, String text) {
        runOnUiThreadUnlessDestroyed(() -> {
            textCache.put(messageId, text);
            Pair<Integer, ConversationItem> pair =
                    adapter.getMessageItem(messageId);
            if (pair != null) {
                boolean scroll = shouldScrollWhenUpdatingMessage();
                if (pair.getSecond() instanceof ConversationMessageItem) {
                    pair.getSecond().setText(text);
                } else {
                    ((VideoCallConversationItem) pair.getSecond())
                            .setRoomId(text);
                }
                adapter.notifyItemChanged(pair.getFirst());
                if (scroll) scrollToBottom();
            }
        });
    }

    // When a message's text or attachments are loaded, scroll to the bottom
    // if the conversation is visible and we were previously at the bottom
    private boolean shouldScrollWhenUpdatingMessage() {
        return getLifecycle().getCurrentState().isAtLeast(STARTED)
                && adapter.isScrolledToBottom(layoutManager);
    }

    @Override
    public void eventOccurred(Event e) {
        if (e instanceof ContactRemovedEvent) {
            ContactRemovedEvent c = (ContactRemovedEvent) e;
            if (c.getContactId().equals(contactId)) {
                LOG.info("Contact removed");
                supportFinishAfterTransition();
            }
        } else if (e instanceof PrivateMessageReceivedEvent) {
            PrivateMessageReceivedEvent p =
                    (PrivateMessageReceivedEvent) e;
            if (p.getGroupId().equals(groupId)) {
                LOG.info("Message received, adding");
                onNewConversationMessage(p.getMessageHeader());
            }
        }
    }

    @UiThread
    private void addConversationItem(ConversationItem item) {
        if (item instanceof VideoCallConversationItem &&
                item.getText() == null) {
            ((VideoCallConversationItem) item)
                    .setRoomId(getText(item.getId()));
        }
        adapter.incrementRevision();
        adapter.add(item);
        // When adding a new message, scroll to the bottom if the conversation
        // is visible, even if we're not currently at the bottom
        if (getLifecycle().getCurrentState().isAtLeast(STARTED))
            scrollToBottom();
    }


    @UiThread
    private void onNewConversationMessage(MessageHeader h) {
        // visitor also loads message text (if existing)
        addConversationItem(h.accept(visitor));
    }

    @UiThread
    private void markMessages(Collection<String> messageIds, boolean sent,
                              boolean seen) {
        adapter.incrementRevision();
        Set<String> messages = new HashSet<>(messageIds);
        SparseArray<ConversationItem> list = adapter.getOutgoingMessages();
        for (int i = 0; i < list.size(); i++) {
            ConversationItem item = list.valueAt(i);
            if (item instanceof VideoCallConversationItem &&
                    item.getText() == null) {
                ((VideoCallConversationItem) item)
                        .setRoomId(getText(item.getId()));
            }

            if (messages.contains(item.getId())) {
                item.setSent(sent);
                item.setSeen(seen);
                adapter.notifyItemChanged(list.keyAt(i));
            }
        }
    }

    @Override
    public void onSendClick(@Nullable String text/*,
			List<AttachmentHeader> attachmentHeaders*/) {
        if (isNullOrEmpty(text))
            throw new AssertionError();
        long timestamp = System.currentTimeMillis();
        timestamp = Math.max(timestamp, getMinTimestampForNewMessage());
        viewModel.sendMessage(text, /*attachmentHeaders, */timestamp);
        textInputView.clearText();
    }

    private void onSendVideoCallRequest(String room_id) {
        long timestamp = System.currentTimeMillis();
        timestamp = Math.max(timestamp, getMinTimestampForNewMessage());
        viewModel.sendVideoCallRequest(timestamp, room_id);
        textInputView.clearText();
    }

    private long getMinTimestampForNewMessage() {
        // Don't use an earlier timestamp than the newest message
        ConversationItem item = adapter.getLastItem();
        return item == null ? 0 : item.getTime() + 1;
    }

    private void onAddedPrivateMessage(@Nullable MessageHeader h) {
        if (h == null) return;
        addConversationItem(h.accept(visitor));
    }

    private void askToDeleteAllMessages() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.dialog_title_delete_all_messages));
        builder.setMessage(
                getString(R.string.dialog_message_delete_all_messages));
        builder.setNegativeButton(R.string.delete, null);
        //(dialog, which) -> deleteAllMessages());
        builder.setPositiveButton(R.string.cancel, null);
        builder.show();
    }

    private void startVideoCall() {
        String room_id = UUID.randomUUID().toString();
        onSendVideoCallRequest(room_id);
        Intent videoCallIntent =
                new Intent(this, VideoCallActivity.class);
        videoCallIntent.putExtra("room_name", room_id);
        startActivity(videoCallIntent);
    }

    private void askToRemoveContact() {
        DialogInterface.OnClickListener okListener =
                (dialog, which) -> removeContact();
        AlertDialog.Builder builder =
                new AlertDialog.Builder(ConversationActivity.this,
                        R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.dialog_title_delete_contact));
        builder.setMessage(
                getString(R.string.dialog_message_delete_contact));
        builder.setNegativeButton(R.string.delete, okListener);
        builder.setPositiveButton(R.string.cancel, null);
        builder.show();
    }

    private void removeContact() {
        runOnDbThread(() -> {
            try {
                contactManager.deleteContact(contactId);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            } finally {
                finishAfterContactRemoved();
            }
        });
    }

    private void finishAfterContactRemoved() {
        runOnUiThreadUnlessDestroyed(() -> {
            String deleted = getString(R.string.contact_deleted_toast);
            Toast.makeText(ConversationActivity.this, deleted, LENGTH_SHORT)
                    .show();
            supportFinishAfterTransition();
        });
    }

    @Override
    public void onFavouriteClicked(View view,
                                   ConversationMessageItem messageItem) {
        if (messageItem.isFavourite()) {
            messageItem.setFavourite(false);
            ((ImageView) view).setImageResource(R.drawable.ic_star_disable);
            try {
                conversationManager.unfavourite(messageItem.getId());
            } catch (DbException e) {
                e.printStackTrace();
            }
        } else {
            messageItem.setFavourite(true);
            ((ImageView) view).setImageResource(R.drawable.ic_star_enable);
            try {
                conversationManager.favourite(messageItem.getId());
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }


    @Nullable
    @Override
    public String getText(String messageId) {
        String text = textCache.get(messageId);
        if (text == null) loadMessageText(messageId);
        return text;
    }

}
