package eu.h2020.helios_social.happ.helios.talk.contactselection;

import java.util.Collection;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.controller.DbController;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

@NotNullByDefault
public interface ContactSelectorController<I extends SelectableContactItem>
		extends DbController {

	void loadContacts(String groupId, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<I>, DbException> handler);

}
