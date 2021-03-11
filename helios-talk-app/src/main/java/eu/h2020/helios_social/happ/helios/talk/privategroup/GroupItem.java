package eu.h2020.helios_social.happ.helios.talk.privategroup;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.chat.ChatItem;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupCount;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageState;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;

@NotNullByDefault
public class GroupItem implements ChatItem {

	private final PrivateGroup privateGroup;
	private int messageCount, unreadCount;
	private long timestamp;
	private boolean dissolved;
	private String lastMessage;
	private boolean favourite;

	public GroupItem(PrivateGroup privateGroup,
			GroupCount count, boolean dissolved) {
		this.privateGroup = privateGroup;
		this.messageCount = count.getMsgCount();
		this.unreadCount = count.getUnreadCount();
		this.timestamp = count.getLatestMsgTime();
		this.dissolved = dissolved;
		this.favourite = false;
	}

	@Override
	public void addMessage(MessageHeader header) {
		messageCount++;
		if (header.getTimestamp() > timestamp) {
			timestamp = header.getTimestamp();
		}
		if ((!header.getMessageState().equals(MessageState.SEEN))) {
			unreadCount++;
		}
	}

	public String getGroupId() {
		return privateGroup.getId();
	}

	@Override
	public String getName() {
		return privateGroup.getName();
	}

	@Override
	public String getLastMessageText() {
		return lastMessage;
	}

	@Override
	public void setLastMessageText(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	@Override
	public boolean isFavourite() {
		return favourite;
	}

	@Override
	public void setFavourite(boolean isFavourite) {
		this.favourite = isFavourite;
	}

	@Override
	public boolean isEmpty() {
		return messageCount == 0;
	}

	@Override
	public int getMessageCount() {
		return messageCount;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int getUnreadCount() {
		return unreadCount;
	}

	public boolean isDissolved() {
		return dissolved;
	}

	public void setDissolved() {
		dissolved = true;
	}

}
