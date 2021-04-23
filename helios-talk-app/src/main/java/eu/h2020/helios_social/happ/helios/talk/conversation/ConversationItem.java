package eu.h2020.helios_social.happ.helios.talk.conversation;


import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import androidx.annotation.LayoutRes;

import java.util.ArrayList;
import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageState;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils.toHexString;

@NotThreadSafe
@NotNullByDefault
public abstract class ConversationItem {

    @LayoutRes
    private final int layoutRes;
    @Nullable
    protected String text;
    protected final String messageId;
    protected final String groupId;
    protected final long time;
    protected final boolean isIncoming;
    protected boolean favourite;
    protected MessageState state;
    protected List<AttachmentItem> list;
    protected int index;

    public ConversationItem(@LayoutRes int layoutRes, MessageHeader h) {
        this.layoutRes = layoutRes;
        this.text = null;
        this.messageId = h.getMessageId();
        this.groupId = h.getGroupId();
        this.time = h.getTimestamp();
        this.state = h.getMessageState();
        this.isIncoming = h.isIncoming();
        this.favourite = h.isFavourite();
        this.list = new ArrayList();

    }

    public ConversationItem(@LayoutRes int layoutRes, String text, String messageId, long time) {
        this.layoutRes = layoutRes;
        this.text = text;
        this.messageId = messageId;
        this.time = time;
        this.list = new ArrayList();
        this.groupId = null;
        this.isIncoming = true;
    }

    @LayoutRes
    public int getLayout() {
        return layoutRes;
    }

    public String getId() {
        return messageId;
    }

    public String getKey() {
        return toHexString(messageId.getBytes());
    }

    public String getGroupId() {
        return groupId;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public long getTime() {
        return time;
    }

    /**
     * Only useful for incoming messages.
     */
    public boolean isRead() {
        return state.equals(MessageState.SEEN);
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void markRead() {
        if (isIncoming) {
            state = MessageState.SEEN;
        }
    }

    /**
     * Only useful for outgoing messages.
     */
    public boolean isSent() {
        return (state.equals(MessageState.DELIVERED) ||
                state.equals(MessageState.SEEN));
    }

    /**
     * Only useful for outgoing messages.
     */
    public void setSent(boolean sent) {
        this.state = MessageState.DELIVERED;
    }

    /**
     * Only useful for outgoing messages.
     */
    public boolean isSeen() {
        return state.equals(MessageState.SEEN);
    }

    /**
     * Only useful for outgoing messages.
     */
    public void setSeen(boolean seen) {
        this.state = MessageState.SEEN;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    public void setAttachmentList(List<AttachmentItem> attachmentItems) {
        this.list = attachmentItems;
    }

    public List<AttachmentItem> getAttachmentList() {
        return list;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
