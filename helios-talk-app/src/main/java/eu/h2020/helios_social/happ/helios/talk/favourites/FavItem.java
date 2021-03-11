package eu.h2020.helios_social.happ.helios.talk.favourites;


import androidx.annotation.LayoutRes;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationItem;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils.toHexString;


@NotThreadSafe
@NotNullByDefault
public class FavItem extends ConversationItem {

    private String author;

    public FavItem(int layoutRes, String author, String text, String messageId, long time) {
        super(layoutRes, text, messageId, time);
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "FavItem{" +
                "author='" + author + '\'' +
                ", text='" + text + '\'' +
                ", messageId='" + messageId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", time=" + time +
                ", isIncoming=" + isIncoming +
                ", favourite=" + favourite +
                ", state=" + state +
                ", list=" + list +
                '}';
    }
}
