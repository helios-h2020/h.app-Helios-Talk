package eu.h2020.helios_social.happ.helios.talk.conversation;


import javax.annotation.concurrent.NotThreadSafe;

import androidx.annotation.LayoutRes;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;

@NotThreadSafe
@NotNullByDefault
class ConversationMessageItem extends ConversationItem {

	ConversationMessageItem(@LayoutRes int layoutRes, MessageHeader h) {
		super(layoutRes, h);
	}


}
