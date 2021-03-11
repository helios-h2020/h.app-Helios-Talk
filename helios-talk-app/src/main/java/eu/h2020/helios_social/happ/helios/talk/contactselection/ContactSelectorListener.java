package eu.h2020.helios_social.happ.helios.talk.contactselection;


import java.util.Collection;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

@NotNullByDefault
public interface ContactSelectorListener {

	@UiThread
	void contactsSelected(Collection<ContactId> contacts);

}
