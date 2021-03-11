package eu.h2020.helios_social.happ.helios.talk.forum.conversation;

import eu.h2020.helios_social.happ.helios.talk.controller.handler.ExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;

public interface ForumController {

    void revealSelf(String groupId, boolean doReveal, ExceptionHandler<Exception> handler);

    boolean isIdentityRevealed(String groupId) throws DbException, FormatException;

    void leaveForum(Forum forum, ExceptionHandler<Exception> handler);
}
