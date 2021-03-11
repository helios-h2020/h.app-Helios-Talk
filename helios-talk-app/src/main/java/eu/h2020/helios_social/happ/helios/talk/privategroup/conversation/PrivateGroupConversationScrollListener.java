package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import eu.h2020.helios_social.happ.helios.talk.group.GroupConversationAdapter;
import eu.h2020.helios_social.happ.helios.talk.group.GroupMessageItem;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerViewScrollListener;

public class PrivateGroupConversationScrollListener extends
        HeliosTalkRecyclerViewScrollListener<GroupConversationAdapter, GroupMessageItem> {

    private final PrivateGroupConversationViewModel viewModel;

    public PrivateGroupConversationScrollListener(GroupConversationAdapter adapter,
                                                  PrivateGroupConversationViewModel viewModel) {
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
