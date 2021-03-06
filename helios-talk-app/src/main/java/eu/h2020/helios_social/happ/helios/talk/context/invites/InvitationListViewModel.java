package eu.h2020.helios_social.happ.helios.talk.context.invites;

import android.app.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import eu.h2020.helios_social.modules.groupcommunications.api.forum.sharing.ForumAccessRequest;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContextInvitationAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContextInvitationRemovedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupAccessRequestAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupAccessRequestRemovedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.RemovePendingContextEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupInvitationAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.ContextInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.SharingContextManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.GroupInvitationType;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.SharingGroupManager;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

@NotNullByDefault
public class InvitationListViewModel extends AndroidViewModel
        implements EventListener {

    private final Logger LOG =
            getLogger(
                    InvitationListViewModel.class.getName());

    @DatabaseExecutor
    private final Executor dbExecutor;
    private final ContactManager contactManager;
    private final ContextManager contextManager;
    private final SharingContextManager sharingContextManager;
    private final EventBus eventBus;
    private final SharingGroupManager sharingGroupManager;
    private final GroupManager groupManager;

    private final MutableLiveData<Collection<InvitationItem>>
            pendingInvitations = new MutableLiveData<>();

    @Inject
    InvitationListViewModel(Application application,
                            @DatabaseExecutor Executor dbExecutor,
                            ContactManager contactManager,
                            ContextManager contextManager,
                            SharingContextManager sharingContextManager,
                            SharingGroupManager sharingGroupManager,
                            GroupManager groupManager,
                            EventBus eventBus) {
        super(application);
        this.dbExecutor = dbExecutor;
        this.contactManager = contactManager;
        this.contextManager = contextManager;
        this.sharingContextManager = sharingContextManager;
        this.sharingGroupManager = sharingGroupManager;
        this.eventBus = eventBus;
        this.groupManager = groupManager;
        this.eventBus.addListener(this);
    }

    void onCreate() {
        if (pendingInvitations.getValue() == null) loadPendingInvitations();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        eventBus.removeListener(this);
    }

    @Override
    public void eventOccurred(Event e) {
        if (e instanceof ContextInvitationRemovedEvent ||
                e instanceof ContextInvitationAddedEvent ||
                e instanceof RemovePendingContextEvent ||
                e instanceof GroupInvitationAddedEvent ||
                e instanceof GroupAccessRequestAddedEvent ||
                e instanceof GroupAccessRequestRemovedEvent) {
            loadPendingInvitations();
        }
    }

    private void loadPendingInvitations() {
        dbExecutor.execute(() -> {
            try {
                LOG.info("Start loading context invitations! ");
                Collection<ContextInvitation> pContextInvitations =
                        contextManager.getPendingContextInvitations();
                List<InvitationItem> items =
                        new ArrayList<>();
                for (ContextInvitation p : pContextInvitations) {
                    Contact c = contactManager.getContact(p.getContactId());
                    items.add(new InvitationItem(p, c.getAlias(), p.getName(),
                            InvitationItem.InvitationType.CONTEXT, null));
                }
                LOG.info("Start loading group invitations! ");
                Collection<GroupInvitation> pGroupInvitations =
                        groupManager.getGroupInvitations();
                for (GroupInvitation p : pGroupInvitations) {
                    Contact c = contactManager.getContact(p.getContactId());
                    String contextName =
                            contextManager.getContext(p.getContextId())
                                    .getName();

                    InvitationItem.InvitationType itemInvType = InvitationItem.InvitationType.FORUM;
                    if (p.getGroupInvitationType() == GroupInvitationType.PrivateGroup) {
                        itemInvType = InvitationItem.InvitationType.GROUP;
                    }
                    items.add(new InvitationItem(p, c.getAlias(), contextName,
                            itemInvType, null));
                }

                LOG.info("Start loading group access requests! ");
                Collection<ForumAccessRequest> pForumAccessRequests =
                        groupManager.getGroupAccessRequests();
                List<String> uniqueOutgoingRequests = new ArrayList<>();
                // we have multiple outgoing requests for the same group (all the moderators), so
                // we must only show one request instead of all of them.
                for (ForumAccessRequest p : pForumAccessRequests) {
                    if (!p.isIncoming()){
                        if (!uniqueOutgoingRequests.contains(p.getGroupId())){
                            uniqueOutgoingRequests.add(p.getGroupId());
                            String contextName =
                                    contextManager.getContext(p.getContextId())
                                            .getName();

                            InvitationItem.InvitationType itemInvType = InvitationItem.InvitationType.FORUM;

                            items.add(new InvitationItem(null, p.getPeerName(), contextName,
                                    itemInvType, p));
                        }

                    } else {
                        String contextName =
                                contextManager.getContext(p.getContextId())
                                        .getName();

                        InvitationItem.InvitationType itemInvType = InvitationItem.InvitationType.FORUM;

                        items.add(new InvitationItem(null, p.getPeerName(), contextName,
                                itemInvType, p));
                    }
                }

                LOG.info("Total Invitations: " + items.size());
                pendingInvitations.postValue(items);
            } catch (DbException | FormatException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    LiveData<Collection<InvitationItem>> getPendingInvitations() {
        return pendingInvitations;
    }

    void rejectPendingContextInvitation(ContextInvitation contextInvitation) {
        dbExecutor.execute(() -> {
            try {
                sharingContextManager
                        .rejectContextInvitation(contextInvitation);
            } catch (DbException e) {
                e.printStackTrace();
            }
        });
    }

    void joinPendingContext(ContextInvitation contextInvitation) {
        dbExecutor.execute(() -> {
            try {
                sharingContextManager
                        .acceptContextInvitation(
                                contextInvitation.getContextId());
            } catch (DbException e) {
                e.printStackTrace();
            }
        });
    }

    void joinPendingGroup(GroupInvitation groupInvitation) {
        dbExecutor.execute(() -> {
            try {
                sharingGroupManager
                        .acceptGroupInvitation(groupInvitation);
            } catch (DbException | FormatException e) {
                e.printStackTrace();
            }
        });
    }

    void rejectPendingGroupInvitation(GroupInvitation groupInvitation) {
        dbExecutor.execute(() -> {
            try {
                sharingGroupManager
                        .rejectGroupInvitation(groupInvitation);
            } catch (DbException | FormatException e) {
                e.printStackTrace();
            }
        });
    }

    void rejectPendingGroupAccessRequest(ForumAccessRequest forumAccessRequest) {
        dbExecutor.execute(() -> {
            try {
                sharingGroupManager
                        .rejectGroupAccessRequest(forumAccessRequest);
            } catch (DbException e) {
                e.printStackTrace();
            }
        });
        loadPendingInvitations();
    }

    void acceptPendingGroupAccessRequest(ForumAccessRequest forumAccessRequest) {
        dbExecutor.execute(() -> {
            try {
                sharingGroupManager
                        .acceptGroupAccessRequest(forumAccessRequest);
            } catch (DbException e) {
                e.printStackTrace();
            }
        });
        loadPendingInvitations();
    }

}
