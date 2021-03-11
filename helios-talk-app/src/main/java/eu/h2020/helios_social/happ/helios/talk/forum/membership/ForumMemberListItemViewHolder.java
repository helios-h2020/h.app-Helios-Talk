package eu.h2020.helios_social.happ.helios.talk.forum.membership;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import com.vanniktech.emoji.EmojiTextView;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
class ForumMemberListItemViewHolder extends RecyclerView.ViewHolder {

    private final CircleImageView avatar;
    private final EmojiTextView username;
    private final EmojiTextView fakename;
    private final Spinner forumRole;

    ForumMemberListItemViewHolder(View v) {
        super(v);
        avatar = v.findViewById(R.id.avatarView);
        username = v.findViewById(R.id.usernameView);
        fakename = v.findViewById(R.id.fakenameView);
        forumRole = v.findViewById(R.id.member_role);
    }

    protected void bind(ForumMemberListItem item, ForumMemberListAdapter.OnForumMemberItemSelectedListener<ForumMemberListItem> listener) {
        // member name, avatar and author info
        avatar.setImageResource(R.drawable.ic_person);
        username.setText(item.getAlias());
        fakename.setText("unknown");
        forumRole.setSelection(item.getRole().getInt());

        forumRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (listener != null) listener.onItemSelected(i, item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


}

