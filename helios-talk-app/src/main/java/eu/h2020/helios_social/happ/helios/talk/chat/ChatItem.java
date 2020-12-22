package eu.h2020.helios_social.happ.helios.talk.chat;

public interface ChatItem {

	String getGroupId();

	boolean isEmpty();

	int getMessageCount();

	long getTimestamp();

	int getUnreadCount();

	String getName();

	String getLastMessageText();

	void setLastMessageText(String lastMessage);

	boolean isFavourite();

	void setFavourite(boolean isFavourite);
}
