package eu.h2020.helios_social.happ.helios.talk.context.invites;

import eu.h2020.helios_social.modules.groupcommunications.api.forum.sharing.ForumAccessRequest;
import eu.h2020.helios_social.modules.groupcommunications.api.sharing.Invitation;

public class InvitationItem {

	enum InvitationType {
		CONTEXT, GROUP, FORUM
	}

	private Invitation invitation;
	private String contactName;
	private InvitationType invitationType;
	private String contextName;
	private ForumAccessRequest request;

	public InvitationItem(Invitation invitation, String contactName, String contextName,
			InvitationType type, ForumAccessRequest request) {
		this.invitation = invitation;
		this.contactName = contactName;
		this.contextName = contextName;
		this.invitationType = type;
		this.request = request;
	}

	public Invitation getInvitation() {
		return invitation;
	}

	public String getContactName() {
		return contactName;
	}

	public String getContextName() {
		return contextName;
	}

	public InvitationType getInvitationType() {
		return invitationType;
	}

	public ForumAccessRequest getRequest() {
		return request;
	}
}
