package eu.h2020.helios_social.happ.helios.talk.share;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;

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
            return R.drawable.ic_community_white;
        } else {
            return R.drawable.ic_group_white;
        }
    }
}
