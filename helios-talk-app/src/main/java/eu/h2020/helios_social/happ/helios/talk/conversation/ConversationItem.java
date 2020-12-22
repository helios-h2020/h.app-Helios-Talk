package eu.h2020.helios_social.happ.helios.talk.conversation;


import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import androidx.annotation.LayoutRes;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageState;

import static eu.h2020.helios_social.happ.helios.talk.api.util.StringUtils.toHexString;

@NotThreadSafe
@NotNullByDefault
public abstract class ConversationItem {

	@LayoutRes
	private final int layoutRes;
	@Nullable
	protected String text;
	private final String messageId;
	private final String groupId;
	private final long time;
	private final boolean isIncoming;
	private boolean favourite;
	private MessageState state;

	public ConversationItem(@LayoutRes int layoutRes, MessageHeader h) {
		this.layoutRes = layoutRes;
		this.text = null;
		this.messageId = h.getMessageId();
		this.groupId = h.getGroupId();
		this.time = h.getTimestamp();
		this.state = h.getMessageState();
		this.isIncoming = h.isIncoming();
		this.favourite = h.isFavourite();
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

}
