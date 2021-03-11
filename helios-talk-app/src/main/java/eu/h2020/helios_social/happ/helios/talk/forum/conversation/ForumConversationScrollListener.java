package eu.h2020.helios_social.happ.helios.talk.forum.conversation;

import eu.h2020.helios_social.happ.helios.talk.group.GroupConversationAdapter;
import eu.h2020.helios_social.happ.helios.talk.group.GroupMessageItem;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerViewScrollListener;

class ForumConversationScrollListener extends
		HeliosTalkRecyclerViewScrollListener<GroupConversationAdapter, GroupMessageItem> {

	private final ForumConversationViewModel viewModel;

	public ForumConversationScrollListener(GroupConversationAdapter adapter,
                                           ForumConversationViewModel viewModel) {
		super(adapter);
		this.viewModel = viewModel;
	}

	@Override
	protected void onItemVisible(GroupMessageItem item) {
		if (!item.isRead()) {
			viewModel.markMessageRead(item.getGroupId(), item.getId());
			item.markRead();

		}
	}
}
