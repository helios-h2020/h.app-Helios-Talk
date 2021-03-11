package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContact;

@NotNullByDefault
class PendingContactListAdapter extends
		HeliosTalkAdapter<PendingContactItem, PendingContactViewHolder> {

	private final PendingContactListener listener;

	PendingContactListAdapter(Context ctx, PendingContactListener listener,
			Class<PendingContactItem> c) {
		super(ctx, c);
		this.listener = listener;
	}

	@Override
	public PendingContactViewHolder onCreateViewHolder(ViewGroup viewGroup,
			int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.list_item_pending_contact, viewGroup, false);
		return new PendingContactViewHolder(v, listener);
	}

	@Override
	public void onBindViewHolder(
			PendingContactViewHolder pendingContactViewHolder, int i) {
		pendingContactViewHolder.bind(items.get(i));
	}

	@Override
	public int compare(PendingContactItem item1, PendingContactItem item2) {
		long timestamp1 = item1.getPendingContact().getTimestamp();
		long timestamp2 = item2.getPendingContact().getTimestamp();
		return Long.compare(timestamp1, timestamp2);
	}

	@Override
	public boolean areContentsTheSame(PendingContactItem item1,
			PendingContactItem item2) {
		PendingContact p1 = item1.getPendingContact();
		PendingContact p2 = item2.getPendingContact();
		return p1.getId().equals(p2.getId());
	}

	@Override
	public boolean areItemsTheSame(PendingContactItem item1,
			PendingContactItem item2) {
		PendingContact p1 = item1.getPendingContact();
		PendingContact p2 = item2.getPendingContact();
		return p1.getId().equals(p2.getId());
	}

}
