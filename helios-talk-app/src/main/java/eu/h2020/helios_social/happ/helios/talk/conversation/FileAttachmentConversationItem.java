package eu.h2020.helios_social.happ.helios.talk.conversation;

import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;

public class FileAttachmentConversationItem extends ConversationMessageItem {

    private String promptText;

    public FileAttachmentConversationItem(int layoutRes, MessageHeader h, String promptText) {
        super(layoutRes, h);
        this.promptText = promptText;
    }

    public String getPromptText() {
        return promptText;
    }

}
