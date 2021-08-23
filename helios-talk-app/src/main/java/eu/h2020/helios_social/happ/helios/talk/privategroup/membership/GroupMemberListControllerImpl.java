package eu.h2020.helios_social.happ.helios.talk.privategroup.membership;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.forum.membership.ForumMemberListController;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMember;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroupManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumManager;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMember;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.membership.ForumMembershipManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;

public class GroupMemberListControllerImpl extends DbControllerImpl
        implements GroupMemberListController {

    private static final Logger LOG =
            Logger.getLogger(GroupMemberListControllerImpl.class.getName());

    private final PrivateGroupManager groupManager;

    @Inject
    GroupMemberListControllerImpl(@DatabaseExecutor Executor dbExecutor,
                                  LifecycleManager lifecycleManager,
                                  PrivateGroupManager groupManager) {
        super(dbExecutor, lifecycleManager);
        this.groupManager = groupManager;
    }

    @Override
    public void loadMembers(String groupId,
                            ResultExceptionHandler<Collection<GroupMemberListItem>, DbException> handler) {
        runOnDbThread(() -> {
            try {
                Collection<GroupMemberListItem> items = new ArrayList<>();
                Collection<GroupMember> members =
                        groupManager.getMembers(groupId);
                for (GroupMember m : members) {
                    items.add(new GroupMemberListItem(m));
                }
                handler.onResult(items);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            } catch (FormatException ex) {
                logException(LOG, WARNING, ex);
            }
        });
    }

}
