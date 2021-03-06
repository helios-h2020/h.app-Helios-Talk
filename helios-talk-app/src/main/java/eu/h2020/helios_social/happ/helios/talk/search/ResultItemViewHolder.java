package eu.h2020.helios_social.happ.helios.talk.search;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vanniktech.emoji.EmojiTextView;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.forum.conversation.ForumConversationActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.LocationForum;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;
import static eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType.ProtectedForum;
import static eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType.PublicForum;
import static eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType.SecretForum;

public class ResultItemViewHolder extends RecyclerView.ViewHolder {

    private EmojiTextView title;
    private TextView tags;
    private TextView forum_type_tag;
    private TextView distance;
    private Button joinButton;
    private ImageView avatar;
    private ResultsActionListener resultsActionListener;

    public ResultItemViewHolder(@NonNull View v, ResultsActionListener resultsActionListener) {
        super(v);
        title = v.findViewById(R.id.nameView);
        tags = v.findViewById(R.id.tags);
        forum_type_tag = v.findViewById(R.id.forumType);
        distance = v.findViewById(R.id.distance);
        joinButton = v.findViewById(R.id.joinButton);
        avatar = v.findViewById(R.id.groupAvatarView);
        this.resultsActionListener = resultsActionListener;
    }

    protected void bind(Context ctx, ResultItem item) {
        title.setText(item.getTitle());
        String tagsText = "";
        for (String tag : item.getTags()) {
            tagsText += "#" + tag + ", ";
        }

        tagsText = tagsText.replaceAll(", $", "");
        tags.setText(tagsText);
        Forum forum = (Forum) item.getItem();
        if (forum.getGroupType().equals(PublicForum)) avatar.setImageResource(R.drawable.ic_public_forum);
        else if (forum.getGroupType().equals(ProtectedForum)) avatar.setImageResource(R.drawable.ic_protected_forum);
        else if (forum.getGroupType().equals(SecretForum)) avatar.setImageResource(R.drawable.ic_secret_forum);
        else avatar.setImageResource(R.drawable.ic_community_white);

        if (item.isLocal()) {
            joinButton.setText("OPEN");
            joinButton.setOnClickListener(l -> {
                resultsActionListener.onOpenPublicForum(forum);
            });
        } else if (forum.getGroupType().equals(GroupType.PublicForum)){
            joinButton.setText("JOIN");
            joinButton.setOnClickListener(l -> {
                resultsActionListener.onJoinPublicForum(forum);
            });

        } else if (forum.getGroupType().equals(GroupType.ProtectedForum)){
            joinButton.setText("REQUEST JOIN");
            joinButton.setOnClickListener(l -> {
                try {
                    resultsActionListener.onRequestJoinProtectedForum(forum);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            });

        }
        if (item.getItem() instanceof LocationForum) forum_type_tag.setText("location_forum");
        else forum_type_tag.setText("forum");

        if (!item.showDistance()) distance.setVisibility(View.INVISIBLE);
        else {
            distance.setVisibility(View.VISIBLE);
            if (item.getDistance() > 1000) {
                distance.setText(String.format("%.1f", Math.floor(item.getDistance() / 1000.0)) + " km");
            } else {
                distance.setText(String.format("%.1f", Math.floor(item.getDistance())) + " m");
            }
        }
    }
}
