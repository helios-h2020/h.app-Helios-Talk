package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;


import javax.annotation.concurrent.NotThreadSafe;

import androidx.annotation.LayoutRes;
import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationItem;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;

@UiThread
@NotThreadSafe
public class GroupMessageItem extends ConversationItem {

	private final PeerInfo peerInfo;

	public GroupMessageItem(@LayoutRes int layoutRes, GroupMessageHeader h,
			String text) {
		super(layoutRes, h);
		this.peerInfo = h.getPeerInfo();
	}

	public PeerInfo getPeerInfo() {
		return peerInfo;
	}

	@LayoutRes
	public int getLayout() {
		return R.layout.list_item_group_conversation_msg;
	}

}
