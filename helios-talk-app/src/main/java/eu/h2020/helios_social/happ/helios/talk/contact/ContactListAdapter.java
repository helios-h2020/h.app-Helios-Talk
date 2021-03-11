package eu.h2020.helios_social.happ.helios.talk.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@NotNullByDefault
public class ContactListAdapter extends
		BaseContactListAdapter<ContactListItem, ContactListItemViewHolder> {

	public ContactListAdapter(Context context,
			OnContactClickListener<ContactListItem> listener) {
		super(context, ContactListItem.class, listener);
	}

	@Override
	public ContactListItemViewHolder onCreateViewHolder(ViewGroup viewGroup,
			int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.list_item_contact, viewGroup, false);

		return new ContactListItemViewHolder(v);
	}

	@Override
	public boolean areContentsTheSame(ContactListItem c1, ContactListItem c2) {
		// check for all properties that influence visual
		// representation of contact
		if (c1.isEmpty() != c2.isEmpty()) {
			return false;
		}
		if (c1.getUnreadCount() != c2.getUnreadCount()) {
			return false;
		}
		if (c1.getTimestamp() != c2.getTimestamp()) {
			return false;
		}
		return c1.isConnected() == c2.isConnected();
	}

	@Override
	public int compare(ContactListItem c1, ContactListItem c2) {
		return Long.compare(c2.getTimestamp(), c1.getTimestamp());
	}

}
