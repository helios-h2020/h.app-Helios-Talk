package eu.h2020.helios_social.happ.helios.talk.contactselection;

import javax.annotation.concurrent.NotThreadSafe;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactItem;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;

@NotThreadSafe
@NotNullByDefault
public class SelectableContactItem extends ContactItem {

	private boolean selected, disabled;

	public SelectableContactItem(Contact contact, boolean selected,
			boolean disabled) {
		super(contact);
		this.selected = selected;
		this.disabled = disabled;
	}

	boolean isSelected() {
		return selected;
	}

	void toggleSelected() {
		selected = !selected;
	}

	public boolean isDisabled() {
		return disabled;
	}

}
