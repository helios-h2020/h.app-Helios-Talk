package eu.h2020.helios_social.happ.helios.talk.context.invites;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;
import eu.h2020.helios_social.modules.groupcommunications.api.context.ContextType;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.ContextInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.GroupInvitationType;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitation;

@NotNullByDefault
public class InvitationViewHolder extends ViewHolder {

	private final InvitationListener listener;
	private final ImageView icon;
	private final TextView name;
	private final TextView time;
	private final TextView message;
	private final Button confirmButton;
	private final Button deleteButton;
	private final TextView outgoingRequet;


	public InvitationViewHolder(View v, InvitationListener listener) {
		super(v);
		icon = v.findViewById(R.id.icon);
		name = v.findViewById(R.id.name);
		time = v.findViewById(R.id.time);
		message = v.findViewById(R.id.message);
		confirmButton = v.findViewById(R.id.confirmButton);
		deleteButton = v.findViewById(R.id.deleteButton);
		outgoingRequet = v.findViewById(R.id.outgoingPrompt);
		this.listener = listener;
	}

	public void bind(
			InvitationItem item) {


		if (item.getInvitationType()
				.equals(InvitationItem.InvitationType.CONTEXT)) {
			icon.setImageResource(R.drawable.ic_context_white);
			ContextInvitation contextInvite =
					(ContextInvitation) item.getInvitation();
			name.setText(contextInvite.getName());
			time.setText(UiUtils.formatDate(time.getContext(),
					contextInvite.getTimestamp()));
			String prompted_outgoing = "You invited %s to join %s";
			String prompted_incoming = "%s invited you to join %s";
			if (contextInvite.getContextType().equals(ContextType.GENERAL)) {
				if (contextInvite.isIncoming()) {
					message.setText(String.format(prompted_incoming,
							item.getContactName(),
							item.getInvitation().getName() + " context")
					);
				} else {
					message.setText(
							String.format(prompted_outgoing,
									item.getContactName(),
									item.getInvitation().getName() +
											" context."));
				}
			} else {
				if (contextInvite.isIncoming()) {
					message.setText(String.format(prompted_incoming,
							item.getContactName(),
							" location context " +
									item.getInvitation().getName() + ".")
					);
				} else {
					message.setText(
							String.format(prompted_outgoing,
									item.getContactName(),
									" location context " +
											item.getInvitation().getName() +
											"."));
				}
			}
			deleteButton.setOnClickListener(v -> {
				listener.onRejectContext(item);
			});

			confirmButton.setOnClickListener(v -> {
				listener.onJoinContext(item);
			});

			if (!contextInvite.isIncoming()) {
				deleteButton.setVisibility(View.GONE);
				confirmButton.setVisibility(View.GONE);
				outgoingRequet.setText(R.string.waiting_context_response);
				outgoingRequet.setVisibility(View.VISIBLE);
			} else {
				outgoingRequet.setVisibility(View.GONE);
			}
		} else {
			icon.setImageResource(R.drawable.ic_group_white);
			GroupInvitation groupInvitation =
					(GroupInvitation) item.getInvitation();
			name.setText(groupInvitation.getName());
			time.setText(UiUtils.formatDate(time.getContext(),
					groupInvitation.getTimestamp()));
			String prompted_outgoing =
					"You invited %s to join private group \"%s\" in context \"%s\"";
			String prompted_incoming =
					"%s invited you to join private group \"%s\" in context \"%s\"";
			if (groupInvitation.getGroupInvitationType()
					.equals(GroupInvitationType.PrivateGroup)) {
				if (groupInvitation.isIncoming()) {
					message.setText(String.format(prompted_incoming,
							item.getContactName(),
							item.getInvitation().getName(),
							item.getContextName())
					);
				} else {
					message.setText(
							String.format(prompted_outgoing,
									item.getContactName(),
									item.getInvitation().getName(),
									item.getContextName()
							));
				}
			} else {
				//TODO: Forum Invites
			}
			deleteButton.setOnClickListener(v -> {
				listener.onRejectGroup(item);
			});

			confirmButton.setOnClickListener(v -> {
				listener.onJoinGroup(item);
			});

			if (!groupInvitation.isIncoming()) {
				deleteButton.setVisibility(View.GONE);
				confirmButton.setVisibility(View.GONE);
				outgoingRequet.setText(R.string.waiting_group_response);
				outgoingRequet.setVisibility(View.VISIBLE);
			} else {
				outgoingRequet.setVisibility(View.GONE);
			}
		}

	}

}
