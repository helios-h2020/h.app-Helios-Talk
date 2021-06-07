package eu.h2020.helios_social.happ.helios.talk.conversation;

import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;

public class SharedContactConversationItem extends ConversationItem {

    private String prompt_text;
    private PeerInfo peerInfo;

    public SharedContactConversationItem(int layoutRes, String prompt_text, MessageHeader h) {
        super(layoutRes, h);
        this.prompt_text = prompt_text;
    }

    public String getPromptText() {
        return prompt_text;
    }

    public PeerInfo getPeerInfo() {
        return peerInfo;
    }

    public void setPeerInfo(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
    }
}
