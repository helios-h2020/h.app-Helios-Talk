package eu.h2020.helios_social.happ.helios.talk.contact;

import android.view.View;

import javax.annotation.Nullable;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contact.BaseContactListAdapter.OnContactClickListener;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

import static androidx.core.view.ViewCompat.setTransitionName;

@UiThread
@NotNullByDefault
class ContactListItemViewHolder extends ContactItemViewHolder<ContactListItem> {

	public ContactListItemViewHolder(View v) {
		super(v);
	}

	@Override
	protected void bind(ContactListItem item, @Nullable
			OnContactClickListener<ContactListItem> listener) {
		super.bind(item, listener);

		ContactId c = item.getContact().getId();
		setTransitionName(avatar, "avatar" + c.getId());
		setTransitionName(bulb, "bulb" + c.getId());
	}
	/*private final TextView unread;
	private final TextView date;

	ContactListItemViewHolder(View v) {
		super(v);
		unread = v.findViewById(R.id.unreadCountView);
		date = v.findViewById(R.id.dateView);
	}

	@Override
	protected void bind(ContactListItem item, @Nullable
			OnContactClickListener<ContactListItem> listener) {
		super.bind(item, listener);

		// unread count
		int unreadCount = item.getUnreadCount();
		if (unreadCount > 0) {
			unread.setText(String.format(Locale.getDefault(), "%d", unreadCount));
			unread.setVisibility(View.VISIBLE);
		} else {
			unread.setVisibility(View.INVISIBLE);
		}

		// date of last message
		if (item.isEmpty()) {
			date.setText(R.string.date_no_private_messages);
		} else {
			long timestamp = item.getTimestamp();
			date.setText(formatDate(date.getContext(), timestamp));
		}

		ContactId c = item.getContact().getId();
		setTransitionName(avatar, UiUtils.getAvatarTransitionName(c));
		setTransitionName(bulb, UiUtils.getBulbTransitionName(c));
}*/

}
