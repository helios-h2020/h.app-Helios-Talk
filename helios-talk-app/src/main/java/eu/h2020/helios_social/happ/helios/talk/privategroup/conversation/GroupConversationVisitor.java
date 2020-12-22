package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import android.content.Context;

import javax.annotation.Nullable;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationMessageVisitor;

@UiThread
@NotNullByDefault
class GroupConversationVisitor implements
		ConversationMessageVisitor<GroupMessageItem> {

	private final Context ctx;
	private final TextCache textCache;

	GroupConversationVisitor(Context ctx, TextCache textCache) {
		this.ctx = ctx;
		this.textCache = textCache;
	}

	@Override
	public GroupMessageItem visitMessageHeader(MessageHeader h) {
		String text = textCache.getText(h.getMessageId());
		GroupMessageItem item = new GroupMessageItem(
				R.layout.list_item_group_conversation_msg,
				(GroupMessageHeader) h, text);
		return item;
	}

	interface TextCache {
		@Nullable
		String getText(String messageId);
	}
}
