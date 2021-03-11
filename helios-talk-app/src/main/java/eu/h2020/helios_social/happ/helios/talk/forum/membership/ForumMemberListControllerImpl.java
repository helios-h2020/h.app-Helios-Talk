package eu.h2020.helios_social.happ.helios.talk.forum.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

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

public class ForumMemberListControllerImpl extends DbControllerImpl
        implements ForumMemberListController {

    private static final Logger LOG =
            Logger.getLogger(ForumMemberListControllerImpl.class.getName());

    private final ForumManager forumManager;
    private final ForumMembershipManager forumMembershipManager;

    @Inject
    ForumMemberListControllerImpl(@DatabaseExecutor Executor dbExecutor,
                                  LifecycleManager lifecycleManager,
                                  ForumManager forumManager,
                                  ForumMembershipManager forumMembershipManager) {
        super(dbExecutor, lifecycleManager);
        this.forumManager = forumManager;
        this.forumMembershipManager = forumMembershipManager;
    }

    @Override
    public void loadMembers(String groupId,
                            ResultExceptionHandler<Collection<ForumMemberListItem>, DbException> handler) {
        runOnDbThread(() -> {
            try {
                Collection<ForumMemberListItem> items = new ArrayList<>();
                Collection<ForumMember> members =
                        forumManager.getForumMembers(groupId);
                for (ForumMember m : members) {
                    items.add(new ForumMemberListItem(m));
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

    @Override
    public void updateMemberRole(String groupId, ForumMember forumMember, ForumMemberRole newRole, ExceptionHandler<DbException> handler) {
        runOnDbThread(() -> {
            try {
                Forum forum = forumManager.getForum(groupId);
                forumMembershipManager.updateForumMemberRole(forum, forumMember, newRole);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            } catch (FormatException ex) {
                logException(LOG, WARNING, ex);
            }
        });
    }
}
