package eu.h2020.helios_social.happ.helios.talk.privategroup.membership;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import eu.h2020.helios_social.happ.helios.talk.group.GroupMessageItem;
import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.PrivateGroupConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.shared.controllers.ConnectionController;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.ContactExistsException;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.InvalidActionException;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.PendingContactExistsException;
import eu.h2020.helios_social.modules.groupcommunications_utils.identity.IdentityManager;
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
    @Inject
    ContactManager contactManager;
    @Inject
    IdentityManager identityManager;
    @Inject
    ConnectionController connectionController;

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
        OnGroupMemberItemSelectedListener<GroupMemberListItem> listener = this::sendConnectionRequest;
        adapter = new GroupMemberListAdapter(this, listener);
        list.setAdapter(adapter);
        list.setEmptyImage(R.drawable.ic_no_contacts);
        list.setEmptyTitle(R.string.empty_member_list);


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
                        list.postOnAnimationDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (adapter.getItemCount() == 0){
                                    list.showEmpty();
                                }
                            }
                        }, 2000);
                    }

                    @Override
                    public void onExceptionUi(DbException exception) {
                        Log.d("onExceptionUi ","true");
                        handleDbException(exception);
                    }
                });
    }

    public interface OnGroupMemberItemSelectedListener<I> {
        void onItemSelected(int selected);
    }


    public void sendConnectionRequest(int position) {

        GroupMemberListItem memberListItem = adapter.getItemAt(position);
        if (memberListItem==null) return;
        if (!canSendRequest(memberListItem.getPeerId().getId())) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.send_connection_request)
                .setMessage(getResources().getString(R.string.send_connection_request_message,  memberListItem.getAlias()))
                .setPositiveButton("ok", (dialogInterface, i) -> connectionController.sendConnectionRequest(memberListItem.getPeerId().getId(),memberListItem.getAlias(), new UiResultExceptionHandler<Void, Exception>(this) {
                    @Override
                    public void onResultUi(Void v) {
                        Toast.makeText(GroupMembershipListActivity.this,
                                "Connection Request to " + memberListItem.getAlias() + " has been sent!",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onExceptionUi(Exception exception) {
                        if (exception instanceof ContactExistsException)
                            Toast.makeText(GroupMembershipListActivity.this,
                                    "You are already Connected with " + memberListItem.getAlias(),
                                    Toast.LENGTH_LONG
                            ).show();
                        else if (exception instanceof PendingContactExistsException)
                            Toast.makeText(GroupMembershipListActivity.this,
                                    "You have already sent connection request to " + memberListItem.getAlias(),
                                    Toast.LENGTH_LONG
                            ).show();
                        else if (exception instanceof InvalidActionException)
                            Toast.makeText(GroupMembershipListActivity.this,
                                    ((InvalidActionException) exception).getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        exception.printStackTrace();
                    }
                }))
                .setNegativeButton("cancel", (dialog, i) -> dialog.dismiss()).create().show();
    }

    private boolean canSendRequest(String id) {
        try {
            if (contactManager.contactExists(new ContactId(id))) {
                Toast.makeText(GroupMembershipListActivity.this,
                        "You are already Connected with this user!",
                        Toast.LENGTH_LONG
                ).show();
                return false;
            }
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
        if (identityManager.getIdentity().getNetworkId().equals(id)) {
            Toast.makeText(GroupMembershipListActivity.this,
                    "You can not send connection request to yourself. Try to click on one other user!",
                    Toast.LENGTH_LONG
            ).show();
            return false;
        }
        return true;
    }

}
