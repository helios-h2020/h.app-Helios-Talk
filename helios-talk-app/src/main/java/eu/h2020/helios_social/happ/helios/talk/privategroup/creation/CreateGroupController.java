package eu.h2020.helios_social.happ.helios.talk.privategroup.creation;


import java.util.Collection;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorController;
import eu.h2020.helios_social.happ.helios.talk.contactselection.SelectableContactItem;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

@NotNullByDefault
public interface CreateGroupController
		extends ContactSelectorController<SelectableContactItem> {

	void createGroup(String name,
			ResultExceptionHandler<String, DbException> result);

	void sendInvitation(String groupId, Collection<ContactId> contacts,
			ResultExceptionHandler<Void, DbException> result);

}
