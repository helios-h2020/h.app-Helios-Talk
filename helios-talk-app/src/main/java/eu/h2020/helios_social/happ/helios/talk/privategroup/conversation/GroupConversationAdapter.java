package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.LinearLayoutManager;
import eu.h2020.helios_social.happ.helios.talk.api.Pair;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationItem;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;
import eu.h2020.helios_social.happ.helios.talk.util.ItemReturningAdapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

@UiThread
@NotNullByDefault
class GroupConversationAdapter extends
		HeliosTalkAdapter<GroupMessageItem, GroupMessageItemViewHolder> implements
		ItemReturningAdapter<GroupMessageItem> {

	private GroupListener listener;

	@Nullable
	private SelectionTracker<String> tracker = null;

	GroupConversationAdapter(Context ctx,
			GroupListener groupListener) {
		super(ctx, GroupMessageItem.class);
		listener = groupListener;
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
	public GroupMessageItemViewHolder onCreateViewHolder(ViewGroup viewGroup,
			@LayoutRes int type) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(
				type, viewGroup, false);
		return new GroupMessageItemViewHolder(v, listener);
	}

	@Override
	public void onBindViewHolder(GroupMessageItemViewHolder ui, int position) {
		GroupMessageItem item = items.get(position);
		boolean selected = tracker != null && tracker.isSelected(item.getKey());
		ui.bind(ctx, item, selected);
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
	ConversationItem getLastItem() {
		if (items.size() > 0) {
			return items.get(items.size() - 1);
		} else {
			return null;
		}
	}

	SparseArray<GroupMessageItem> getOutgoingMessages() {
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
	Pair<Integer, GroupMessageItem> getMessageItem(String messageId) {
		for (int i = 0; i < items.size(); i++) {
			GroupMessageItem item = items.get(i);
			if (item.getId().equals(messageId)) {
				return new Pair<>(i, item);
			}
		}
		return null;
	}

	boolean isScrolledToBottom(LinearLayoutManager layoutManager) {
		return layoutManager.findLastVisibleItemPosition() == items.size() - 1;
	}
}
