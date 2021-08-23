package eu.h2020.helios_social.happ.helios.talk.privategroup.membership;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMember;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMember;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerId;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
class GroupMemberListItem {

    private final GroupMember groupMember;

    GroupMemberListItem(GroupMember groupMember) {
        this.groupMember = groupMember;
    }

    GroupMember getMember() {
        return groupMember;
    }

    String getAlias() {
        return groupMember.getAlias();
    }


    PeerId getPeerId() {
        return groupMember.getPeerId();
    }

    byte[] getProfilePic() {return groupMember.getProfilePic();}
}
