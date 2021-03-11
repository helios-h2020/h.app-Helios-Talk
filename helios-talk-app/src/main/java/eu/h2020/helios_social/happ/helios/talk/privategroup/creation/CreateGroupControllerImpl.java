package eu.h2020.helios_social.happ.helios.talk.privategroup.creation;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.JoinGroupEvent;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitationFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.SharingGroupManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;

@Immutable
@NotNullByDefault
class CreateGroupControllerImpl extends ContactSelectorControllerImpl
		implements CreateGroupController {

	private static final Logger LOG =
			Logger.getLogger(CreateGroupControllerImpl.class.getName());

	private final GroupFactory groupFactory;
	private final GroupManager groupManager;
	private final GroupInvitationFactory groupInvitationFactory;
	private final SharingGroupManager sharingGroupManager;
	private final EventBus eventBus;

	@Inject
	CreateGroupControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			GroupFactory groupFactory, GroupManager groupManager,
			ContextualEgoNetwork egoNetwork,
			GroupInvitationFactory groupInvitationFactory,
			SharingGroupManager sharingGroupManager, EventBus eventBus) {
		super(dbExecutor, lifecycleManager, contactManager, egoNetwork);
		this.groupFactory = groupFactory;
		this.groupManager = groupManager;
		this.groupInvitationFactory = groupInvitationFactory;
		this.sharingGroupManager = sharingGroupManager;
		this.eventBus = eventBus;
	}

	@Override
	public void createGroup(String name,
			ResultExceptionHandler<String, DbException> handler) {
		runOnDbThread(() -> {
			LOG.info("Creating and Adding group to database...");
			try {
				String currentContextId =
						egoNetwork.getCurrentContext().getData().toString()
								.split("%")[1];
				PrivateGroup group =
						groupFactory.createPrivateGroup(name, currentContextId);
				groupManager.addGroup(group);
				eventBus.broadcast(
						new JoinGroupEvent(group.getId(), group.getPassword(),
								GroupType.PrivateGroup));
				handler.onResult(group.getId());
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			} catch (FormatException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	protected boolean isDisabled(String groupId, Contact contact)
			throws DbException, FormatException {
		return !groupManager.isInvitationAllowed(groupId,
				GroupType.PrivateGroup);
	}

	@Override
	public void sendInvitation(String groupId, Collection<ContactId> contactIds,
			ResultExceptionHandler<Void, DbException> handler) {
		runOnDbThread(() -> {
			try {
				PrivateGroup group = (PrivateGroup)
						groupManager.getGroup(groupId, GroupType.PrivateGroup);
				for (ContactId c : contactIds) {
					GroupInvitation groupInvitation = groupInvitationFactory
							.createOutgoingGroupInvitation(c, group);
					sharingGroupManager.sendGroupInvitation(groupInvitation);
				}
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			} catch (FormatException e) {
				logException(LOG, WARNING, e);
				e.printStackTrace();
			}
		});
	}
}
