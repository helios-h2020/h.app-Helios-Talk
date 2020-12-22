package eu.h2020.helios_social.happ.helios.talk.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactListItem;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.GroupItem;
import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.GroupConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.setTransitionName;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_ID;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;
import static eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.GroupConversationActivity.GROUP_NAME;
import static eu.h2020.helios_social.happ.helios.talk.util.UiUtils.formatDate;

public class ChatItemViewHolder extends RecyclerView.ViewHolder {
	private final static float ALPHA = 0.42f;

	protected final ViewGroup layout;
	protected final CircleImageView avatar;
	protected final TextView lastMessage;
	protected final CircleImageView textAvatarView;
	protected final TextView name;
	@Nullable
	protected final ImageView bulb;
	private final TextView unread;
	private final TextView date;

	public ChatItemViewHolder(View v) {
		super(v);

		layout = (ViewGroup) v;
		avatar = v.findViewById(R.id.avatarView);
		textAvatarView = v.findViewById(R.id.txtAvatarView);
		name = v.findViewById(R.id.nameView);
		// this can be null as not all layouts that use this ViewHolder have it
		bulb = v.findViewById(R.id.bulbView);

		unread = v.findViewById(R.id.uCountView);
		date = v.findViewById(R.id.dateView);
		lastMessage = v.findViewById(R.id.lstMessageView);
	}

	protected void bind(Context ctx, ChatItem item) {
		if (item instanceof ContactListItem && !item.isEmpty()) {
			lastMessage.setText(item.getLastMessageText());
			name.setText(item.getName());
			ContactListItem cItem = (ContactListItem) item;
			avatar.setVisibility(View.VISIBLE);
			textAvatarView.setVisibility(View.INVISIBLE);
			avatar.setImageResource(R.drawable.ic_person);

			bulb.setVisibility(View.VISIBLE);
			if (bulb != null) {
				// online/offline
				if (cItem.isConnected()) {
					bulb.setImageResource(R.drawable.contact_connected);
				} else {
					bulb.setImageResource(R.drawable.contact_disconnected);
				}
			}

			// unread count
			int unreadCount = item.getUnreadCount();
			if (unreadCount > 0) {
				lastMessage.setTypeface(null, Typeface.BOLD);
				name.setTypeface(null, Typeface.BOLD);
				unread.setText(
						String.format(Locale.getDefault(), "%d", unreadCount));
				unread.setVisibility(View.VISIBLE);
			} else {
				lastMessage.setTypeface(null, Typeface.NORMAL);
				name.setTypeface(null, Typeface.NORMAL);
				unread.setVisibility(View.INVISIBLE);
			}

			long timestamp = ((ContactListItem) item).getRealTimestamp();
			date.setText(formatDate(date.getContext(), timestamp));

			ContactId c = cItem.getContact().getId();
			setTransitionName(avatar, UiUtils.getAvatarTransitionName(c));
			setTransitionName(bulb, UiUtils.getBulbTransitionName(c));

		} else if (item instanceof GroupItem) {
			lastMessage.setText(item.getLastMessageText());
			name.setText(item.getName());
			bulb.setVisibility(View.INVISIBLE);
			avatar.setVisibility(VISIBLE);
			avatar.setImageResource(R.drawable.ic_group);

			GroupItem groupItem = (GroupItem) item;
			//textAvatarView.setVisibility(View.VISIBLE);
			//textAvatarView.setImageResource(R.drawable.ic_group);

			//textAvatarView.setText(item.getName().substring(0, 1));
			//textAvatarView.setBackgroundBytes(groupItem.getId().getBytes());
			//textAvatarView.setUnreadCount(item.getUnreadCount());

			// unread count
			int unreadCount = item.getUnreadCount();
			if (unreadCount > 0) {
				name.setTypeface(null, Typeface.BOLD);
				lastMessage.setTypeface(null, Typeface.BOLD);
				unread.setText(
						String.format(Locale.getDefault(), "%d", unreadCount));
				unread.setVisibility(View.VISIBLE);
			} else {
				name.setTypeface(null, Typeface.NORMAL);
				lastMessage.setTypeface(null, Typeface.NORMAL);
				unread.setVisibility(View.INVISIBLE);
			}

			if (!groupItem.isDissolved()) {
				// full visibility
				textAvatarView.setAlpha(1);
				name.setAlpha(1);

				// Date and Status
				if (groupItem.isEmpty()) {
					date.setVisibility(GONE);
				} else {
					long lastUpdate = groupItem.getTimestamp();
					date.setText(UiUtils.formatDate(ctx, lastUpdate));
					date.setVisibility(VISIBLE);
				}
			} else {
				// grey out
				textAvatarView.setAlpha(ALPHA);
				name.setAlpha(ALPHA);
				date.setVisibility(GONE);
			}
		} /*else if (item instanceof ForumListItem) {
			lastMessage.setText(item.getLastMessageText());
			name.setText(item.getName());
			bulb.setVisibility(View.INVISIBLE);
			avatar.setVisibility(VISIBLE);
			avatar.setImageResource(R.drawable.ic_community_white);

			ForumListItem forumItem = (ForumListItem) item;

			// unread count
			int unreadCount = item.getUnreadCount();
			if (unreadCount > 0) {
				name.setTypeface(null, Typeface.BOLD);
				lastMessage.setTypeface(null, Typeface.BOLD);
				unread.setText(
						String.format(Locale.getDefault(), "%d", unreadCount));
				unread.setVisibility(View.VISIBLE);
			} else {
				name.setTypeface(null, Typeface.NORMAL);
				lastMessage.setTypeface(null, Typeface.NORMAL);
				unread.setVisibility(View.INVISIBLE);
			}

			textAvatarView.setAlpha(1);
			name.setAlpha(1);

			// Date and Status
			if (forumItem.isEmpty()) {
				date.setVisibility(GONE);
			} else {
				long lastUpdate = forumItem.getTimestamp();
				date.setText(UiUtils.formatDate(ctx, lastUpdate));
				date.setVisibility(VISIBLE);
			}

		}*/

		// Open Group/Forum/Contact Conversation on Click
		layout.setOnClickListener(v ->
		{
			/*else if (item instanceof ForumListItem) {
				ForumListItem forum = (ForumListItem) item;
				Intent i = new Intent(ctx, ForumActivity.class);
				GroupId id = forum.getId();
				i.putExtra(GROUP_ID, id.getBytes());
				i.putExtra(GROUP_NAME, forum.getName());
				ctx.startActivity(i);
			} else*/
			if (item instanceof ContactListItem && !item.isEmpty()) {
				ContactListItem contact = (ContactListItem) item;
				Intent i = new Intent(ctx,
						ConversationActivity.class);
				ContactId contactId = contact.getContact().getId();
				i.putExtra(CONTACT_ID, contactId.getId());
				i.putExtra(GROUP_ID, contact.getGroupId());
				ctx.startActivity(i);
			} else if (item instanceof GroupItem) {
				GroupItem group = (GroupItem) item;
				Intent i = new Intent(ctx, GroupConversationActivity.class);
				String id = group.getGroupId();
				i.putExtra(GROUP_ID, id);
				i.putExtra(GROUP_NAME, group.getName());
				ctx.startActivity(i);
			}
		});
	}
}
