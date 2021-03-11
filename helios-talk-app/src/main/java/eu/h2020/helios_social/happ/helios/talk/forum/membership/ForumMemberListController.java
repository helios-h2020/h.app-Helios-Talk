package eu.h2020.helios_social.happ.helios.talk.forum.membership;

import java.util.Collection;

import eu.h2020.helios_social.happ.helios.talk.controller.DbController;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMember;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;

public interface ForumMemberListController extends DbController {

    void loadMembers(String groupId,
                     ResultExceptionHandler<Collection<ForumMemberListItem>, DbException> handler);

    void updateMemberRole(String groupId, ForumMember forumMember, ForumMemberRole newRole, ExceptionHandler<DbException> handler);
}
