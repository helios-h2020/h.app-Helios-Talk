package eu.h2020.helios_social.happ.helios.talk.forum.conversation;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.membership.ForumMembershipManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static java.util.logging.Level.WARNING;

public class ForumControllerImpl extends DbControllerImpl implements ForumController {
    private static final Logger LOG =
            Logger.getLogger(ForumControllerImpl.class.getName());

    private final GroupManager groupManager;
    private final ForumMembershipManager forumMembershipManager;

    @Inject
    ForumControllerImpl(@DatabaseExecutor Executor dbExecutor, LifecycleManager lifecycleManager,
                        GroupManager groupManager, ForumMembershipManager forumMembershipManager) {
        super(dbExecutor, lifecycleManager);
        this.groupManager = groupManager;
        this.forumMembershipManager = forumMembershipManager;
    }

    @Override
    public void revealSelf(String groupId, boolean doReveal, ExceptionHandler<Exception> handler) {
        runOnDbThread(() -> {
            try {
                long start = now();
                groupManager.revealSelf(groupId, doReveal);
                logDuration(LOG, "Reveal Self to Forum", start);
            } catch (DbException | FormatException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    public boolean isIdentityRevealed(String groupId) throws DbException, FormatException {
        return groupManager.identityRevealed(groupId);
    }

    @Override
    public void leaveForum(Forum forum, ExceptionHandler<Exception> handler) {
        runOnDbThread(() -> {
            try {
                long start = now();
                forumMembershipManager.leaveForum(forum);
                logDuration(LOG, "Leaving Forum...", start);
            } catch (DbException | FormatException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });

    }
}
