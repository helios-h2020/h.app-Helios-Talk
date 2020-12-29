package eu.h2020.helios_social.modules.groupcommunications.api.forum.membership;

import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMember;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;

public interface ForumMembershipManager {

    /**
     * Removes forum from the database
     *
     * @param forum
     * @throws DbException
     * @throws FormatException
     */
    void leaveForum(Forum forum) throws DbException, FormatException;

    /**
     * Updates the role of a Forum Member
     *
     * @param forum
     * @param forumMember
     * @param updatedRole
     * @throws DbException
     * @throws FormatException
     */
    void updateForumMemberRole(Forum forum, ForumMember forumMember,
                               ForumMemberRole updatedRole) throws DbException, FormatException;
}
