package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.view.View;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
interface ConversationListener {

	//void respondToRequest(ConversationRequestItem item, boolean accept);

	//void openRequestedShareable(ConversationRequestItem item);

	/*void onAttachmentClicked(View view, ConversationMessageItem messageItem,
			AttachmentItem attachmentItem);*/

	void onFavouriteClicked(View view, ConversationMessageItem messageItem);

}
