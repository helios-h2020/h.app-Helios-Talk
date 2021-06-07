package eu.h2020.helios_social.happ.helios.talk.share;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.chat.ChatItem;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactListItem;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.ForumItem;
import eu.h2020.helios_social.happ.helios.talk.forum.conversation.ForumConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.GroupItem;
import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.PrivateGroupConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.setTransitionName;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_ID;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;
import static eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.PrivateGroupConversationActivity.GROUP_NAME;
import static eu.h2020.helios_social.happ.helios.talk.util.UiUtils.formatDate;

public class ShareContentChatItemViewHolder extends RecyclerView.ViewHolder {

    private final ViewGroup layout;
    private final CircleImageView avatar;
    private final ImageView groupAvatarView;
    private final TextView title;
    private final Button sendButton;
    private final ShareContentListener listener;

    public ShareContentChatItemViewHolder(View v, ShareContentListener listener) {
        super(v);

        layout = (ViewGroup) v;
        avatar = v.findViewById(R.id.avatarView);
        groupAvatarView = v.findViewById(R.id.groupAvatarView);
        title = v.findViewById(R.id.chatTitle);
        this.sendButton = v.findViewById(R.id.send);
        this.listener = listener;
    }

    protected void bind(Context ctx, ShareContentChatItem item) {
        title.setText(item.getTitle());
        if (item.getType().equals(GroupType.PrivateConversation)) {
            ShareContentContactChatItem contactChatItem = (ShareContentContactChatItem) item;
            avatar.setVisibility(View.VISIBLE);
            groupAvatarView.setVisibility(View.INVISIBLE);
            if (contactChatItem.getProfilePicture() == null)
                avatar.setImageResource(contactChatItem.getIconResourceId());
            else
                avatar.setImageBitmap(BitmapFactory.decodeByteArray(
                        contactChatItem.getProfilePicture(),
                        0,
                        contactChatItem.getProfilePicture().length));
        } else {
            avatar.setVisibility(GONE);
            groupAvatarView.setVisibility(VISIBLE);
            groupAvatarView.setImageResource(item.getIconResourceId());
        }

        sendButton.setOnClickListener(l -> {
            listener.onShareContent(item);
        });
    }
}