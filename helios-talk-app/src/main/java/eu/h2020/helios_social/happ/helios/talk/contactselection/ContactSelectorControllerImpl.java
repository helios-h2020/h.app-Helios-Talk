package eu.h2020.helios_social.happ.helios.talk.contactselection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;

@Immutable
@NotNullByDefault
public abstract class ContactSelectorControllerImpl
		extends DbControllerImpl
		implements ContactSelectorController<SelectableContactItem> {

	private static final Logger LOG =
			Logger.getLogger(ContactSelectorControllerImpl.class.getName());

	protected final ContactManager contactManager;
	protected final ContextualEgoNetwork egoNetwork;

	public ContactSelectorControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ContextualEgoNetwork egoNetwork) {
		super(dbExecutor, lifecycleManager);
		this.contactManager = contactManager;
		this.egoNetwork = egoNetwork;
	}

	@Override
	public void loadContacts(String groupId, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<SelectableContactItem>, DbException> handler) {
		runOnDbThread(() -> {
			try {
				Collection<SelectableContactItem> contacts = new ArrayList<>();
				String currentContext =
						egoNetwork.getCurrentContext().getData().toString()
								.split("%")[1];
				for (Contact c : contactManager.getContacts(currentContext)) {
					// was this contact already selected?
					boolean selected =
							selection.contains(c.getId());
					// can this contact be selected?
					boolean disabled = isDisabled(groupId, c);
					contacts.add(new SelectableContactItem(c, selected,
							disabled));
				}
				handler.onResult(contacts);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			} catch (FormatException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	@DatabaseExecutor
	protected abstract boolean isDisabled(String groupId, Contact c)
			throws DbException, FormatException;

}
