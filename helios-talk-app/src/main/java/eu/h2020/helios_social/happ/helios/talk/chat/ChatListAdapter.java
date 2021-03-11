package eu.h2020.helios_social.happ.helios.talk.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactListItem;
import eu.h2020.helios_social.happ.helios.talk.privategroup.GroupItem;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;

import static androidx.recyclerview.widget.SortedList.INVALID_POSITION;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ChatListAdapter
		extends HeliosTalkAdapter<ChatItem, RecyclerView.ViewHolder> {

	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;
	private int chats_position = 0;

	public ChatListAdapter(Context ctx) {
		super(ctx, ChatItem.class);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
			int viewType) {
		if (viewType == 1) {
			View v = LayoutInflater.from(ctx).inflate(
					R.layout.list_item_chat, parent, false);
			return new ChatItemViewHolder(v);
		} else {
			View v = LayoutInflater.from(ctx).inflate(
					R.layout.header_item, parent, false);
			return new HeaderItemViewHolder(v);
		}


	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder ui, int position) {
		if (ui instanceof ChatItemViewHolder) {
			((ChatItemViewHolder) ui).bind(ctx, items.get(position));
		} else {
			((HeaderItemViewHolder) ui).bind(ctx, items.get(position));
		}
	}

	@Override
	public int compare(ChatItem a, ChatItem b) {
		if (a == b) return 0;
		// The chat with the latest message comes first
		long aTime = a.getTimestamp(), bTime = b.getTimestamp();
		if (aTime > bTime) return -1;
		if (aTime < bTime) return 1;
		// Break ties by group name
		String aName = a.getName();
		String bName = b.getName();
		return String.CASE_INSENSITIVE_ORDER.compare(aName, bName);
	}

	@Override
	public boolean areContentsTheSame(ChatItem a, ChatItem b) {
		if (a.isEmpty() != b.isEmpty()) return false;
		if (a.getUnreadCount() != b.getUnreadCount()) return false;
		if (a.getTimestamp() != b.getTimestamp()) return false;
		if (a instanceof ContactListItem && b instanceof ContactListItem)
			return ((ContactListItem) a).isConnected() ==
					((ContactListItem) b).isConnected();
		if (a instanceof GroupItem && b instanceof GroupItem)
			return ((GroupItem) a).isDissolved() ==
					((GroupItem) b).isDissolved();
		return false;
	}

	@Override
	public boolean areItemsTheSame(ChatItem a, ChatItem b) {
		if (a instanceof GroupItem && b instanceof GroupItem) {
			return ((GroupItem) a).getGroupId()
					.equals(((GroupItem) b).getGroupId());
		} else if (a instanceof ContactListItem &&
				b instanceof ContactListItem) {
			return ((ContactListItem) a).getContact()
					.equals(((ContactListItem) b).getContact());
		}/* else if (a instanceof ForumListItem && b instanceof ForumListItem) {
			return ((ForumListItem) a).getId().equals(((ForumListItem) b).getId());
		}*/
		return false;
	}

	int findItemPosition(String groupId) {
		for (int i = 0; i < items.size(); i++) {
			ChatItem item = items.get(i);
			if (item instanceof HeaderItem) continue;
			if (((ChatItem) item).getGroupId().equals(groupId)) {
				return i;
			} else if (item instanceof GroupItem &&
					((GroupItem) item).getGroupId().equals(groupId)) {
				return i;
			} /*else if (item instanceof ForumListItem &&
					((ForumListItem) item).getId().equals(g)) {
				return i;
			}*/
		}
		return INVALID_POSITION;
	}

	void removeItem(String groupId) {
		int pos = findItemPosition(groupId);
		if (pos != INVALID_POSITION) items.removeItemAt(pos);
	}

	void setChatsPosition(int position) {
		this.chats_position = position;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0 || position == chats_position) {
			return TYPE_HEADER;
		}
		return TYPE_ITEM;
	}

}
