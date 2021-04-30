package eu.h2020.helios_social.happ.helios.talk.contact;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.recyclerview.widget.LinearLayoutManager;

import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.connection.ConnectionRegistry;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContactAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PendingContactAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContextInvitationAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupInvitationAddedEvent;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactType;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.AddContactActivity;
import eu.h2020.helios_social.happ.helios.talk.fragment.HeliosContextFragment;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import io.github.kobakei.materialfabspeeddial.FabSpeedDial;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_ID;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;
import static java.util.logging.Level.WARNING;

public class ContactListFragment extends HeliosContextFragment
        implements EventListener, FabSpeedDial.OnMenuItemClickListener {

    public static final String TAG = ContactListFragment.class.getName();
    private static final Logger LOG = Logger.getLogger(TAG);

    @Inject
    AndroidNotificationManager notificationManager;
    @Inject
    ContactManager contactManager;
    @Inject
    ConversationManager conversationManager;
    @Inject
    ConnectionRegistry connectionRegistry;
    @Inject
    EventBus eventBus;

    private ContactListAdapter adapter;
    private HeliosTalkRecyclerView list;


    public static ContactListFragment newInstance() {
        Bundle args = new Bundle();
        ContactListFragment fragment = new ContactListFragment();
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
        requireActivity().setTitle(R.string.contact_list_button);

        View contentView = inflater.inflate(R.layout.contact_list_view,
                container, false);

        FabSpeedDial speedDial = contentView.findViewById(R.id.speedDial);
        speedDial.addOnMenuItemClickListener(this);

        BaseContactListAdapter.OnContactClickListener<ContactListItem>
                onContactClickListener =
                (view, item) -> {
                    Intent i = new Intent(getActivity(),
                            ConversationActivity.class);
                    ContactId contactId = item.getContact().getId();
                    i.putExtra(CONTACT_ID, contactId.getId());
                    i.putExtra(GROUP_ID, item.getGroupId());
                    startActivity(i);
                };
        adapter = new ContactListAdapter(requireContext(),
                onContactClickListener);
        list = contentView.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        String currentContext =
                egoNetwork.getCurrentContext().getData().toString()
                        .split("%")[0];
        list.setEmptyImage(R.drawable.ic_no_contacts_illustration);
        if (currentContext.equals("All")) {
            list.setEmptyTitle(R.string.no_contacts);
            list.setEmptyText(getString(R.string.no_contacts_details));
        } else {
            list.setEmptyTitle(R.string.no_contacts);
            list.setEmptyText(R.string.no_contacts_in_context_details);
        }
        list.setEmptyAction(getString(R.string.no_contacts_action));

        return contentView;
    }

    @Override
    public void onMenuItemClick(FloatingActionButton fab, @Nullable TextView v,
                                int itemId) {
        switch (itemId) {
            case R.id.action_add_contact_remotely:
                startActivity(
                        new Intent(getContext(), AddContactActivity.class));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.addListener(this);
        loadContacts();
        list.startPeriodicUpdate();
    }

    @SuppressLint("RestrictedApi")
    public void onResume() {
        super.onResume();
        actionBar.invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.removeListener(this);
        adapter.clear();
        list.showProgressBar();
        list.stopPeriodicUpdate();
    }

    private void loadContacts() {
        int revision = adapter.getRevision();
        listener.runOnDbThread(() -> {
            try {
                long start = now();
                List<ContactListItem> contacts = new ArrayList<>();
                String currentContext =
                        egoNetwork.getCurrentContext().getData().toString();
                Collection<ContactId> contextMembers = null;
				/*if (!currentContext.equals("All@All"))
					contextMembers =
							contextManager.getMembers(
									new Context.Builder(currentContext)
											.build());*/

                String contextId =
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1];
                List<ContactId> onlineContacts = connectionRegistry.getConnectedContacts();
                for (Contact c : contactManager.getContacts(contextId)) {
                    ContactId id = c.getId();
                    Group g =
                            conversationManager.getContactGroup(id, contextId);
                    contacts.add(
                            new ContactListItem(c, g.getId(), onlineContacts.contains(c.getId())));
					/*try {
						ContactId id = c.getId();

						/*if (contextMembers == null ||
								contextMembers.contains(id)) {
							GroupCount count =
									conversationManager.getGroupCount(id);
							boolean connected =
									connectionRegistry.isConnected(c.getId());
							contacts.add(
									new ContactListItem(c, connected, count));
						}
					} catch (NoSuchContactException e) {
						// Continue
					}*/
                }
                logDuration(LOG, "Full load", start);
                displayContacts(revision, contacts);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    private void displayContacts(int revision, List<ContactListItem> contacts) {
        runOnUiThreadUnlessDestroyed(() -> {
            if (revision == adapter.getRevision()) {
                adapter.incrementRevision();
                if (contacts.isEmpty()) list.showData();
                else adapter.replaceAll(contacts);
            } else {
                LOG.info("Concurrent update, reloading");
                loadContacts();
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void eventOccurred(Event e) {
        if (e instanceof ContactAddedEvent) {
            LOG.info("Contact added, reloading");
            loadContacts();
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

}
