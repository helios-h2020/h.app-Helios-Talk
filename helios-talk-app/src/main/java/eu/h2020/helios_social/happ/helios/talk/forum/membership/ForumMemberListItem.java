package eu.h2020.helios_social.happ.helios.talk.forum.membership;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMember;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerId;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
class ForumMemberListItem {

    private final ForumMember forumMember;
    private boolean online;

    ForumMemberListItem(ForumMember forumMember) {
        this.forumMember = forumMember;
        this.online = online;
    }

    ForumMember getMember() {
        return forumMember;
    }

    String getAlias() {
        return forumMember.getAlias();
    }

    PeerId getPeerId() {
        return forumMember.getPeerId();
    }

    ForumMemberRole getRole() {
        return forumMember.getRole();
    }

}
