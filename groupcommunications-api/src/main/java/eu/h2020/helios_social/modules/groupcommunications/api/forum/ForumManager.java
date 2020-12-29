package eu.h2020.helios_social.modules.groupcommunications.api.forum;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import eu.h2020.helios_social.modules.groupcommunications.api.context.utils.Datespan;
import eu.h2020.helios_social.modules.groupcommunications.api.context.utils.Daytime;
import eu.h2020.helios_social.modules.groupcommunications.api.context.utils.Season;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;

/**
 * Interface for Forum Manager
 */
public interface ForumManager<T> {

    /**
     * Stores a new forum, the user subscribed/created
     *
     * @param forum
     * @param type
     * @param isAdministrator
     */
    void addForum(Forum forum, ForumType type, boolean isAdministrator)
            throws FormatException, DbException;

    /**
     * Stores a new forum, the user subscribed/created
     *
     * @param txn
     * @param forum
     * @param type
     * @param isAdministrator
     */
    void addForum(T txn, Forum forum, ForumType type, boolean isAdministrator)
            throws FormatException, DbException;

    /**
     * Returns the forum with the given identifier     *
     *
     * @param groupId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Forum getForum(String groupId) throws DbException, FormatException;

    /**
     * Returns the forum with the given identifier
     *
     * @param txn
     * @param groupId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Forum getForum(T txn, String groupId) throws DbException, FormatException;

    /**
     * Returns all forums the user has subscribed to
     *
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<Forum> getForums() throws DbException, FormatException;

    /**
     * Returns all forums the user has subscribed to
     *
     * @param txn
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<Forum> getForums(T txn)
            throws DbException, FormatException;

    /**
     * Returns all forums in the given context, the user has subscribed
     *
     * @param contextId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<Forum> getForums(String contextId)
            throws DbException, FormatException;

    /**
     * Returns all forums in the given context, the user has subscribed
     *
     * @param txn
     * @param contextId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<Forum> getForums(T txn, String contextId)
            throws DbException, FormatException;

    /**
     * Returns all members of the given Forum
     *
     * @param groupId
     * @return a collection of Forum Member
     */
    Collection<ForumMember> getForumMembers(String groupId)
            throws DbException, FormatException;

    /**
     * Returns all members of the given Forum
     *
     * @param txn
     * @param groupId
     * @return a collection of Forum Member
     * @throws DbException
     * @throws FormatException
     */
    Collection<ForumMember> getForumMembers(T txn,
                                            String groupId)
            throws DbException, FormatException;

    /**
     * Returns user's role in the given forum
     *
     * @param groupId
     * @return
     */
    ForumMemberRole getRole(String groupId) throws DbException, FormatException;

    /**
     * Returns user's role in the given forum
     *
     * @param txn
     * @param groupId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    ForumMemberRole getRole(T txn, String groupId)
            throws DbException, FormatException;

    /**
     * Returns user’s fake identifier in the forum with the given identifier
     *
     * @param txn
     * @param groupId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    String getFakeId(T txn, String groupId)
            throws DbException, FormatException;

    /**
     * Updates user’s Forum Member Role in the forum with the given identifier
     *
     * @param groupId
     * @param newRole
     * @throws DbException
     * @throws FormatException
     */
    void updateRole(String groupId,
                    ForumMemberRole newRole)
            throws DbException, FormatException;

    /**
     * Updates user’s Forum Member Role in the forum with the given identifier
     *
     * @param txn
     * @param groupId
     * @param newRole
     * @throws DbException
     * @throws FormatException
     */
    void updateRole(T txn, String groupId,
                    ForumMemberRole newRole)
            throws DbException, FormatException;

    /**
     * Updates in the forum member list the Role of the forum member.
     *
     * @param forumMember
     * @throws DbException
     */
    void updateForumMemberRole(ForumMember forumMember)
            throws DbException;

    /**
     * Updates in the forum member list the Role of the forum member.
     *
     * @param txn
     * @param forumMember
     * @throws DbException
     */
    void updateForumMemberRole(T txn, ForumMember forumMember)
            throws DbException;

    /**
     * Adds a new member in the forum member list
     *
     * @param forumMember
     * @throws DbException
     */
    void addForumMember(ForumMember forumMember)
            throws DbException;

    /**
     * Adds a new member in the forum member list
     *
     * @param txn
     * @param forumMember
     * @throws DbException
     */
    void addForumMember(T txn, ForumMember forumMember)
            throws DbException;

    /**
     * Adds a collection of members in the forum member list
     *
     * @param forumMembers
     * @throws DbException
     */
    void addForumMembers(Collection<ForumMember> forumMembers)
            throws DbException;

    /**
     * Adds a collection of members in the forum member list
     *
     * @param txn
     * @param forumMembers
     * @throws DbException
     */
    void addForumMembers(T txn,
                         Collection<ForumMember> forumMembers)
            throws DbException;

    /**
     * Adds a new peer in the moderator list of the forum given with the given identifier
     *
     * @param groupId
     * @param moderator
     * @throws DbException
     * @throws FormatException
     */
    void addModerator(String groupId, String moderator)
            throws DbException, FormatException;

    /**
     * Adds a new peer in the moderator list of the forum given with the given identifier
     *
     * @param txn
     * @param groupId
     * @param moderator
     * @throws DbException
     * @throws FormatException
     */
    void addModerator(T txn, String groupId,
                      String moderator) throws DbException, FormatException;

    /**
     * Adds a new member in the forum member list
     *
     * @param forumMember
     * @throws DbException
     */
    void removeForumMember(ForumMember forumMember)
            throws DbException;

    /**
     * Adds a new member in the forum member list
     *
     * @param txn
     * @param forumMember
     * @throws DbException
     */
    void removeForumMember(T txn, ForumMember forumMember)
            throws DbException;

    /**
     * Removes a peer from the moderator list of the forum with the given identifier
     *
     * @param groupId
     * @param moderator
     * @throws DbException
     * @throws FormatException
     */
    void removeModerator(String groupId, String moderator)
            throws DbException, FormatException;

    /**
     * Removes a peer from the moderator list of the forum with the given identifier
     *
     * @param txn
     * @param groupId
     * @param moderator
     * @throws DbException
     * @throws FormatException
     */
    void removeModerator(T txn, String groupId,
                         String moderator) throws DbException, FormatException;

    /**
     * Deletes the member list of the forum with the given identifier
     *
     * @param groupId
     * @throws DbException
     * @throws FormatException
     */
    void removeForumMemberList(String groupId)
            throws DbException, FormatException;
}
