package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.Context;


import javax.annotation.Nullable;

import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;

import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationMessageVisitor;

import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message.Type.IMAGES;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message.Type.TEXT;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message.Type.VIDEOCALL;
import static java.util.Collections.emptyList;

@UiThread
@NotNullByDefault
class ConversationVisitor implements
        ConversationMessageVisitor<ConversationItem> {

    private final Context ctx;
    private final TextCache textCache;
    private final AttachmentCache attachmentCache;
    private final LiveData<String> contactName;

    ConversationVisitor(Context ctx, TextCache textCache,
                        AttachmentCache attachmentCache, LiveData<String> contactName) {
        this.ctx = ctx;
        this.textCache = textCache;
        this.attachmentCache = attachmentCache;
        this.contactName = contactName;
    }

    @Override
    public ConversationItem visitMessageHeader(MessageHeader h) {
        ConversationItem item;

        if (!h.isIncoming()) {
            if (h.getMessageType() == VIDEOCALL) {
                String text = ctx.getString(R.string.video_request_sent,
                        contactName.getValue());
                item = new VideoCallConversationItem(
                        R.layout.list_item_conversation_videocall_notice_out,
                        text,
                        h);
            } else if (h.getMessageType() == IMAGES) {
                item = new ConversationMessageItem(
                        R.layout.list_item_conversation_msg_out,
                        h);
            } else {
                item = new ConversationMessageItem(
                        R.layout.list_item_conversation_msg_out, h);
            }
        } else {
            if (h.getMessageType() == VIDEOCALL) {
                String text = ctx.getString(R.string.video_request_received,
                        contactName.getValue());
                item = new VideoCallConversationItem(
                        R.layout.list_item_conversation_videocall_notice_in,
                        text,
                        h);
            } else if (h.getMessageType() == IMAGES) {
                item = new ConversationMessageItem(
                        R.layout.list_item_conversation_msg_in,
                        h);
            } else {
                item = new ConversationMessageItem(
                        R.layout.list_item_conversation_msg_in,
                        h);
            }
        }
        String text;
        if (h.getMessageType() == IMAGES) {
            List<AttachmentItem> attachments = attachmentCache.getAttachments(h.getMessageId());
            ((ConversationMessageItem) item).setAttachmentList(attachments);
        }
        if (h.hasText()) {
            text = textCache.getText(h.getMessageId());
            if (text != null && h.getMessageType() != VIDEOCALL) item.setText(text);
            if (text != null && h.getMessageType() == VIDEOCALL) {
                ((VideoCallConversationItem) item).setRoomId(text);
            }
        } else {
            item.setText("");
        }

        return item;
    }

    interface TextCache {
        @Nullable
        String getText(String messageId);
    }

    interface AttachmentCache {
        List<AttachmentItem> getAttachments(String messageId);
    }
}
