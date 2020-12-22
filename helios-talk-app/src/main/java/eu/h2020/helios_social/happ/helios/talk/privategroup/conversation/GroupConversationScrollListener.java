package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerViewScrollListener;

class GroupConversationScrollListener extends
		HeliosTalkRecyclerViewScrollListener<GroupConversationAdapter, GroupMessageItem> {

	private final GroupConversationViewModel viewModel;

	public GroupConversationScrollListener(GroupConversationAdapter adapter,
			GroupConversationViewModel viewModel) {
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
