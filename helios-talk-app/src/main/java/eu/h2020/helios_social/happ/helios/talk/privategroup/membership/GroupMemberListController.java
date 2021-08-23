package eu.h2020.helios_social.happ.helios.talk.privategroup.membership;

import java.util.Collection;

import eu.h2020.helios_social.happ.helios.talk.controller.DbController;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;


public interface GroupMemberListController extends DbController {

    void loadMembers(String groupId,
                     ResultExceptionHandler<Collection<GroupMemberListItem>, DbException> handler);

}
