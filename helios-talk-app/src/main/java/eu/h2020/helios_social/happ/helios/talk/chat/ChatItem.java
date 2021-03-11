package eu.h2020.helios_social.happ.helios.talk.chat;

import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;

public interface ChatItem {

    void addMessage(MessageHeader messageHeader);

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
