package eu.h2020.helios_social.happ.helios.talk.search;

import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;

public interface ResultsActionListener {

    void onJoinPublicForum(Forum forum);

    void onRequestJoinProtectedForum(Forum forum) throws DbException;

    void onOpenPublicForum(Forum forum);
}
