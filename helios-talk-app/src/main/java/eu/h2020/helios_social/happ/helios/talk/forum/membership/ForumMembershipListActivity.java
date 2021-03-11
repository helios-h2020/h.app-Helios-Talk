package eu.h2020.helios_social.happ.helios.talk.forum.membership;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMember;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

public class ForumMembershipListActivity extends HeliosTalkActivity {

    @Inject
    ForumMemberListController controller;
    @Inject
    EventBus eventBus;

    private ForumMemberListAdapter adapter;
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

        ForumMemberListAdapter.OnForumMemberItemSelectedListener<ForumMemberListItem> listener =
                (selected, item) -> {
                    if (selected == item.getRole().getInt()) return;
                    showChangeMemberRoleDialog(item.getMember(), ForumMemberRole.valueOf(selected));
                };
        adapter = new ForumMemberListAdapter(this, listener);
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
        controller.loadMembers(groupId,
                new UiResultExceptionHandler<Collection<ForumMemberListItem>, DbException>(
                        this) {
                    @Override
                    public void onResultUi(Collection<ForumMemberListItem> members) {
                        adapter.addAll(members);
                    }

                    @Override
                    public void onExceptionUi(DbException exception) {
                        handleDbException(exception);
                    }
                });
    }

    private void showChangeMemberRoleDialog(ForumMember member, ForumMemberRole newRole) {
        DialogInterface.OnClickListener okListener = (dialog, which) -> updateForumMemberRole(member, newRole);
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.forums_change_role_dialog_title));
        builder.setMessage("Are you sure you want to change " + member.getAlias() + "'s role in the forum?");
        builder.setPositiveButton(R.string.dialog_yes, okListener);
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void updateForumMemberRole(ForumMember forumMember, ForumMemberRole newRole) {
        controller.updateMemberRole(groupId, forumMember, newRole, new UiExceptionHandler<DbException>(this) {
            @Override
            public void onExceptionUi(DbException exception) {
                handleDbException(exception);
            }
        });
    }
}
