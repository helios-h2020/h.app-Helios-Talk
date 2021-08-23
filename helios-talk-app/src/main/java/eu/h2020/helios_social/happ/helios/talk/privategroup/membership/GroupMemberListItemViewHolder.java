package eu.h2020.helios_social.happ.helios.talk.privategroup.membership;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import com.vanniktech.emoji.EmojiTextView;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.forum.membership.ForumMemberListAdapter;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
class GroupMemberListItemViewHolder extends RecyclerView.ViewHolder {

    private final CircleImageView avatar;
    private final EmojiTextView username;

    GroupMemberListItemViewHolder(View v) {
        super(v);
        avatar = v.findViewById(R.id.avatarView);
        username = v.findViewById(R.id.usernameView);

    }

    protected void bind(GroupMemberListItem item, GroupMemberListAdapter.OnGroupMemberItemSelectedListener<GroupMemberListItem> listener) {
        // member name, avatar info
        if (item.getProfilePic() == null)
            avatar.setImageResource(R.drawable.ic_person);
        else
            avatar.setImageBitmap(BitmapFactory.decodeByteArray(
                    item.getProfilePic(),
                    0,
                    item.getProfilePic().length)
            );
        username.setText(item.getAlias());
    }


}


