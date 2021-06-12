package eu.h2020.helios_social.happ.helios.talk.group;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationListener;
import eu.h2020.helios_social.happ.helios.talk.conversation.ImageItemDecoration;
import eu.h2020.helios_social.modules.groupcommunications_utils.Pair;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationItem;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;
import eu.h2020.helios_social.happ.helios.talk.util.ItemReturningAdapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

@UiThread
@NotNullByDefault
public class GroupConversationAdapter extends
        HeliosTalkAdapter<GroupMessageItem, GroupConversationItemViewHolder> implements
        ItemReturningAdapter<GroupMessageItem> {

    private ConversationListener listener;
    private final RecyclerView.RecycledViewPool imageViewPool;
    private final ImageItemDecoration imageItemDecoration;
    private int position;

    @Nullable
    private SelectionTracker<String> tracker = null;

    public GroupConversationAdapter(Context ctx,
                                    ConversationListener groupListener) {
        super(ctx, GroupMessageItem.class);
        listener = groupListener;
        // This shares the same pool for view recycling between all image lists
        imageViewPool = new RecyclerView.RecycledViewPool();
        // Share the item decoration as well
        imageItemDecoration = new ImageItemDecoration(ctx);
    }

    @LayoutRes
    @Override
    public int getItemViewType(int position) {
        GroupMessageItem item = items.get(position);
        return item.getLayout();
    }

    String getItemKey(int position) {
        return items.get(position).getKey();
    }

    int getPositionOfKey(String key) {
        for (int i = 0; i < items.size(); i++) {
            if (key.equals(items.get(i).getKey())) return i;
        }
        return NO_POSITION;
    }

    @Override
    public GroupConversationItemViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                              @LayoutRes int type) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                type, viewGroup, false);
        switch (type) {
            case R.layout.list_item_group_conversation_file_msg:
                return new GroupFileMessageViewHolder(v, listener);
            case R.layout.list_item_group_conversation_msg:
                return new GroupMessageViewHolder(v, listener, imageViewPool, imageItemDecoration);
            default:
                throw new IllegalArgumentException("Unknown Conversation Item");
        }
    }

    @Override
    public void onBindViewHolder(GroupConversationItemViewHolder ui, int position) {
        GroupMessageItem item = items.get(position);
        boolean selected = tracker != null && tracker.isSelected(item.getKey());
        ui.bind(ctx, item, selected);
        ui.author.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(ui.getAdapterPosition());
                return false;
            }
        });
    }

    @Override
    public int compare(GroupMessageItem c1, GroupMessageItem c2) {
        return Long.compare(c1.getTime(), c2.getTime());
    }

    @Override
    public boolean areItemsTheSame(GroupMessageItem c1,
                                   GroupMessageItem c2) {
        return c1.getId().equals(c2.getId());
    }

    @Override
    public boolean areContentsTheSame(GroupMessageItem c1,
                                      GroupMessageItem c2) {
        return c1.equals(c2);
    }

    void setSelectionTracker(SelectionTracker<String> tracker) {
        this.tracker = tracker;
    }

    @Nullable
    public ConversationItem getLastItem() {
        if (items.size() > 0) {
            return items.get(items.size() - 1);
        } else {
            return null;
        }
    }

    public SparseArray<GroupMessageItem> getOutgoingMessages() {
        SparseArray<GroupMessageItem> messages = new SparseArray<>();

        for (int i = 0; i < items.size(); i++) {
            GroupMessageItem item = items.get(i);
            if (!item.isIncoming()) {
                messages.put(i, item);
            }
        }
        return messages;
    }

    @Nullable
    public Pair<Integer, GroupMessageItem> getMessageItem(String messageId) {
        for (int i = 0; i < items.size(); i++) {
            GroupMessageItem item = items.get(i);
            if (item.getId().equals(messageId)) {
                return new Pair<>(i, item);
            }
        }
        return null;
    }

    public boolean isScrolledToBottom(LinearLayoutManager layoutManager) {
        return layoutManager.findLastVisibleItemPosition() == items.size() - 1;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onViewRecycled(@NonNull GroupConversationItemViewHolder holder) {
        holder.author.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }
}
