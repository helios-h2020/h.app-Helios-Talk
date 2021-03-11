package eu.h2020.helios_social.happ.helios.talk.forum;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.chat.ChatItem;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupCount;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageState;

@NotNullByDefault
public class ForumItem implements ChatItem {

    private final Forum forum;
    private int messageCount, unreadCount;
    private long timestamp;
    private boolean isFavourite;
    private String lastMessage;
    private String groupId;

    public ForumItem(Forum forum, GroupCount count) {
        this.forum = forum;
        this.messageCount = count.getMsgCount();
        this.unreadCount = count.getUnreadCount();
        this.timestamp = count.getLatestMsgTime();
        this.isFavourite = false;
        this.groupId = forum.getId();
    }

    @Override
    public void addMessage(MessageHeader h) {
        messageCount++;
        if (h.getTimestamp() > timestamp) {
            timestamp = h.getTimestamp();
        }
        if ((!h.getMessageState().equals(MessageState.SEEN))) {
            unreadCount++;
        }
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    public Forum getForum() {
        return forum;
    }

    public boolean isEmpty() {
        return messageCount == 0;
    }

    @Override
    public int getMessageCount() {
        return 0;
    }

    public int getPostCount() {
        return messageCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    @Override
    public String getName() {
        return forum.getName();
    }

    @Override
    public String getLastMessageText() {
        return this.lastMessage;
    }

    @Override
    public void setLastMessageText(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public boolean isFavourite() {
        return isFavourite;
    }

    @Override
    public void setFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }
}
