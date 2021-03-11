package eu.h2020.helios_social.happ.helios.talk.forum.creation;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.forum.conversation.ForumConversationActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateForumActivity extends HeliosTalkActivity
        implements CreateForumListener {

    @Inject
    CreateForumController controller;

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_fragment_container);

        if (bundle == null) {
            showInitialFragment(new CreateForumFragment());
        }
    }

    private void openNewGroup(String groupId) {
        Intent i = new Intent(this, ForumConversationActivity.class);
        i.putExtra(GROUP_ID, groupId);
        startActivity(i);
        finish();
    }

    @Override
    public void onNamedForumChosen(String name, GroupType groupType, ArrayList<String> tags,
                                   ForumMemberRole defaultMemberRole) {
        controller.createNamedForum(name, groupType, tags, defaultMemberRole,
                new UiResultExceptionHandler<String, DbException>(this) {
                    @Override
                    public void onResultUi(String groupId) {
                        openNewGroup(groupId);
                    }

                    @Override
                    public void onExceptionUi(DbException exception) {
                        handleDbException(exception);
                    }
                });
    }

    @Override
    public void onLocationForumChosen(String name, GroupType groupType, ArrayList<String> tags,
                                      double latitude, double longitude, int radius,
                                      ForumMemberRole defaultMemberRole) {
        controller.createLocationForum(name, groupType, tags, latitude, longitude, radius,
                defaultMemberRole, new UiResultExceptionHandler<String, DbException>(this) {
                    @Override
                    public void onResultUi(String groupId) {
                        openNewGroup(groupId);
                    }

                    @Override
                    public void onExceptionUi(DbException exception) {
                        handleDbException(exception);
                    }
                });
    }
}
