package eu.h2020.helios_social.modules.groupcommunications.api.conversation;

import java.util.Collection;
import java.util.Set;

import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Favourite;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupCount;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;

/**
 * Interface for Conversation Manager
 */
public interface ConversationManager {

    /**
     * Returns all message headers given a group identifier
     *
     * @param groupId
     * @return
     * @throws DbException
     */
    Collection<MessageHeader> getMessageHeaders(String groupId)
            throws DbException;

    /**
     * Returns all group message headers given a group identifier
     *
     * @param groupId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<GroupMessageHeader> getGroupMessageHeaders(String groupId)
            throws DbException, FormatException;

    /**
     * Returns the text  body of a given message identifier
     *
     * @param messageId
     * @return
     * @throws DbException
     */
    String getMessageText(String messageId)
            throws DbException;

    /**
     * Returns the group corresponding to the private conversation for the given contact
     * in the given context
     *
     * @param contactId
     * @param contextId
     * @return
     */
    Group getContactGroup(ContactId contactId, String contextId)
            throws DbException;

    /**
     * Stores a group corresponding to the private conversation for the given contact
     * in the given context
     *
     * @param contactId
     * @param group
     * @throws DbException
     */
    void addContactGroup(ContactId contactId, Group group)
            throws DbException;

    /**
     * Returns the group count of messages exchanged in the given conversation
     *
     * @param groupId
     * @return
     */
    GroupCount getGroupCount(String groupId) throws DbException;

    /**
     * Returns the collection of favorites in the given context
     *
     * @param contextId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<Favourite> getFavourites(String contextId)
            throws DbException, FormatException;

    /**
     * Returns all the messages in the given private/group conversation based on groupId
     *
     * @param groupId
     * @return
     */
    Set<String> getMessageIds(String groupId);

    /**
     * Returns all conversation {@Link PrivateMassage}s based in groupId
     *
     * @param groupId Conversation Identifier
     * @return
     */
    Collection<Message> getAllMessages(String groupId);

    /**
     * Marks message as favorite with the given identifier
     *
     * @param messageId
     * @throws DbException
     */
    void favourite(String messageId) throws DbException;

    /**
     * Unflag message as favorite
     *
     * @param messageId
     * @throws DbException
     */
    void unfavourite(String messageId) throws DbException;

    /**
     * Deletes all conversation messages
     *
     * @param groupId
     */
    void deleteAllMessages(String groupId);

    /**
     * Deletes the given set of messages.
     *
     * @param messageIds set of message Identifiers to be deleted
     */
    void deleteMessages(Collection<String> messageIds);

    /**
     * Marks message as read
     *
     * @param groupId
     * @param messageId
     * @param read
     * @throws DbException
     */
    void setReadFlag(String groupId, String messageId, boolean read)
            throws DbException;
}
