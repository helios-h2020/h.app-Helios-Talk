package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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

import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.activity.RequestCodes;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentRetriever;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts.ShareContactActivity;
import eu.h2020.helios_social.happ.helios.talk.shared.controllers.ConnectionController;
import eu.h2020.helios_social.happ.helios.talk.view.FileAttachmentPreview;
import eu.h2020.helios_social.happ.helios.talk.view.ImagePreview;
import eu.h2020.helios_social.happ.helios.talk.view.TextAttachmentController;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.connection.ConnectionRegistry;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.GeneralContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.LocationContextProxy;
import eu.h2020.helios_social.modules.groupcommunications_utils.Pair;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.ContactExistsException;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.InvalidActionException;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.PendingContactExistsException;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContactRemovedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.NoSuchContactException;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
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
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.MessageSentEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PrivateMessageReceivedEvent;
import eu.h2020.helios_social.modules.videocall.VideoCallActivity;

import static android.widget.Toast.LENGTH_SHORT;
import static androidx.core.view.ViewCompat.setTransitionName;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingConstants.MAX_IMAGE_ATTACHMENTS_PER_MESSAGE;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils.isNullOrEmpty;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingConstants.MAX_MESSAGE_TEXT_LENGTH;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ConversationActivity extends HeliosTalkActivity
        implements EventListener, ConversationListener,
        ConversationVisitor.TextCache, ActionMode.Callback,
        TextSendController.SendListener,
        ConversationVisitor.AttachmentCache,
        TextAttachmentController.AttachmentListener {

    public static final String CONTACT_ID = "helios.talk.CONTACT_ID";
    public static final String GROUP_ID = "helios.talk.GROUP_ID";
    public static final String CONTACT_NAME = "helios.talk.CONTACT_NAME";
    public static final String ATTACHMENT_URI = "helios.talk.ATTACHMENT_URI";
    public static final String TEXT_MESSAGE = "helios.talk.TEXT_MESSAGE";
    public static final String TIMESTAMP = "helios.talk.TIMESTAMP";

    private final int CAMERA_PERMISSIONS_CODE = 107;

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
    volatile ConnectionRegistry connectionRegistry;
    @Inject
    EventBus eventBus;
    @Inject
    ConnectionController connectionController;
    private String[] CAPTURE_FROM_CAMERA_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private final Map<String, String> textCache = new ConcurrentHashMap<>();

    private final Observer<String> contactNameObserver = name -> {
        requireNonNull(name);
        loadMessages();
    };

    private ConversationViewModel viewModel;
    private AttachmentRetriever attachmentRetriever;
    private ConversationVisitor visitor;
    private ConversationAdapter adapter;
    private Toolbar toolbar;
    private CircleImageView toolbarAvatar;
    private ImageView toolbarStatus;
    private TextView toolbarTitle;
    private HeliosTalkRecyclerView list;
    private LinearLayoutManager layoutManager;
    private TextInputView textInputView;
    private TextAttachmentController sendController;
    private SelectionTracker<String> tracker;
    private Intent photoCaptureIntent;
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
        attachmentRetriever = viewModel.getAttachmentRetriever();

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

        visitor = new ConversationVisitor(this, this, this,
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

        ImagePreview imagePreview = findViewById(R.id.imagePreview);
        FileAttachmentPreview fileAttachmentPreview = findViewById(R.id.fileAttachmentPreview);
        sendController = new TextAttachmentController(this, textInputView, imagePreview,
                                                      fileAttachmentPreview, this);
        sendController.setImagesSupported();

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

            UiUtils.observeOnce(viewModel.getContext(), this, context -> {
                String cid = egoNetwork.getCurrentContext().getData().toString().split("%")[1];
                if (!cid.equals(context.getId())) {
                    egoNetwork.setCurrent(egoNetwork.getOrCreateContext(context.getName() + "%" + context.getId()));
                    if (context instanceof GeneralContextProxy) {
                        GeneralContextProxy generalContext = (GeneralContextProxy) context;
                        styleBasedOnContext(context.getId(), generalContext.getColor());
                        toolbar.setBackground(
                                new ColorDrawable(generalContext.getColor()));

                    } else if (context instanceof LocationContextProxy) {
                        LocationContextProxy generalContext = (LocationContextProxy) context;
                        styleBasedOnContext(context.getId(), generalContext.getColor());
                        toolbar.setBackground(
                                new ColorDrawable(generalContext.getColor()));
                    }
                }
            });
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
        UiUtils.observeOnce(viewModel.getContact(), this, contact -> {
            //menu.findItem(R.id.action_set_alias).setEnabled(true);
            if (contact.getProfilePicture() != null)
                toolbarAvatar.setImageBitmap(BitmapFactory.decodeByteArray(
                        contact.getProfilePicture(),
                        0,
                        contact.getProfilePicture().length)
                );
        });

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
            case R.id.action_social_remove_person:
                Toast.makeText(this, "Temporarily Unavailable Operation!", Toast.LENGTH_LONG).show();
                //askToRemoveContact();
                return true;
            case R.id.start_video_call:
                startVideoCall();
                return true;
            case R.id.action_share_contact:
                Intent intent = new Intent(this, ShareContactActivity.class);
                intent.putExtra(CONTACT_ID, contactId.getId());
                intent.putExtra(CONTACT_NAME, viewModel.getContactDisplayName().getValue());
                startActivity(intent);
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
    protected void onActivityResult(int request, int result,
                                    @Nullable Intent data) {
        super.onActivityResult(request, result, data);

        if (request == RequestCodes.REQUEST_ATTACH_IMAGE &&
                result == RESULT_OK) {
            sendController.onAttachmentsReceived(data, Message.Type.IMAGES);
        } else if (request == RequestCodes.REQUEST_ATTACH_CAPTURED_PHOTO && result == RESULT_OK) {
            sendController.onAttachmentsReceived(data, Message.Type.IMAGES);
        } else if (request == RequestCodes.REQUEST_ATTACH_FILE && result == RESULT_OK) {
            sendController.onAttachmentsReceived(data, Message.Type.FILE_ATTACHMENT);
        }
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
        if (connectionRegistry.isConnected(contactId)) {
            toolbarStatus.setImageDrawable(ContextCompat.getDrawable(
                    ConversationActivity.this, R.drawable.contact_online));
            toolbarStatus.setContentDescription(getString(R.string.online));
        } else {
            toolbarStatus.setImageDrawable(ContextCompat.getDrawable(
                    ConversationActivity.this, R.drawable.contact_offline));
            toolbarStatus.setContentDescription(getString(R.string.offline));
        }
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

    private void loadMessageAttachments(String messageId) {
        runOnDbThread(() -> {
            try {
                attachmentRetriever.getMessageAttachments(messageId);
                displayMessageAttachments(messageId, attachmentRetriever.cacheGet(messageId));
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    private void eagerlyLoadMessageSize(MessageHeader h) {
        try {

            if (h.getMessageType() != Message.Type.IMAGES && h.getMessageType() != Message.Type.FILE_ATTACHMENT) {
                String id = h.getMessageId();
                String text = textCache.get(id);
                if (text == null) {
                    LOG.info("Eagerly loading text for latest message");
                    text = conversationManager.getMessageText(id);
                    textCache.put(id, requireNonNull(text));
                }
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

    private void displayMessageAttachments(String messageId, List<AttachmentItem> items) {
        runOnUiThreadUnlessDestroyed(() -> {
            Pair<Integer, ConversationItem> pair =
                    adapter.getMessageItem(messageId);
            if (pair != null) {
                boolean scroll = shouldScrollWhenUpdatingMessage();
                ((ConversationMessageItem) pair.getSecond())
                        .setAttachmentList(items);
                adapter.notifyItemChanged(pair.getFirst());
                if (scroll) scrollToBottom();
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
            if (item instanceof VideoCallConversationItem && ((VideoCallConversationItem) item).getRoomId() == null) {
                ((VideoCallConversationItem) item).setRoomId(getText(item.getId()));
            } else if (item instanceof SharedContactConversationItem && ((SharedContactConversationItem) item).getPeerInfo() == null) {
                PeerInfo peerInfo = new Gson().fromJson(getText(item.getId()), PeerInfo.class);
                ((SharedContactConversationItem) item).setPeerInfo(peerInfo);
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
                } else if (pair.getSecond() instanceof VideoCallConversationItem) {
                    ((VideoCallConversationItem) pair.getSecond())
                            .setRoomId(text);
                } else {
                    PeerInfo peerInfo = new Gson().fromJson(text, PeerInfo.class);
                    ((SharedContactConversationItem) pair.getSecond())
                            .setPeerInfo(peerInfo);
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
        } else if (e instanceof MessageSentEvent) {
            MessageSentEvent messageSentEvent = (MessageSentEvent) e;
            markMessage(messageSentEvent.getMessageId(), true, false);
        }
    }

    @UiThread
    private void addConversationItem(ConversationItem item) {
        if (item instanceof VideoCallConversationItem &&
                item.getText() == null) {
            ((VideoCallConversationItem) item)
                    .setRoomId(getText(item.getId()));
        } else if (item instanceof SharedContactConversationItem && item.getText() == null) {
            PeerInfo peerInfo = new Gson().fromJson(item.getId(), PeerInfo.class);
            ((SharedContactConversationItem) item).setPeerInfo(peerInfo);
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
    private void markMessage(String messageId, boolean sent, boolean seen) {
        adapter.incrementRevision();
        HashMap<String, ConversationItem> outgoingMessages = adapter.getOutgoingMessagesAsMap();
        if (outgoingMessages.containsKey(messageId)) {
            ConversationItem item = outgoingMessages.get(messageId);
            item.setSeen(seen);
            item.setSent(sent);
            adapter.notifyItemChanged(item.getIndex());
        }
    }

    @Override
    public void onSendClick(@Nullable String text, List<AttachmentItem> attachments, Message.Type messageType) {
        if (isNullOrEmpty(text) && attachments.isEmpty())
            throw new AssertionError();
        long timestamp = System.currentTimeMillis();
        timestamp = Math.max(timestamp, getMinTimestampForNewMessage());

        viewModel.sendMessage(text, attachments, messageType, timestamp);
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

    private void sendConnectionRequestDialog(PeerInfo peerInfo) {
        DialogInterface.OnClickListener yesListener =
                (dialog, which) -> {
                    sendConnectionRequest(requireNonNull(peerInfo));
                };
        AlertDialog.Builder builder =
                new AlertDialog.Builder(ConversationActivity.this,
                                        R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.send_connection_request));
        builder.setMessage("Are you sure you want to send a connection request to " + peerInfo.getAlias());
        builder.setPositiveButton(R.string.dialog_yes, yesListener);
        builder.setNegativeButton(R.string.cancel, null);
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
    public void onImageClicked(View view, ConversationItem messageItem, AttachmentItem attachmentItem) {
        Intent intent = new Intent(this, ImageActivity.class);
        String contactName = viewModel.getContactDisplayName().getValue();
        String message = messageItem.getText();
        String uri = attachmentItem.getUri().toString();
        intent.putExtra(CONTACT_NAME, contactName);
        intent.putExtra(ATTACHMENT_URI, uri);
        intent.putExtra(TEXT_MESSAGE, message);
        intent.putExtra(TIMESTAMP, messageItem.getTime());
        startActivity(intent);
    }

    @Override
    public void onFavouriteClicked(View view,
                                   ConversationItem messageItem) {
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

    @Override
    public void onSharedContactClicked(View view, ConversationItem messageItem) {
        SharedContactConversationItem item = (SharedContactConversationItem) messageItem;
        sendConnectionRequestDialog(item.getPeerInfo());
    }

    @Override
    public void onFileClicked(View view, AttachmentItem attachmentItem) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri fileuri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                new File(attachmentItem.getUri().getPath())
        );
        intent.setDataAndType(fileuri, attachmentItem.getMimeType());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


    @Nullable
    @Override
    public String getText(String messageId) {
        String text = textCache.get(messageId);
        if (text == null) loadMessageText(messageId);
        return text;
    }

    @Override
    public void onAttachImage(Intent intent) {
        startActivityForResult(intent, RequestCodes.REQUEST_ATTACH_IMAGE);
    }

    @Override
    public void onAttachCapturedPhoto(Intent intent) {
        if (cameraExists()) {
            if (areCameraRelatedPermissionsGranted()) {
                startActivityForResult(intent, RequestCodes.REQUEST_ATTACH_CAPTURED_PHOTO);
            } else {
                photoCaptureIntent = intent;
                ActivityCompat.requestPermissions(this, CAPTURE_FROM_CAMERA_PERMISSIONS, CAMERA_PERMISSIONS_CODE);
            }
        } else {
            Toast.makeText(
                    this,
                    "No available cameras in your system",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public void onAttachFile(Intent intent) {
        startActivityForResult(intent, RequestCodes.REQUEST_ATTACH_FILE);
    }

    @Override
    public void onTooManyImageAttachments() {
        String format = getResources().getString(
                R.string.messaging_too_many_attachments_toast);
        String warning = String.format(format, MAX_IMAGE_ATTACHMENTS_PER_MESSAGE);
        Toast.makeText(this, warning, LENGTH_SHORT).show();
    }

    public void showError(Exception e) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Internal error");
        builder.setMessage(e.getMessage());
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        e.printStackTrace();
    }

    @Override
    public List<AttachmentItem> getAttachments(String messageId) {
        List<AttachmentItem> attachments =
                attachmentRetriever.cacheGet(messageId);
        if (attachments == null) {
            loadMessageAttachments(messageId);
            return emptyList();
        }
        return attachments;
    }

    void sendConnectionRequest(PeerInfo peerInfo) {
        connectionController.sendConnectionRequest(peerInfo, new UiResultExceptionHandler<Void, Exception>(this) {
            @Override
            public void onResultUi(Void v) {
                Toast.makeText(ConversationActivity.this,
                               "Connection Request to " + peerInfo.getAlias() + " has been sent!",
                               Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onExceptionUi(Exception exception) {
                if (exception instanceof ContactExistsException)
                    Toast.makeText(ConversationActivity.this,
                                   "You are already Connected with " + peerInfo.getAlias(),
                                   Toast.LENGTH_LONG
                    ).show();
                else if (exception instanceof PendingContactExistsException)
                    Toast.makeText(ConversationActivity.this,
                                   "You have already sent connection request to " + peerInfo.getAlias(),
                                   Toast.LENGTH_LONG
                    ).show();
                else if (exception instanceof InvalidActionException)
                    Toast.makeText(ConversationActivity.this,
                                   ((InvalidActionException) exception).getMessage(),
                                   Toast.LENGTH_LONG
                    ).show();
                exception.printStackTrace();
            }
        });
    }

    /**
     * Check if this device has a camera
     */
    private boolean cameraExists() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private boolean areCameraRelatedPermissionsGranted() {
        // Check if we have write permission
        if (ContextCompat.checkSelfPermission(this, CAPTURE_FROM_CAMERA_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, CAPTURE_FROM_CAMERA_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSIONS_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 2 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(photoCaptureIntent, RequestCodes.REQUEST_ATTACH_CAPTURED_PHOTO);
                } else {
                    //TODO
                }
                return;
        }
    }
}
