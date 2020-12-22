package eu.h2020.helios_social.happ.helios.talk.contactselection;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contact.BaseContactListAdapter;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactItemViewHolder;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

@NotNullByDefault
public abstract class BaseContactSelectorAdapter<I extends SelectableContactItem, H extends ContactItemViewHolder<I>>
		extends BaseContactListAdapter<I, H> {

	public BaseContactSelectorAdapter(Context context, Class<I> c,
			OnContactClickListener<I> listener) {
		super(context, c, listener);
	}

	public Collection<ContactId> getSelectedContactIds() {
		Collection<ContactId> selected = new ArrayList<>();

		for (int i = 0; i < items.size(); i++) {
			SelectableContactItem item = items.get(i);
			if (item.isSelected()) selected.add(item.getContact().getId());
		}
		return selected;
	}

}
