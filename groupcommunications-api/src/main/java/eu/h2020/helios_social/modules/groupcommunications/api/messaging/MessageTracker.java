package eu.h2020.helios_social.modules.groupcommunications.api.messaging;


import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

/**
 * Interface for Message Tracker
 */
public interface MessageTracker<T> {

	/**
	 * Initializes the group count with zero messages
	 */
	void initializeGroupCount(T txn, String groupId)
			throws DbException;

	/**
	 * Gets the number of visible and unread messages in the group
	 **/
	GroupCount getGroupCount(String groupId) throws DbException;

	/**
	 * Gets the number of visible and unread messages in the group
	 **/
	GroupCount getGroupCount(T txn, String groupId) throws
			DbException;

	/**
	 * Updates the group count for the given incoming message.
	 */
	void trackIncomingMessage(T txn, Message message)
			throws DbException;

	/**
	 * Updates the group count for the given outgoing message.
	 */
	void trackOutgoingMessage(T txn, Message message)
			throws DbException;

	/**
	 * Updates the group count for the given message.
	 */
	void trackMessage(T txn, String groupId, long timestamp,
			boolean read)
			throws DbException;

	/**
	 * Marks a message as read and updates the group count.
	 */
	void setReadFlag(String groupId, String messageId)
			throws DbException;

	/**
	 * Resets the GroupCount
	 */
	void resetGroupCount(T txn, String groupId, int msgCount,
			int unreadCount) throws DbException;

}
