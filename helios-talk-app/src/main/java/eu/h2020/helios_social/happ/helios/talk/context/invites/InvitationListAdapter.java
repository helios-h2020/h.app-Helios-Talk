package eu.h2020.helios_social.happ.helios.talk.context.invites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;
import eu.h2020.helios_social.modules.groupcommunications.api.sharing.Invitation;

@NotNullByDefault
class InvitationListAdapter extends
		HeliosTalkAdapter<InvitationItem, InvitationViewHolder> {

	private final InvitationListener listener;

	InvitationListAdapter(Context ctx, InvitationListener listener,
			Class<InvitationItem> c) {
		super(ctx, c);
		this.listener = listener;
	}

	@Override
	public InvitationViewHolder onCreateViewHolder(ViewGroup viewGroup,
			int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.list_item_invite, viewGroup, false);
		return new InvitationViewHolder(v, listener);
	}

	@Override
	public void onBindViewHolder(
			InvitationViewHolder pendingContactViewHolder, int i) {
		pendingContactViewHolder.bind(items.get(i));
	}

	@Override
	public int compare(
			InvitationItem item1, InvitationItem item2) {
		long timestamp1 = item1.getInvitation().getTimestamp();
		long timestamp2 = item2.getInvitation().getTimestamp();
		return -Long.compare(timestamp1, timestamp2);
	}

	@Override
	public boolean areContentsTheSame(
			InvitationItem item1,
			InvitationItem item2) {
		Invitation p1 = item1.getInvitation();
		Invitation p2 = item2.getInvitation();
		return p1.getContextId().equals(p2.getContextId());
	}

	@Override
	public boolean areItemsTheSame(
			InvitationItem item1,
			InvitationItem item2) {
		Invitation p1 = item1.getInvitation();
		Invitation p2 = item2.getInvitation();
		return p1.getContextId().equals(p2.getContextId()) &&
				p1.getContactId().equals(p2.getContactId());
	}

}
