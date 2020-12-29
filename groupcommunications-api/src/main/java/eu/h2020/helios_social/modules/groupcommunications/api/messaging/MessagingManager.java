package eu.h2020.helios_social.modules.groupcommunications.api.messaging;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;

/**
 * Interface for the Messaging Manager
 */
public interface MessagingManager {

	/**
	 * Sends the given private message in the given contact and context
	 * @param contactId
	 * @param contextId
	 * @param privateMessage
	 * @return
	 * @throws DbException
	 */
	MessageHeader sendPrivateMessage(ContactId contactId, String contextId,
			Message privateMessage)
			throws DbException;

	/**
	 * Sends a message to the defined group
	 * @param group
	 * @param groupMessage
	 * @return
	 * @throws DbException
	 * @throws FormatException
	 */
	MessageHeader sendGroupMessage(Group group,
										  GroupMessage groupMessage)
			throws DbException, FormatException;
}
