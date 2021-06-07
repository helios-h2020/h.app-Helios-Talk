package eu.h2020.helios_social.happ.helios.talk.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.annotation.UiThread;
import androidx.recyclerview.widget.LinearLayoutManager;

import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.search.SearchActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.connection.ConnectionRegistry;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupMessageReceivedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PendingContactAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContextInvitationAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupInvitationAddedEvent;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactListFragment;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactListItem;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.CreateForumActivity;
import eu.h2020.helios_social.happ.helios.talk.fragment.HeliosContextFragment;
import eu.h2020.helios_social.happ.helios.talk.privategroup.GroupItem;
import eu.h2020.helios_social.happ.helios.talk.forum.ForumItem;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateGroupActivity;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactType;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupCount;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.mining.MiningManager;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PrivateMessageReceivedEvent;
import io.github.kobakei.materialfabspeeddial.FabSpeedDial;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static java.util.Collections.sort;
import static java.util.logging.Level.WARNING;

public class ChatListFragment extends HeliosContextFragment
        implements FabSpeedDial.OnMenuItemClickListener, EventListener {
    public static final String TAG = ChatListFragment.class.getName();
    private static final Logger LOG = Logger.getLogger(TAG);

    @Inject
    AndroidNotificationManager notificationManager;
    @Inject
    volatile ConversationManager conversationManager;
    @Inject
    volatile MiningManager miningManager;
    @Inject
    EventBus eventBus;
    @Inject
    volatile ConnectionRegistry connectionRegistry;

    private ChatListAdapter adapter;
    private HeliosTalkRecyclerView list;
    private FabSpeedDial speedDial;

    public static ChatListFragment newInstance() {
        Bundle args = new Bundle();
        ChatListFragment fragment = new ChatListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getUniqueTag() {
        return TAG;
    }

    @Override
    public void injectFragment(ActivityComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View contentView = inflater.inflate(R.layout.fragment_chat_list,
                                            container, false);
        speedDial = contentView.findViewById(R.id.speedDial);
        speedDial.addOnMenuItemClickListener(this);

        adapter = new ChatListAdapter(requireContext());
        list = contentView.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        list.setEmptyImage(R.drawable.ic_no_chats_illustration);
        String currentContext =
                egoNetwork.getCurrentContext().getData().toString()
                        .split("%")[0];

        list.setEmptyTitle(R.string.no_conversations);
        if (currentContext.equals("All"))
            list.setEmptyText(R.string.no_conversations_details);
        else
            list.setEmptyText(R.string.no_conversations_in_context_details);
        list.setEmptyAction(R.string.no_conversation_action);

        ImageView searchIcon = contentView.findViewById(R.id.searchIcon);
        searchIcon.setOnClickListener(l -> {
            Intent searchActivity = new Intent(getContext(),
                                               SearchActivity.class);
            startActivity(searchActivity);
        });

        EditText searchView = contentView.findViewById(R.id.searchView);

        searchView.setOnClickListener(l -> {
            Intent searchActivity = new Intent(getContext(),
                                               SearchActivity.class);
            startActivity(searchActivity);
        });

        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.addListener(this);
        list.startPeriodicUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChats();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.removeListener(this);
        adapter.clear();
        list.showProgressBar();
        list.stopPeriodicUpdate();
    }

    private void loadChats() {
        int revision = adapter.getRevision();
        listener.runOnDbThread(() -> {
            try {
                long start = now();
                List<ChatItem> chats = new ArrayList<>();

                String currentContext =
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1];

                HashMap<Node, Double> favourites = miningManager.getNextInteractionRecommendations(
                        egoNetwork.getCurrentContext().getData().toString());

                List<ContactId> onlineContacts = connectionRegistry.getConnectedContacts();

                Set<ContactId> favs = new HashSet();
                if (favourites.size() > 0) {
                    HeaderItem headerItem = new HeaderItem("recommended");
                    headerItem.setWeight(1000);
                    chats.add(headerItem);
                    for (Map.Entry<Node, Double> fav : favourites.entrySet()) {
                        ContactId id = new ContactId(fav.getKey().getId());
                        Contact c = contactManager.getContact(id);
                        Group group = conversationManager
                                .getContactGroup(c.getId(), currentContext);

                        GroupCount count =
                                conversationManager.getGroupCount(group.getId());
                        boolean isConnected = onlineContacts.contains(c.getId());
                        ContactListItem item =
                                new ContactListItem(c, group.getId(), isConnected, count);
                        item.setFavourite(true);
                        item.setLastMessageText(getLastMessage(group.getId(),
                                                               GroupType.PrivateConversation));
                        item.setWeight((int) (fav.getValue() * 1000));

                        if (count.getMsgCount() > 0) {
                            chats.add(item);
                            favs.add(id);
                        }
                        adapter.setChatsPosition(favourites.size() + 1);
                    }
                }

                for (Contact c : contactManager.getContacts(currentContext)) {
                    if (favs.contains(c.getId())) continue;
                    Group group = conversationManager
                            .getContactGroup(c.getId(), currentContext);
                    GroupCount count =
                            conversationManager
                                    .getGroupCount(group.getId());
                    boolean isConnected = onlineContacts.contains(c.getId());
                    ContactListItem item =
                            new ContactListItem(c, group.getId(), isConnected,
                                                count);

                    item.setLastMessageText(getLastMessage(group.getId(),
                                                           GroupType.PrivateConversation));
                    if (count.getMsgCount() > 0)
                        chats.add(item);
                }

                Collection<Group> groups =
                        groupManager.getGroups(currentContext);
                for (Group group : groups) {
                    GroupCount count =
                            conversationManager
                                    .getGroupCount(group.getId());
                    if (group.getGroupType().equals(GroupType.PrivateGroup)) {
                        GroupItem item =
                                new GroupItem((PrivateGroup) group, count, false);

                        item.setLastMessageText(getLastMessage(group.getId(),
                                                               GroupType.PrivateGroup));
                        chats.add(item);
                    } else {
                        ForumItem item =
                                new ForumItem((Forum) group, count);
                        System.out.println();

                        item.setLastMessageText(getLastMessage(group.getId(),
                                                               group.getGroupType()));
                        chats.add(item);
                    }
                }
                if (chats.size() > 0) {
                    HeaderItem headerItem = new HeaderItem("chats");
                    chats.add(headerItem);
                }
                logDuration(LOG, "Chats Full load", start);
                displayChats(revision, chats);
            } catch (DbException | FormatException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    private void displayChats(int revision, List<ChatItem> chats) {
        runOnUiThreadUnlessDestroyed(() -> {
            if (revision == adapter.getRevision()) {
                adapter.incrementRevision();
                if (chats.isEmpty()) {
                    list.showData();
                } else {
                    adapter.replaceAll(chats);
                }
            } else {
                loadChats();
            }
        });
    }

    private String getLastMessage(String groupId, GroupType groupType)
            throws DbException, FormatException {
        String lastMessage = "No messages yet";
        Collection<MessageHeader> headers = new ArrayList<>();
        if (groupType.equals(GroupType.PrivateConversation)) {
            headers.addAll(conversationManager.getMessageHeaders(groupId));
        } else {
            headers.addAll(conversationManager.getGroupMessageHeaders(groupId));
        }
        // Sort headers by timestamp in *descending* order
        List<MessageHeader> sorted =
                new ArrayList<>(headers);
        sort(sorted, (a, b) ->
                Long.compare(b.getTimestamp(), a.getTimestamp()));
        if (!sorted.isEmpty()) {
            // If the latest header is a private message, eagerly load
            // its size so we can set the scroll position correctly
            MessageHeader latest = sorted.get(0);
            String username = "you: ";
            if (latest.isIncoming()) {
                username = "";
            }
            if (latest instanceof GroupMessageHeader) {
                PeerInfo peerInfo = ((GroupMessageHeader) latest).getPeerInfo();
                String visibleAlias = peerInfo.getAlias();
                if (visibleAlias == null) visibleAlias = peerInfo.getFunnyName();
                lastMessage =
                        visibleAlias + ": " + conversationManager
                                .getMessageText(latest.getMessageId());
            } else {
                if (((MessageHeader) latest).getMessageType().equals(
                        Message.Type.VIDEOCALL) && latest.isIncoming()) {
                    lastMessage = username +
                            "Video Call Invite Received";
                } else if (((MessageHeader) latest).getMessageType().equals(
                        Message.Type.VIDEOCALL) && !latest.isIncoming()) {
                    lastMessage = username +
                            "Sent a Video Call";
                } else if (((MessageHeader) latest).getMessageType().equals(
                        Message.Type.CONTACT)) {
                    lastMessage = username + "have shared a contact";
                } else {
                    lastMessage = username +
                            conversationManager
                                    .getMessageText(latest.getMessageId());
                }
            }
        }
        return lastMessage;
    }

    @Override
    public void onMenuItemClick(FloatingActionButton floatingActionButton,
                                @androidx.annotation.Nullable TextView textView, int i) {
        switch (i) {
            case R.id.action_new_conversation:
                showNextFragment(ContactListFragment.newInstance());
                return;
            case R.id.action_new_group:
                Intent intent = new Intent(getActivity(),
                                           CreateGroupActivity.class);
                startActivity(intent);
                return;
            case R.id.action_new_forum:
                Intent createForumIntent = new Intent(getActivity(),
                                                      CreateForumActivity.class);
                startActivity(createForumIntent);
                return;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void eventOccurred(Event e) {
        if (e instanceof GroupMessageReceivedEvent) {
            LOG.info("Group message received, updating item");
            GroupMessageReceivedEvent p =
                    (GroupMessageReceivedEvent) e;
            GroupMessageHeader h = p.getMessageHeader();
            updateItem(h.getGroupId(), h);
        } else if (e instanceof PrivateMessageReceivedEvent) {
            LOG.info("Conversation message received, updating item");
            PrivateMessageReceivedEvent p =
                    (PrivateMessageReceivedEvent) e;
            MessageHeader h = p.getMessageHeader();
            updateItem(p.getContactId(), h);
        } else if (e instanceof ContextInvitationAddedEvent) {
            if (((ContextInvitationAddedEvent) e).getInvite().isIncoming())
                actionBar.invalidateOptionsMenu();
        } else if (e instanceof GroupInvitationAddedEvent) {
            if (((GroupInvitationAddedEvent) e).getInvite().isIncoming())
                actionBar.invalidateOptionsMenu();
        } else if (e instanceof PendingContactAddedEvent) {
            if (((PendingContactAddedEvent) e).getPendingContact().getPendingContactType().equals(PendingContactType.INCOMING))
                actionBar.invalidateOptionsMenu();
        }
    }

    @UiThread
    private void updateItem(ContactId c, MessageHeader h) {
        adapter.incrementRevision();
        int position = adapter.findItemPosition(h.getGroupId());
        if (position < 0) {
            loadChats();
            return;
        }
        ChatItem item = adapter.getItemAt(position);
        try {
            String username = "you: ";
            if (h.isIncoming()) {
                username = "";
            }
            item.setLastMessageText(username + conversationManager
                    .getMessageText(h.getMessageId()));
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (item != null) {
            item.addMessage(h);
            adapter.updateItemAt(position, item);
        }
    }

    @UiThread
    private void updateItem(String groupId, GroupMessageHeader h) {
        adapter.incrementRevision();
        int position = adapter.findItemPosition(h.getGroupId());
        if (position < 0) {
            loadChats();
            return;
        }
        ChatItem item = adapter.getItemAt(position);
        try {
            String username = h.getPeerInfo().getFunnyName();
            if (h.getPeerInfo().getAlias() != null) {
                username = h.getPeerInfo().getAlias();
            }
            item.setLastMessageText(username + ": " + conversationManager
                    .getMessageText(h.getMessageId()));
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (item != null) {
            item.addMessage(h);
            adapter.updateItemAt(position, item);
        }
    }
}
