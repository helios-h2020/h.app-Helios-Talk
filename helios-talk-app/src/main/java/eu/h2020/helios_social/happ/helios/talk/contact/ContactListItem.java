package eu.h2020.helios_social.happ.helios.talk.contact;


import javax.annotation.concurrent.NotThreadSafe;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.chat.ChatItem;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupCount;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageState;

@NotThreadSafe
@NotNullByDefault
public class ContactListItem extends ContactItem implements ChatItem {

	private long timestamp;
	private String groupId;
	private long future_timestamp;
	private int unread;
	private int messageCount;
	private String lastMessage;
	private boolean favourite;
	private int weight;

	public ContactListItem(Contact contact, String groupId, boolean connected) {
		super(contact, connected);
		this.groupId = groupId;
		this.favourite = false;
		this.weight = 0;
		this.future_timestamp = 4117849822000l;
	}

	public ContactListItem(Contact contact, String groupId, boolean connected,
			GroupCount count) {
		super(contact, connected);
		this.groupId = groupId;
		this.unread = count.getUnreadCount();
		this.timestamp = count.getLatestMsgTime();
		this.messageCount = count.getMsgCount();
		this.favourite = false;
		this.weight = 0;
		this.future_timestamp = 4117849822000l;
	}

	public void addMessage(MessageHeader h) {
		messageCount++;
		if (h.getTimestamp() > timestamp) timestamp = h.getTimestamp();
		if (!h.getMessageState().equals(MessageState.SEEN)) unread++;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public String getName() {
		return contact.getAlias();
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
		return favourite ? future_timestamp + weight : timestamp;
	}

	public long getRealTimestamp() {
		return timestamp;
	}

	@Override
	public int getUnreadCount() {
		return unread;
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
	public String getGroupId() {
		return groupId;
	}

	@Override
	public boolean isFavourite() {
		return favourite;
	}

	@Override
	public void setFavourite(boolean isFavourite) {
		this.favourite = isFavourite;
	}

}
