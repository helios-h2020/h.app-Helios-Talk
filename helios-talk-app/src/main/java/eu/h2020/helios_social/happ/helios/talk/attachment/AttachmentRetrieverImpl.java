package eu.h2020.helios_social.happ.helios.talk.attachment;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.inject.Inject;

import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import static java.util.logging.Logger.getLogger;

@NotNullByDefault
public class AttachmentRetrieverImpl implements AttachmentRetriever {

    private static final Logger LOG =
            getLogger(AttachmentRetrieverImpl.class.getName());

    private final ConversationManager conversationManager;

    private final Map<String, List<AttachmentItem>> attachmentCache =
            new ConcurrentHashMap<>();

    @Inject
    public AttachmentRetrieverImpl(ConversationManager conversationManager) {
        this.conversationManager = conversationManager;
    }

    @Override
    public void cachePut(String messageId,
                         List<AttachmentItem> attachmentsItems) {
        attachmentCache.put(messageId, attachmentsItems);
    }

    @Override
    @Nullable
    public List<AttachmentItem> cacheGet(String messageId) {
        return attachmentCache.get(messageId);
    }

    @Override
    public List<AttachmentItem> getMessageAttachments(String messageId)
            throws DbException {
        if (!attachmentCache.containsKey(messageId)) {
            List<AttachmentItem> attachmentItems = new ArrayList<>();
            for (Attachment a : conversationManager.getAttachments(messageId)) {
                attachmentItems.add(new AttachmentItem(Uri.parse(a.getUri()), a.getContentType(), a.getUrl()));
            }
            cachePut(messageId, attachmentItems);
        }
        return attachmentCache.get(messageId);
    }
}
