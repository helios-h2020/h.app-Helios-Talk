package eu.h2020.helios_social.happ.helios.talk.context.invites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.sharing.ForumAccessRequest;
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
		long timestamp1;
		long timestamp2;

		if (item1.getInvitation()!=null) {
			timestamp1 = item1.getInvitation().getTimestamp();
		} else {
			timestamp1 = item1.getRequest().getTimestamp();
		}
		if (item2.getInvitation()!=null) {
			timestamp2 = item2.getInvitation().getTimestamp();
		} else {
			timestamp2 = item2.getRequest().getTimestamp();
		}
		return -Long.compare(timestamp1, timestamp2);
	}

	@Override
	public boolean areContentsTheSame(
			InvitationItem item1,
			InvitationItem item2) {
		if (item1.getInvitation()!=null && item2.getInvitation()!=null) {
			Invitation p1 = item1.getInvitation();
			Invitation p2 = item2.getInvitation();
			return p1.getContextId().equals(p2.getContextId());
		} else if (item1.getRequest()!=null && item2.getRequest()!=null) {
			ForumAccessRequest p1 = item1.getRequest();
			ForumAccessRequest p2 = item2.getRequest();
			return p1.getContextId().equals(p2.getContextId());
		}
		return false;
	}

	@Override
	public boolean areItemsTheSame(
			InvitationItem item1,
			InvitationItem item2) {
		if (item1.getInvitation()!=null && item2.getInvitation()!=null) {
			Invitation p1 = item1.getInvitation();
			Invitation p2 = item2.getInvitation();
			return p1.getContextId().equals(p2.getContextId()) &&
					p1.getContactId().equals(p2.getContactId());
		} else if (item1.getRequest()!=null && item2.getRequest()!=null) {
			ForumAccessRequest p1 = item1.getRequest();
			ForumAccessRequest p2 = item2.getRequest();
			return p1.getContextId().equals(p2.getContextId()) &&
					p1.getContactId().equals(p2.getContactId());
		}

		return false;
	}

}
