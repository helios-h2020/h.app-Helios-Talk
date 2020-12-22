package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import javax.annotation.concurrent.Immutable;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContact;

@NotNullByDefault
class PendingContactItem {

	private final PendingContact pendingContact;

	PendingContactItem(PendingContact pendingContact) {
		this.pendingContact = pendingContact;
	}

	PendingContact getPendingContact() {
		return pendingContact;
	}
}
