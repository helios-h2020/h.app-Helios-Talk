package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.apache.commons.lang3.NotImplementedException;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.activity.RequestCodes;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentRetriever;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.conversation.ImageActivity;
import eu.h2020.helios_social.happ.helios.talk.group.GroupConversationAdapter;
import eu.h2020.helios_social.happ.helios.talk.group.GroupConversationVisitor;
import eu.h2020.helios_social.happ.helios.talk.group.GroupMessageItem;
import eu.h2020.helios_social.happ.helios.talk.privategroup.membership.GroupMembershipListActivity;
import eu.h2020.helios_social.happ.helios.talk.shared.controllers.ConnectionController;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.happ.helios.talk.view.FileAttachmentPreview;
import eu.h2020.helios_social.happ.helios.talk.view.ImagePreview;
import eu.h2020.helios_social.happ.helios.talk.view.TextAttachmentController;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.GeneralContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.LocationContextProxy;
import eu.h2020.helios_social.modules.groupcommunications_utils.Pair;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.ContactExistsException;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.InvalidActionException;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.NoSuchGroupException;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.PendingContactExistsException;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationItem;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.GroupInviteActivity;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkSnackbarBuilder;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.happ.helios.talk.view.TextInputView;
import eu.h2020.helios_social.happ.helios.talk.view.TextSendController;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupMessageReceivedEvent;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.ATTACHMENT_URI;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_NAME;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.TEXT_MESSAGE;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.TIMESTAMP;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingConstants.MAX_IMAGE_ATTACHMENTS_PER_MESSAGE;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils.isNullOrEmpty;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingConstants.MAX_MESSAGE_TEXT_LENGTH;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class PrivateGroupConversationActivity extends HeliosTalkActivity
        implements EventListener, PrivateGroupListener,
        GroupConversationVisitor.TextCache, GroupConversationVisitor.AttachmentCache,
        TextSendController.SendListener, TextAttachmentController.AttachmentListener {
    private static final Logger LOG =
            Logger.getLogger(PrivateGroupConversationActivity.class.getName());
    public static final String GROUP_NAME = "helios.talk.GROUP_NAME";

    @Inject
    AndroidNotificationManager notificationManager;
    @Inject
    ContextualEgoNetwork egoNetwork;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    EventBus eventBus;
    @Inject
    ConversationManager conversationManager;
    @Inject
    ConnectionController connectionController;

    private PrivateGroupConversationViewModel viewModel;
    private AttachmentRetriever attachmentRetriever;
    private GroupConversationVisitor visitor;
    private GroupConversationAdapter adapter;
    private HeliosTalkRecyclerView list;
    private LinearLayoutManager layoutManager;
    private TextInputView textInput;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private TextAttachmentController sendController;
    private String groupId;
    private String groupName;
    private String IS_NEW="isNew";

    private final Map<String, String> textCache = new ConcurrentHashMap<>();
    @Nullable
    private Parcelable layoutManagerState;
    private final Observer<PrivateGroup> privateGroupObserver =
            privateGroup -> {
                requireNonNull(privateGroup);
                loadMessages();
            };

    @Nullable
    private Boolean isOwner = null;
    private boolean isDissolved = false;
    private MenuItem inviteMenuItem, leaveMenuItem,
            dissolveMenuItem;
    private Boolean isNewGroup;

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        Intent i = getIntent();
        groupId = i.getStringExtra(GROUP_ID);
        if (groupId == null)
            throw new IllegalStateException("No GroupId in intent.");
        isNewGroup = i.getBooleanExtra(IS_NEW,false);
        groupName = i.getStringExtra(GROUP_NAME);
        LOG.info("group id: " + groupId + " group name:" + groupName);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(PrivateGroupConversationViewModel.class);
        attachmentRetriever = viewModel.getAttachmentRetriever();

        viewModel.isGroupDisolved().observe(this, deleted -> {
            requireNonNull(deleted);
            if (deleted) finish();
        });
        viewModel.getAddedGroupMessage().observeEvent(this,
                                                      this::onAddedGroupMessage);

        setContentView(R.layout.activity_group_conversation);

        toolbar = requireNonNull(setUpCustomToolbar(true));
        toolbarTitle = toolbar.findViewById(R.id.groupName);
        viewModel.getPrivateGroup().observe(this, privateGroup -> {
            requireNonNull(privateGroup);
            toolbarTitle.setText(privateGroup.getName());
        });

        visitor = new GroupConversationVisitor(this, this, this);
        adapter = new GroupConversationAdapter(this, this);
        layoutManager = new LinearLayoutManager(this);
        list = findViewById(R.id.conversationView);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        list.setEmptyText(getString(R.string.no_private_messages));
        PrivateGroupConversationScrollListener scrollListener =
                new PrivateGroupConversationScrollListener(adapter, viewModel);
        list.getRecyclerView().addOnScrollListener(scrollListener);
        list.getRecyclerView().setItemAnimator(new SlideInUpAnimator());

        textInput = findViewById(R.id.text_input_container);
        ImagePreview imagePreview = findViewById(R.id.imagePreview);
        FileAttachmentPreview fileAttachmentPreview = findViewById(R.id.fileAttachmentPreview);
        sendController = new TextAttachmentController(this, textInput, imagePreview,
                                                      fileAttachmentPreview, this);
        sendController.setImagesSupported();

        textInput.setSendController(sendController);
        textInput.setMaxTextLength(MAX_MESSAGE_TEXT_LENGTH);
        textInput.setReady(true);
        textInput.setOnKeyboardShownListener(this::scrollToBottom);

        registerForContextMenu(list);

        if (isNewGroup){
            Intent i3 = new Intent(this, GroupInviteActivity.class);
            i3.putExtra(GROUP_ID, groupId);
            startActivityForResult(i3, RequestCodes.REQUEST_GROUP_INVITE);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;
        try {
            position = adapter.getPosition();
            GroupMessageItem messageItem = adapter.getItemAt(position);
            if (messageItem != null) {
                connectionController.sendConnectionRequest(messageItem.getPeerInfo(), new UiResultExceptionHandler<Void, Exception>(this) {
                    @Override
                    public void onResultUi(Void v) {
                        Toast.makeText(PrivateGroupConversationActivity.this,
                                       "Connection Request to " + messageItem.getPeerInfo().getAlias() + " has been sent!",
                                       Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onExceptionUi(Exception exception) {
                        if (exception instanceof ContactExistsException)
                            Toast.makeText(PrivateGroupConversationActivity.this,
                                           "You are already Connected with " + messageItem.getPeerInfo().getAlias(),
                                           Toast.LENGTH_LONG
                            ).show();
                        else if (exception instanceof PendingContactExistsException)
                            Toast.makeText(PrivateGroupConversationActivity.this,
                                           "You have already sent connection request to " + messageItem.getPeerInfo().getAlias(),
                                           Toast.LENGTH_LONG
                            ).show();
                        else if (exception instanceof InvalidActionException)
                            Toast.makeText(PrivateGroupConversationActivity.this,
                                           ((InvalidActionException) exception).getMessage(),
                                           Toast.LENGTH_LONG
                            ).show();
                        exception.printStackTrace();
                    }
                });
            }


        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.removeListener(this);
        notificationManager.unblockNotification(groupId);
        viewModel.getPrivateGroup().removeObserver(privateGroupObserver);
        list.stopPeriodicUpdate();
        LOG.info("Group Conversation Activity Stopped for group: " + groupName);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (layoutManager != null) {
            layoutManagerState = layoutManager.onSaveInstanceState();
            outState.putParcelable("layoutManager", layoutManagerState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        layoutManagerState = savedInstanceState.getParcelable("layoutManager");
    }

    private void scrollToBottom() {
        int items = adapter.getItemCount();
        if (items > 0) list.scrollToPosition(items - 1);
    }

    private void customScrollToBottom(int y){
            list.getRecyclerView().smoothScrollBy(0, Math.abs(y));
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.addListener(this);
        notificationManager.blockNotification(groupId);
        notificationManager.clearGroupMessageNotification(groupId);
        viewModel.getPrivateGroup().observe(this, privateGroupObserver);
        list.startPeriodicUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (signedIn()) {
            viewModel.setGroupName(groupName);
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
    public void onBackPressed() {
        Intent intent = new Intent(this, NavDrawerActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_actions, menu);

        //revealMenuItem = menu.findItem(R.id.action_group_reveal);
        inviteMenuItem = menu.findItem(R.id.action_group_invite);
        leaveMenuItem = menu.findItem(R.id.action_group_leave);
        dissolveMenuItem = menu.findItem(R.id.action_group_dissolve);

        // all role-dependent items are invisible until we know our role
        //revealMenuItem.setVisible(false);
        inviteMenuItem.setEnabled(true);
        leaveMenuItem.setVisible(false);
        dissolveMenuItem.setVisible(false);

        // show items based on role
        showMenuItems();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_group_member_list:
				Intent i1 = new Intent(this, GroupMembershipListActivity.class);
				i1.putExtra(GROUP_ID, groupId);
				startActivity(i1);
				return true;
			/*case R.id.action_group_reveal:
				if (isOwner == null || isOwner)
					throw new IllegalStateException();
				Intent i2 = new Intent(this, RevealContactsActivity.class);
				i2.putExtra(GROUP_ID, groupId.getBytes());
				startActivity(i2);
				return true;*/
            case R.id.action_group_invite:
				/*if (isOwner == null || !isOwner)
					throw new IllegalStateException();*/
                Intent i3 = new Intent(this, GroupInviteActivity.class);
                i3.putExtra(GROUP_ID, groupId);
                startActivityForResult(i3, RequestCodes.REQUEST_GROUP_INVITE);
                return true;
            case R.id.action_group_leave:
                if (isOwner == null || isOwner)
                    throw new IllegalStateException();
                showLeaveGroupDialog();
                return true;
            case R.id.action_group_dissolve:
                if (isOwner == null || !isOwner)
                    throw new IllegalStateException();
                showDissolveGroupDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == RequestCodes.REQUEST_GROUP_INVITE &&
                result == RESULT_OK) {
            displaySnackbar(R.string.forums_invitation_sent);
        } else if (request == RequestCodes.REQUEST_ATTACH_IMAGE &&
                result == RESULT_OK) {
            sendController.onAttachmentsReceived(data, Message.Type.IMAGES);
        } else if (request == RequestCodes.REQUEST_ATTACH_FILE && result == RESULT_OK) {
            sendController.onAttachmentsReceived(data, Message.Type.FILE_ATTACHMENT);
        } else super.onActivityResult(request, result, data);
    }

    protected void displaySnackbar(@StringRes int stringId) {
        new HeliosTalkSnackbarBuilder()
                .make(list, stringId, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void loadMessages() {
        int revision = adapter.getRevision();
        runOnDbThread(() -> {
            try {
                long start = now();
                Collection<GroupMessageHeader> headers =
                        conversationManager.getGroupMessageHeaders(groupId);
                logDuration(LOG, "Loading messages", start);
                // Sort headers by timestamp in *descending* order
                List<GroupMessageHeader> sorted =
                        new ArrayList<>(headers);
                sort(sorted, (a, b) ->
                        Long.compare(b.getTimestamp(), a.getTimestamp()));
                if (!sorted.isEmpty()) {
                    // If the latest header is a private message, eagerly load
                    // its size so we can set the scroll position correctly
                    GroupMessageHeader latest = sorted.get(0);
                    eagerlyLoadMessageSize(latest);
                }
                displayMessages(revision, sorted);
            } catch (NoSuchGroupException e) {
                finishOnUiThread();
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            } catch (FormatException e) {
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

    private void eagerlyLoadMessageSize(GroupMessageHeader h) {
        try {
            String id = h.getMessageId();
            String text = textCache.get(id);
            if ((text == null || text.equals("")) && h.hasText()) {
                LOG.info("Eagerly loading text for latest message");
                text = conversationManager.getMessageText(id);
                textCache.put(id, requireNonNull(text));
            }
        } catch (DbException e) {
            logException(LOG, WARNING, e);
        }
    }

    private void displayMessages(int revision,
                                 Collection<GroupMessageHeader> headers) {
        runOnUiThreadUnlessDestroyed(() -> {
            if (revision == adapter.getRevision()) {
                adapter.incrementRevision();
                textInput.setReady(true);

                List<GroupMessageItem> items = createItems(headers);
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
            Pair<Integer, GroupMessageItem> pair =
                    adapter.getMessageItem(messageId);
            if (pair != null) {
                boolean scroll = shouldScrollWhenUpdatingMessage();
                pair.getSecond().setAttachmentList(items);
                adapter.notifyItemChanged(pair.getFirst());
                if (scroll) scrollToBottom();
                int overallXScroldl = list.getRecyclerView().computeVerticalScrollOffset();
                if (overallXScroldl >0)
                    customScrollToBottom(overallXScroldl);
            }
        });
    }

    private void setGroupEnabled(boolean enabled) {
        isDissolved = !enabled;
        sendController.setReady(enabled);
        list.getRecyclerView().setAlpha(enabled ? 1f : 0.5f);

        if (!enabled) {
            textInput.setVisibility(GONE);
            if (textInput.isKeyboardOpen()) textInput.hideSoftKeyboard();
        } else {
            textInput.setVisibility(VISIBLE);
        }
    }

    private void showMenuItems() {
        // we need to have the menu items and know if we are the creator
        if (leaveMenuItem == null || isOwner == null) return;
        //revealMenuItem.setVisible(!isOwner);
        inviteMenuItem.setVisible(isOwner);
        leaveMenuItem.setVisible(!isOwner);
        dissolveMenuItem.setVisible(isOwner);
    }

    private void showLeaveGroupDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.groups_leave_dialog_title));
        builder.setMessage(getString(R.string.groups_leave_dialog_message));
        //TODO functionality
        builder.setNegativeButton(R.string.dialog_button_leave, null);
        builder.setPositiveButton(R.string.cancel, null);
        builder.show();
    }

    private void showDissolveGroupDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.groups_dissolve_dialog_title));
        builder.setMessage(getString(R.string.groups_dissolve_dialog_message));
        //TODO add functionality
        builder.setNegativeButton(R.string.groups_dissolve_button, null);
        builder.setPositiveButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void onFavouriteClicked(View view, ConversationItem messageItem) {
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
        throw new NotImplementedException("This functionality is not implemented for forum conversations");
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

    @Override
    public void onGroupDissolved() {
        setGroupEnabled(false);
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.groups_dissolved_dialog_title));
        builder.setMessage(getString(R.string.groups_dissolved_dialog_message));
        builder.setNeutralButton(R.string.ok, null);
        builder.show();
    }

    /**
     * Creates GroupMessageItems from headers loaded from the database.
     * <p>
     * Attention: Call this only after groupName has been initialized.
     */
    private List<GroupMessageItem> createItems(
            Collection<GroupMessageHeader> headers) {
        List<GroupMessageItem> items = new ArrayList<>(headers.size());
        for (GroupMessageHeader h : headers) {
            GroupMessageItem item = h.accept(visitor);
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
            Pair<Integer, GroupMessageItem> pair =
                    adapter.getMessageItem(messageId);
            if (pair != null) {
                boolean scroll = shouldScrollWhenUpdatingMessage();

                pair.getSecond().setText(text);

                adapter.notifyItemChanged(pair.getFirst());
                if (scroll)
                    scrollToBottom();
                int overallXScroldl = list.getRecyclerView().computeVerticalScrollOffset();
                if (overallXScroldl >0)
                    customScrollToBottom(overallXScroldl);

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
        if (e instanceof GroupMessageReceivedEvent) {
            GroupMessageReceivedEvent p =
                    (GroupMessageReceivedEvent) e;
            if (p.getMessageHeader().getGroupId().equals(groupId)) {
                LOG.info("Message received, adding");
                onNewConversationMessage(p.getMessageHeader());
            }
        }
    }

    @UiThread
    private void addConversationItem(GroupMessageItem item) {
        adapter.incrementRevision();
        adapter.add(item);
        // When adding a new message, scroll to the bottom if the conversation
        // is visible, even if we're not currently at the bottom
        if (getLifecycle().getCurrentState().isAtLeast(STARTED))
            scrollToBottom();
    }


    @UiThread
    private void onNewConversationMessage(GroupMessageHeader h) {
        // visitor also loads message text (if existing)
        addConversationItem(h.accept(visitor));
    }

    @UiThread
    private void markMessages(Collection<String> messageIds, boolean sent,
                              boolean seen) {
        adapter.incrementRevision();
        Set<String> messages = new HashSet<>(messageIds);
        SparseArray<GroupMessageItem> list = adapter.getOutgoingMessages();
        for (int i = 0; i < list.size(); i++) {
            ConversationItem item = list.valueAt(i);
            if (messages.contains(item.getId())) {
                item.setSent(sent);
                item.setSeen(seen);
                adapter.notifyItemChanged(list.keyAt(i));
            }
        }
    }

    @Override
    public void onSendClick(String text, List<AttachmentItem> attachments, Message.Type messageType) {
        if (isNullOrEmpty(text) && attachments.isEmpty())
            throw new AssertionError();
        long timestamp = System.currentTimeMillis();
        timestamp = Math.max(timestamp, getMinTimestampForNewMessage());

        viewModel.sendMessage(text, attachments, timestamp, messageType);
        textInput.clearText();
    }

    private long getMinTimestampForNewMessage() {
        // Don't use an earlier timestamp than the newest message
        ConversationItem item = adapter.getLastItem();
        return item == null ? 0 : item.getTime() + 1;
    }

    @Nullable
    @Override
    public String getText(String messageId) {
        String text = textCache.get(messageId);
        if (text == null || text.equals("")) loadMessageText(messageId);
        return text;

    }

    private void onAddedGroupMessage(@Nullable
                                             GroupMessageHeader h) {
        if (h == null) return;
        addConversationItem(h.accept(visitor));
    }

    @Override
    public void onImageClicked(View view, ConversationItem messageItem, AttachmentItem attachmentItem) {
        GroupMessageItem item = (GroupMessageItem) messageItem;
        Intent intent = new Intent(this, ImageActivity.class);
        String peerName;
        if (item.getPeerInfo().getFunnyName() == null) {
            peerName = item.getPeerInfo().getAlias();
        } else {
            peerName = item.getPeerInfo().getFunnyName();
        }
        String message = item.getText();
        String uri = attachmentItem.getUri().toString();
        intent.putExtra(CONTACT_NAME, peerName);
        intent.putExtra(ATTACHMENT_URI, uri);
        intent.putExtra(TEXT_MESSAGE, message);
        intent.putExtra(TIMESTAMP, messageItem.getTime());
        startActivity(intent);
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

    @Override
    public void onAttachImage(Intent intent) {
        startActivityForResult(intent, RequestCodes.REQUEST_ATTACH_IMAGE);
    }

    @Override
    public void onAttachCapturedPhoto(Intent intent) {

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
}
