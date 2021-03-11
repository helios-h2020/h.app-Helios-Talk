package eu.h2020.helios_social.happ.helios.talk.attachment;

import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.List;

import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface AttachmentRetriever {

    void cachePut(String messageId, List<AttachmentItem> attachments);

    @Nullable
    List<AttachmentItem> cacheGet(String messageId);

    List<AttachmentItem> getMessageAttachments(String messageId) throws DbException;

}
