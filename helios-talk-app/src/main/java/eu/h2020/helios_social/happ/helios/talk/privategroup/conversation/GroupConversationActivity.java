package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

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

import com.google.android.material.snackbar.Snackbar;

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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.activity.RequestCodes;
import eu.h2020.helios_social.happ.helios.talk.api.Pair;
import eu.h2020.helios_social.happ.helios.talk.api.db.NoSuchGroupException;
import eu.h2020.helios_social.happ.helios.talk.api.event.Event;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventListener;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;
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
import eu.h2020.helios_social.modules.groupcommunications.messaging.event.GroupMessageReceivedEvent;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logDuration;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logException;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.now;
import static eu.h2020.helios_social.happ.helios.talk.api.util.StringUtils.isNullOrEmpty;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingConstants.MAX_MESSAGE_TEXT_LENGTH;
import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class GroupConversationActivity extends HeliosTalkActivity
		implements EventListener, GroupListener,
		GroupConversationVisitor.TextCache, TextSendController.SendListener {
	private static final Logger LOG =
			Logger.getLogger(GroupConversationActivity.class.getName());
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

	private GroupConversationViewModel viewModel;
	private GroupConversationVisitor visitor;
	private GroupConversationAdapter adapter;
	private HeliosTalkRecyclerView list;
	private LinearLayoutManager layoutManager;
	private TextInputView textInput;
	private Toolbar toolbar;
	private TextView toolbarTitle;
	private TextSendController sendController;
	private String groupId;
	private String groupName;

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
	private MenuItem /*revealMenuItem,*/ inviteMenuItem, leaveMenuItem,
			dissolveMenuItem;

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
		groupName = i.getStringExtra(GROUP_NAME);
		LOG.info("group id: " + groupId + " group name:" + groupName);

		viewModel = ViewModelProviders.of(this, viewModelFactory)
				.get(GroupConversationViewModel.class);

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

		textInput = findViewById(R.id.text_input_container);
		sendController = new TextSendController(textInput, this, false);
		textInput.setSendController(sendController);
		textInput.setMaxTextLength(MAX_MESSAGE_TEXT_LENGTH);
		textInput.setReady(true);

		visitor = new GroupConversationVisitor(this, this);
		adapter = new GroupConversationAdapter(this, this);
		list = findViewById(R.id.conversationView);
		layoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(layoutManager);
		list.setAdapter(adapter);
		list.setEmptyText(getString(R.string.no_private_messages));
		GroupConversationScrollListener scrollListener =
				new GroupConversationScrollListener(adapter, viewModel);
		list.getRecyclerView().addOnScrollListener(scrollListener);


	}

	@Override
	public void onStop() {
		super.onStop();
		eventBus.removeListener(this);
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

	@Override
	public void onStart() {
		super.onStart();
		eventBus.addListener(this);
		viewModel.getPrivateGroup().observe(this, privateGroupObserver);
		list.startPeriodicUpdate();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (signedIn()) {
			viewModel.setGroupName(groupName);
			viewModel.setGroupId(groupId);
			viewModel.setContextId(
					egoNetwork.getCurrentContext().getData().toString()
							.split("%")[1]);
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
				/*Intent i1 = new Intent(this, GroupMemberListActivity.class);
				i1.putExtra(GROUP_ID, groupId.getBytes());
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
			displaySnackbar(R.string.groups_invitation_sent);
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

	private void eagerlyLoadMessageSize(GroupMessageHeader h) {
		try {
			String id = h.getMessageId();
			// If the message has text, load it
			//if (h.hasText()) {
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
	public void onFavouriteClicked(View view, GroupMessageItem messageItem) {
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
		/*if (e instanceof AttachmentReceivedEvent) {
			AttachmentReceivedEvent a = (AttachmentReceivedEvent) e;
			if (a.getContactId().equals(contactId)) {
				LOG.info("Attachment received");
				onAttachmentReceived(a.getMessageId());
				//addInteraction(InteractionType.RECEIVED);
			}
		}
		if (e instanceof GroupRemovedEvent) {
			GroupRemovedEvent g = (GroupRemovedEvent) e;
			if (g.getGroup().getId().equals(groupId)) {
				LOG.info("Group has been removed");
				supportFinishAfterTransition();
			}
		} else */if (e instanceof GroupMessageReceivedEvent) {
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
	public void onSendClick(String text) {
		if (isNullOrEmpty(text))
			throw new AssertionError();
		long timestamp = System.currentTimeMillis();
		timestamp = Math.max(timestamp, getMinTimestampForNewMessage());
		viewModel.sendMessage(text, /*attachmentHeaders, */timestamp);
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
}
