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
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.LocationForum;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

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
        avatar.setImageResource(R.drawable.ic_community_white);
        title.setText(item.getTitle());
        String tagsText = "";
        for (String tag : item.getTags()) {
            tagsText += "#" + tag + ", ";
        }

        tagsText = tagsText.replaceAll(", $", "");
        tags.setText(tagsText);

        if (item.isLocal()) {
            joinButton.setText("OPEN");
            joinButton.setOnClickListener(l -> {
                resultsActionListener.onOpenPublicForum((Forum) item.getItem());
            });
        } else {
            joinButton.setText("JOIN");
            joinButton.setOnClickListener(l -> {
                resultsActionListener.onJoinPublicForum((Forum) item.getItem());
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
