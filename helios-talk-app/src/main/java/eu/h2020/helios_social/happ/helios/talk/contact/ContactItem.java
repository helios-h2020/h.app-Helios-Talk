package eu.h2020.helios_social.happ.helios.talk.contact;

import javax.annotation.concurrent.NotThreadSafe;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;

@NotThreadSafe
@NotNullByDefault
public class ContactItem {

	protected final Contact contact;
	private boolean connected;

	public ContactItem(Contact contact) {
		this(contact, false);
	}

	public ContactItem(Contact contact, boolean connected) {
		this.contact = contact;
		this.connected = connected;
	}

	public Contact getContact() {
		return contact;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

}
