package eu.h2020.helios_social.happ.helios.talk.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;


@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ShareContentChatListAdapter
        extends HeliosTalkAdapter<ShareContentChatItem, RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private int chats_position = 0;
    private ShareContentListener shareContentListener;

    public ShareContentChatListAdapter(Context ctx, ShareContentListener shareContentListener) {
        super(ctx, ShareContentChatItem.class);
        this.shareContentListener = shareContentListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View v = LayoutInflater.from(ctx).inflate(
                R.layout.list_item_chat_small, parent, false);
        return new ShareContentChatItemViewHolder(v, shareContentListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder ui, int position) {
        ((ShareContentChatItemViewHolder) ui).bind(ctx, items.get(position));
    }

    @Override
    public int compare(ShareContentChatItem a, ShareContentChatItem b) {
        if (a == b) return 0;
        // The chat with the latest message comes first
        long aTime = a.getLstTimestamp(), bTime = b.getLstTimestamp();
        if (aTime > bTime) return -1;
        if (aTime < bTime) return 1;
        // Break ties by group name
        String aName = a.getTitle();
        String bName = b.getTitle();
        return String.CASE_INSENSITIVE_ORDER.compare(aName, bName);
    }

    @Override
    public boolean areContentsTheSame(ShareContentChatItem a, ShareContentChatItem b) {
        return a.getGroupId().equals(b.getGroupId());
    }

    @Override
    public boolean areItemsTheSame(ShareContentChatItem a, ShareContentChatItem b) {
        return a.getGroupId().equals(b.getGroupId());
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }
}
