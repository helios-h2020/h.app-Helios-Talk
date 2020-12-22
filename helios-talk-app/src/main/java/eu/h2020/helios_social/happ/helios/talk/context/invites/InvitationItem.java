package eu.h2020.helios_social.happ.helios.talk.context.invites;

import eu.h2020.helios_social.modules.groupcommunications.api.sharing.Invitation;

public class InvitationItem {

	enum InvitationType {
		CONTEXT, GROUP, FORUM
	}

	private Invitation invitation;
	private String contactName;
	private InvitationType invitationType;
	private String contextName;

	public InvitationItem(Invitation invitation, String contactName, String contextName,
			InvitationType type) {
		this.invitation = invitation;
		this.contactName = contactName;
		this.contextName = contextName;
		this.invitationType = type;
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
}
