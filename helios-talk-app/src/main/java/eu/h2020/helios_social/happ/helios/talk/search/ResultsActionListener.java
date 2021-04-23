package eu.h2020.helios_social.happ.helios.talk.search;

import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;

public interface ResultsActionListener {

    void onJoinPublicForum(Forum forum);

    void onOpenPublicForum(Forum forum);
}
