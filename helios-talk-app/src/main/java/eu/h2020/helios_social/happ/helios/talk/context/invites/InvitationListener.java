package eu.h2020.helios_social.happ.helios.talk.context.invites;


public interface InvitationListener {

	void onRejectGroup(InvitationItem item);

	void onJoinGroup(InvitationItem item);

	void onRejectContext(InvitationItem item);

	void onJoinContext(InvitationItem item);

}
