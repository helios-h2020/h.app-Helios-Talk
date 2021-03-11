package eu.h2020.helios_social.happ.helios.talk.favourites;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationItem;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationListener;
import eu.h2020.helios_social.happ.helios.talk.conversation.ImageItemDecoration;
import eu.h2020.helios_social.happ.helios.talk.group.GroupMessageItem;
import eu.h2020.helios_social.happ.helios.talk.group.GroupMessageViewHolder;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;
import eu.h2020.helios_social.happ.helios.talk.util.ItemReturningAdapter;
import eu.h2020.helios_social.modules.groupcommunications_utils.Pair;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class FavListAdapter extends HeliosTalkAdapter<FavItem, FavItemViewHolder> implements
        ItemReturningAdapter<FavItem> {

    private final RecyclerView.RecycledViewPool imageViewPool;
    private final ImageItemDecoration imageItemDecoration;

    @Nullable
    private SelectionTracker<String> tracker = null;

    public FavListAdapter(Context ctx) {
        super(ctx, FavItem.class);
        // This shares the same pool for view recycling between all image lists
        imageViewPool = new RecyclerView.RecycledViewPool();
        // Share the item decoration as well
        imageItemDecoration = new ImageItemDecoration(ctx);
    }

    @LayoutRes
    @Override
    public int getItemViewType(int position) {
        ConversationItem item = items.get(position);
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
    public FavItemViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                @LayoutRes int type) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                type, viewGroup, false);
        return new FavItemViewHolder(v,
                imageViewPool, imageItemDecoration);
    }

    @Override
    public void onBindViewHolder(FavItemViewHolder ui, int position) {
        FavItem item = items.get(position);
        ui.bind(ctx, item);
    }

    @Override
    public int compare(FavItem c1, FavItem c2) {
        return Long.compare(c1.getTime(), c2.getTime());
    }

    @Override
    public boolean areItemsTheSame(FavItem c1,
                                   FavItem c2) {
        return c1.getId().equals(c2.getId());
    }

    @Override
    public boolean areContentsTheSame(FavItem c1,
                                      FavItem c2) {
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

    public SparseArray<FavItem> getOutgoingMessages() {
        SparseArray<FavItem> messages = new SparseArray<>();

        for (int i = 0; i < items.size(); i++) {
            FavItem item = items.get(i);
            if (!item.isIncoming()) {
                messages.put(i, item);
            }
        }
        return messages;
    }

    @Nullable
    public Pair<Integer, FavItem> getMessageItem(String messageId) {
        for (int i = 0; i < items.size(); i++) {
            FavItem item = items.get(i);
            if (item.getId().equals(messageId)) {
                return new Pair<>(i, item);
            }
        }
        return null;
    }

    public boolean isScrolledToBottom(LinearLayoutManager layoutManager) {
        return layoutManager.findLastVisibleItemPosition() == items.size() - 1;
    }
}
