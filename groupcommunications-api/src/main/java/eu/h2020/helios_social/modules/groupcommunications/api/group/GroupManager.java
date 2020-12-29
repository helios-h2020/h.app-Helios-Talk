package eu.h2020.helios_social.modules.groupcommunications.api.group;

import java.util.Collection;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.utils.Pair;

public interface GroupManager<T> {

    /**
     * Stores a new group conversation (private group/forum), the user joined/subscribed to.
     *
     * @param group
     * @throws FormatException
     * @throws DbException
     */
    void addGroup(Group group)
            throws FormatException, DbException;

    /**
     * Stores a new group conversation (private group/forum), the user joined/subscribed to.
     *
     * @param txn
     * @param group
     * @throws FormatException
     * @throws DbException
     */
    void addGroup(T txn, Group group)
            throws FormatException, DbException;

    /**
     * Returns the group with the given identifier and type
     *
     * @param groupId
     * @param groupType
     * @return
     * @throws FormatException
     * @throws DbException
     */
    Group getGroup(String groupId, GroupType groupType)
            throws FormatException, DbException;

    /**
     * Returns all groups (private groups and forums) in the given context, the user has
     * joined/subscribed
     *
     * @param contextId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<Group> getGroups(String contextId)
            throws DbException, FormatException;

    /**
     * Returns all groups (private groups and forums) in the given context, the user has
     * joined/subscribed
     *
     * @param txn
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<Group> getGroups(T txn)
            throws DbException, FormatException;

    /**
     * Returns the pseudo-identifier and pseudonym the user uses in the give group
     *
     * @param groupId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Pair<String, String> getFakeIdentity(String groupId)
            throws DbException, FormatException;

    /**
     * Returns the pseudo-identifier and pseudonym the user uses in the give group
     *
     * @param txn
     * @param groupId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Pair<String, String> getFakeIdentity(T txn, String groupId)
            throws DbException, FormatException;

    /**
     * Stores a new Group Invitation
     *
     * @param txn
     * @param groupInvitation
     * @throws DbException
     */
    void addGroupInvitation(T txn, GroupInvitation groupInvitation)
            throws DbException;

    /**
     * Stores a new Group Invitation
     *
     * @param groupInvitation
     * @throws DbException
     */
    void addGroupInvitation(GroupInvitation groupInvitation)
            throws DbException;

    /**
     * Deletes Group Invitation
     *
     * @param contactId
     * @param groupId
     * @throws DbException
     */
    void removeGroupInvitation(ContactId contactId, String groupId)
            throws DbException;

    /**
     * Deletes Group Invitation
     *
     * @param txn
     * @param contactId
     * @param groupId
     * @throws DbException
     */
    void removeGroupInvitation(T txn,
                               ContactId contactId, String groupId)
            throws DbException;

    /**
     * Returns all Group Invitations
     *
     * @return
     * @throws DbException
     */
    Collection<GroupInvitation> getGroupInvitations()
            throws DbException;

    /**
     * Returns true if the user is allowed to invite members to the given group
     *
     * @param groupId
     * @param groupType
     * @return
     * @throws FormatException
     * @throws DbException
     */
    boolean isInvitationAllowed(String groupId, GroupType groupType)
            throws FormatException, DbException;
}
