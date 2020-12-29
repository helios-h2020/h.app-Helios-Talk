package eu.h2020.helios_social.modules.groupcommunications.api.context.sharing;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

/**
 * Interface for Sharing Context Manager
 */
public interface SharingContextManager {

	/**
	 * Sends a Context Invitation
	 *
	 * @param contextInvitation
	 */
	public void sendContextInvitation(ContextInvitation contextInvitation)
			throws DbException;

	/**
	 * Accept all context invitations with the given context identifier
	 *
	 * @param pendingContextId
	 */
	public void acceptContextInvitation(String pendingContextId)
			throws DbException;

	/**
	 * Accepts a context Invitation
	 * @param contextInvitation
	 * @throws DbException
	 */
	void acceptContextInvitation(ContextInvitation contextInvitation)
			throws DbException;

	/**
	 * Reject Context Invitation
	 *
	 * @param contextInvitation
	 */
	public void rejectContextInvitation(ContextInvitation contextInvitation)
			throws DbException;
}
