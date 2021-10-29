package eu.h2020.helios_social.happ.helios.talk.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.CreateForumActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateChatActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateGroupActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumType;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

public class GroupTypeSelectionActivity extends HeliosTalkActivity {
    private ListView listView;
    private String groupTypeNames[] = {
            "New Chat",
            "New Group Chat",
            "New Public Forum",
            "New Protected Forum",
            "New Secret Forum"
    };

    private int infos[] = {
            R.string.chat_description,
            R.string.group_chat_description,
            R.string.public_forum_description,
            R.string.protected_forum_description,
            R.string.secret_forum_description,
    };


    private int hints[] = {
            R.string.chat_hint,
            R.string.group_chat_hint,
            R.string.public_forum_hint,
            R.string.protected_forum_hint,
            R.string.secret_forum_hint,
    };

    private Integer imageIds[] = {
            R.drawable.ic_chat_bubble,
            R.drawable.ic_group_add_48dp,
            R.drawable.ic_public,
            R.drawable.ic_protected,
            R.drawable.ic_secret,
    };

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_type_selection);
        ListView listView=findViewById(R.id.list);

        // For populating list data
        GroupTypeSelectionAdapter groupTypeSelectionAdapter = new GroupTypeSelectionAdapter(this, groupTypeNames, infos, hints, imageIds);
        listView.setAdapter(groupTypeSelectionAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(GroupTypeSelectionActivity.this,
                                CreateChatActivity.class);
                        startActivity(intent);
                        return;
                    case 1:
                        intent = new Intent(GroupTypeSelectionActivity.this,
                                CreateGroupActivity.class);
                        startActivity(intent);
                        return;
                    case 2:
                        Intent createPublicForumIntent = new Intent(GroupTypeSelectionActivity.this,
                                CreateForumActivity.class);
                        createPublicForumIntent.putExtra("type",2);
                        startActivity(createPublicForumIntent);
                        return;
                    case 3:
                        Intent createProtectedForumIntent = new Intent(GroupTypeSelectionActivity.this,
                                CreateForumActivity.class);
                        createProtectedForumIntent.putExtra("type",3);

                        startActivity(createProtectedForumIntent);
                        return;
                    case 4:
                        Intent createSecretForumIntent = new Intent(GroupTypeSelectionActivity.this,
                                CreateForumActivity.class);
                        createSecretForumIntent.putExtra("type",4);

                        startActivity(createSecretForumIntent);
                        return;
                }
            }
        });
    }
}