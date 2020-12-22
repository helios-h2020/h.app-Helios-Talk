package eu.h2020.helios_social.happ.helios.talk.conversation;


import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;

public class VideoCallConversationItem extends ConversationItem {

	private String room_id;
	private String prompt_text;

	VideoCallConversationItem(int layoutRes, String prompt_text,
			MessageHeader h) {
		super(layoutRes, h);
		this.prompt_text = prompt_text;
	}

	public void setRoomId(String room_id) {
		this.room_id = room_id;
	}

	public String getRoomId() {
		return this.room_id;
	}

	public String getPromptText() {
		return prompt_text;
	}
}
