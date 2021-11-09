package eu.h2020.helios_social.happ.helios.talk.share;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;

import static eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType.ProtectedForum;
import static eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType.PublicForum;
import static eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType.SecretForum;

public class ShareContentGroupChatItem extends ShareContentChatItem {

    private Group group;

    public ShareContentGroupChatItem(Forum forum) {
        super(forum.getId(), forum.getContextId(), forum.getName(), forum.getGroupType());
        this.group = forum;
    }

    public ShareContentGroupChatItem(PrivateGroup group) {
        super(group.getId(), group.getContextId(), group.getName(), group.getGroupType());
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public int getIconResourceId() {
        if (group instanceof Forum) {
            if (group.getGroupType().equals(PublicForum)) return R.drawable.ic_public_forum;
            else if (group.getGroupType().equals(ProtectedForum)) return R.drawable.ic_protected_forum;
            else if (group.getGroupType().equals( SecretForum)) return R.drawable.ic_secret_forum;
            return R.drawable.ic_community_white;
        } else {
            return R.drawable.ic_group_white;
        }
    }
}
