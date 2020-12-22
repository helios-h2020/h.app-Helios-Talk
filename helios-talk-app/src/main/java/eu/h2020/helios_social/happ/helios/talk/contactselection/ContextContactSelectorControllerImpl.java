package eu.h2020.helios_social.happ.helios.talk.contactselection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.happ.helios.talk.api.db.DatabaseExecutor;
import eu.h2020.helios_social.happ.helios.talk.api.lifecycle.LifecycleManager;
import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.ContextInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;

public abstract class ContextContactSelectorControllerImpl
		extends DbControllerImpl
		implements ContextContactSelectorController<SelectableContactItem> {

	private static final Logger LOG =
			Logger.getLogger(ContactSelectorControllerImpl.class.getName());

	private final ContactManager contactManager;
	protected final ContextManager contextManager;

	public ContextContactSelectorControllerImpl(
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ContextManager contextManager) {
		super(dbExecutor, lifecycleManager);
		this.contactManager = contactManager;
		this.contextManager = contextManager;
	}

	@Override
	public void loadContacts(String contextId, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<SelectableContactItem>, DbException> handler) {
		runOnDbThread(() -> {
			try {
				Collection<SelectableContactItem> contacts = new ArrayList<>();
				Collection<ContextInvitation> contextInvitations =
						contextManager.getOutgoingContextInvitations(contextId);
				for (Contact c : contactManager.getContacts()) {
					// was this contact already selected?
					boolean selected = selection.contains(c.getId());
					// can this contact be selected?
					boolean disabled = isDisabled(contextId, c, contextInvitations);
					contacts.add(new SelectableContactItem(c, selected,
							disabled));
				}
				handler.onResult(contacts);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			} catch (FormatException e) {
				e.printStackTrace();
			}
		});
	}

	@DatabaseExecutor
	protected abstract boolean isDisabled(String contextId, Contact c,
			Collection<ContextInvitation> contextInvitations)
			throws DbException, FormatException;

}
