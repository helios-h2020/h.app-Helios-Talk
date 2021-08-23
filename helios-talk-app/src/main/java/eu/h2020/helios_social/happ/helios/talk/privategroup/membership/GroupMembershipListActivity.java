package eu.h2020.helios_social.happ.helios.talk.privategroup.membership;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.membership.ForumMemberListAdapter;
import eu.h2020.helios_social.happ.helios.talk.forum.membership.ForumMemberListController;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMember;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

public class GroupMembershipListActivity extends HeliosTalkActivity {

    @Inject
    GroupMemberListController controller;
    @Inject
    EventBus eventBus;

    private GroupMemberListAdapter adapter;
    private HeliosTalkRecyclerView list;
    private String groupId;
    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.list);

        Intent i = getIntent();
        groupId = i.getStringExtra(GROUP_ID);
        if (groupId == null)
            throw new IllegalStateException("No GroupId in intent");

        list = findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(linearLayoutManager);

        GroupMemberListAdapter.OnGroupMemberItemSelectedListener<GroupMemberListItem> listener =
                (selected, item) -> { return;};
        adapter = new GroupMemberListAdapter(this, listener);
        list.setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        loadMembers();
        list.startPeriodicUpdate();
    }

    @Override
    public void onStop() {
        super.onStop();
        list.stopPeriodicUpdate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMembers() {
        Log.d("loadMembers ","true");
        controller.loadMembers(groupId,
                new UiResultExceptionHandler<Collection<GroupMemberListItem>, DbException>(
                        this) {
                    @Override
                    public void onResultUi(Collection<GroupMemberListItem> members) {
                        Log.d("onResultUi ",members.toString());
                        adapter.addAll(members);
                    }

                    @Override
                    public void onExceptionUi(DbException exception) {
                        Log.d("onExceptionUi ","true");
                        handleDbException(exception);
                    }
                });
    }

}
