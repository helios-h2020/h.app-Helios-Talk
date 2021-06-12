package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.view.View;

import androidx.annotation.UiThread;

import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
public interface ConversationListener {

    void onImageClicked(View view, ConversationItem messageItem,
                        AttachmentItem attachmentItem);

    void onFavouriteClicked(View view, ConversationItem messageItem);

    void onSharedContactClicked(View view, ConversationItem messageItem);

    void onFileClicked(View view, AttachmentItem attachmentItem);

}
