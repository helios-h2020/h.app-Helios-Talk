package eu.h2020.helios_social.happ.helios.talk.group;

import android.content.Context;

import androidx.annotation.UiThread;

import java.util.List;

import javax.annotation.Nullable;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationMessageVisitor;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;

import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message.Type.IMAGES;

@UiThread
@NotNullByDefault
public class GroupConversationVisitor implements
        ConversationMessageVisitor<GroupMessageItem> {

    private final Context ctx;
    private final AttachmentCache attachmentCache;
    private final TextCache textCache;

    public GroupConversationVisitor(Context ctx, TextCache textCache,
                                    AttachmentCache attachmentCache) {
        this.ctx = ctx;
        this.textCache = textCache;
        this.attachmentCache = attachmentCache;
    }

    @Override
    public GroupMessageItem visitMessageHeader(MessageHeader h) {
        GroupMessageItem item = new GroupMessageItem(
                R.layout.list_item_group_conversation_msg,
                (GroupMessageHeader) h, null);

        if (h.getMessageType() == IMAGES) {
            List<AttachmentItem> attachments = attachmentCache.getAttachments(h.getMessageId());
            item.setAttachmentList(attachments);
        }
        if (h.hasText()) {
            String text = textCache.getText(h.getMessageId());
            item.setText(text);
        }

        return item;
    }

    public interface TextCache {
        @Nullable
        String getText(String messageId);
    }

    public interface AttachmentCache {
        List<AttachmentItem> getAttachments(String messageId);
    }
}
