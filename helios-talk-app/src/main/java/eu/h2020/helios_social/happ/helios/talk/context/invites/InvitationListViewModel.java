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
import eu.h2020.helios_social.happ.helios.talk.api.context.ContextInvitationAddedEvent;
import eu.h2020.helios_social.happ.helios.talk.api.context.ContextInvitationRemovedEvent;
import eu.h2020.helios_social.happ.helios.talk.api.context.RemovePendingContextEvent;
import eu.h2020.helios_social.happ.helios.talk.api.db.DatabaseExecutor;
import eu.h2020.helios_social.happ.helios.talk.api.event.Event;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventListener;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.ContextInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.SharingContextManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.SharingGroupManager;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logException;
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
				e instanceof RemovePendingContextEvent) {
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
							InvitationItem.InvitationType.CONTEXT));
				}
				LOG.info("Start loading group invitations! ");
				Collection<GroupInvitation> pGroupInvitations =
						groupManager.getGroupInvitations();
				for (GroupInvitation p : pGroupInvitations) {
					Contact c = contactManager.getContact(p.getContactId());
					String contextName =
							contextManager.getContext(p.getContextId())
									.getName();
					items.add(new InvitationItem(p, c.getAlias(), contextName,
							InvitationItem.InvitationType.GROUP));
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

}
