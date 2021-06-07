package eu.h2020.helios_social.happ.helios.talk.share;

import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

public abstract class ShareContentChatItem {

    private String title;
    private GroupType type;
    private long lstTimestamp;
    private String groupId;
    private String contextId;

    public ShareContentChatItem(String groupId, String contextId, String title, GroupType type) {
        this.groupId = groupId;
        this.contextId = contextId;
        this.title = title;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public GroupType getType() {
        return type;
    }

    public long getLstTimestamp() {
        return lstTimestamp;
    }

    public void setLstTimestamp(long lstTimestamp) {
        this.lstTimestamp = lstTimestamp;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getContextId() {
        return contextId;
    }

    public abstract int getIconResourceId();
}
