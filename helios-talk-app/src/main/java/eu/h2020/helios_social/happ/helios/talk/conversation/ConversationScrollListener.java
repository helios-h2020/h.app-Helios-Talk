package eu.h2020.helios_social.happ.helios.talk.conversation;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerViewScrollListener;

@NotNullByDefault
public class ConversationScrollListener extends
        HeliosTalkRecyclerViewScrollListener<ConversationAdapter, ConversationItem> {

    private final ConversationViewModel viewModel;

    public ConversationScrollListener(ConversationAdapter adapter,
                                      ConversationViewModel viewModel) {
        super(adapter);
        this.viewModel = viewModel;
    }

    @Override
    protected void onItemVisible(ConversationItem item) {
        if (!item.isRead() && item.isIncoming()) {
            viewModel.markMessageRead(item.getGroupId(), item.getId());
            item.markRead();
        }
    }

}
