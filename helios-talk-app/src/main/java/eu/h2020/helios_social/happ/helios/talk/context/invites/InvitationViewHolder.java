package eu.h2020.helios_social.happ.helios.talk.context.invites;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.sharing.ForumAccessRequest;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
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
    private final ImageButton deleteOutgoingButton;


    public InvitationViewHolder(View v, InvitationListener listener) {
        super(v);
        icon = v.findViewById(R.id.icon);
        name = v.findViewById(R.id.name);
        time = v.findViewById(R.id.time);
        message = v.findViewById(R.id.message);
        confirmButton = v.findViewById(R.id.confirmButton);
        deleteButton = v.findViewById(R.id.deleteButton);
        outgoingRequet = v.findViewById(R.id.outgoingPrompt);
        deleteOutgoingButton = v.findViewById(R.id.deleteOutgoingButton);
        this.listener = listener;
    }

    public void bind(InvitationItem item) {

        if (item.getInvitationType()
                .equals(InvitationItem.InvitationType.CONTEXT)) {
            icon.setImageResource(R.drawable.ic_context_white);
            ContextInvitation contextInvite =
                    (ContextInvitation) item.getInvitation();
            name.setText(contextInvite.getName());
            time.setText(UiUtils.formatDate(time.getContext(),
                                            contextInvite.getTimestamp()));

            String type = "";
            if (contextInvite.getContextType().equals(ContextType.LOCATION)) {
                type = " location";
            } else if (contextInvite.getContextType().equals(ContextType.TIME)) {
                type = " temporal";
            } else if (contextInvite.getContextType().equals(ContextType.SPATIOTEMPORAL)) {
                type = " spatio-temporal";
            }
            String prompted_outgoing = "You invited %s to join %s";
            String prompted_incoming = "%s invited you to join %s";

            if (contextInvite.isIncoming()) {
                message.setText(String.format(prompted_incoming,
                                              item.getContactName(),
                                              item.getInvitation().getName() + type + " context")
                );
            } else {
                message.setText(
                        String.format(prompted_outgoing,
                                      item.getContactName(),
                                      item.getInvitation().getName() +
                                              type + " context."));
            }

            deleteButton.setOnClickListener(v -> {
                listener.onRejectContext(item);
            });
            confirmButton.setText(R.string.join_context);
            confirmButton.setOnClickListener(v -> {
                listener.onJoinContext(item);
            });
            deleteOutgoingButton.setOnClickListener(v -> listener.onRejectContext(item));

            if (!contextInvite.isIncoming()) {
                deleteButton.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);
                deleteOutgoingButton.setVisibility(View.VISIBLE);
                outgoingRequet.setText(R.string.waiting_context_response);
                outgoingRequet.setVisibility(View.VISIBLE);
            } else {
                outgoingRequet.setVisibility(View.GONE);
                deleteOutgoingButton.setVisibility(View.GONE);
            }
        } else if (item.getInvitation()!=null){
            GroupInvitation groupInvitation =
                    (GroupInvitation) item.getInvitation();
            name.setText(groupInvitation.getName());
            time.setText(UiUtils.formatDate(time.getContext(),
                                            groupInvitation.getTimestamp()));
            String prompted_outgoing =
                    "You invited %s to join " + groupInvitation.getGroupInvitationType().toString() + " \"%s\" in context \"%s\"";
            String prompted_incoming =
                    "%s invited you to join " + groupInvitation.getGroupInvitationType().toString() + " \"%s\" in context \"%s\"";
            if (groupInvitation.getGroupInvitationType()
                    .equals(GroupInvitationType.PrivateGroup)) {
                icon.setImageResource(R.drawable.ic_group_white);
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
                icon.setImageResource(R.drawable.ic_community);
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
            }
            deleteButton.setOnClickListener(v -> {
                listener.onRejectGroup(item);
            });
            confirmButton.setText(R.string.join_context);

            confirmButton.setOnClickListener(v -> {
                listener.onJoinGroup(item);
            });
            deleteOutgoingButton.setOnClickListener(v -> listener.onRejectGroup(item));

            if (!groupInvitation.isIncoming()) {
                deleteButton.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);
                deleteOutgoingButton.setVisibility(View.VISIBLE);
                outgoingRequet.setText(R.string.waiting_group_response);
                outgoingRequet.setVisibility(View.VISIBLE);
            } else {
                outgoingRequet.setVisibility(View.GONE);
                deleteOutgoingButton.setVisibility(View.GONE);
            }
        }
        // for forum access requests
        else {
            ForumAccessRequest forumAccessRequest =
                    item.getRequest();
            name.setText(forumAccessRequest.getName());
            time.setText(UiUtils.formatDate(time.getContext(),
                    forumAccessRequest.getTimestamp()));
            String prompted_outgoing =
                    "You requested to join forum" + " \"%s\" in context \"%s\"";
            String prompted_incoming =
                    "%s wants to join forum" + " \"%s\" in context \"%s\"";

            icon.setImageResource(R.drawable.ic_protected_forum);
            if (forumAccessRequest.isIncoming()) {
                message.setText(String.format(prompted_incoming,
                        item.getContactName(),
                        item.getRequest().getName(),
                        item.getContextName())
                );
            } else {
                message.setText(
                        String.format(prompted_outgoing,
                                item.getRequest().getName(),
                                item.getContextName()
                        ));
            }


            deleteButton.setOnClickListener(v -> {
                listener.onRejectGroupAccessRequest(item);
            });
            confirmButton.setText(R.string.accept);
            confirmButton.setOnClickListener(v -> {
                listener.onAcceptGroupAccessRequest(item);
            });

            if (!forumAccessRequest.isIncoming()) {
                deleteButton.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);
                outgoingRequet.setText(R.string.waiting_forum_response);
                outgoingRequet.setVisibility(View.VISIBLE);
            } else {
                outgoingRequet.setVisibility(View.GONE);
            }
        }

    }

}
