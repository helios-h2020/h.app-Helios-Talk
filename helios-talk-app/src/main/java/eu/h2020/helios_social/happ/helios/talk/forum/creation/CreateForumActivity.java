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
import eu.h2020.helios_social.modules.groupcommunications_utils.util.Location;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateForumActivity extends HeliosTalkActivity
        implements CreateForumListener {

    int forumType;
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String RADIUS = "radius";
    @Inject
    CreateForumController controller;
    private final String IS_NEW="isNew";

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        Intent i = getIntent();
        forumType = i.getIntExtra("type",2);
        setContentView(R.layout.activity_fragment_container);

        if (bundle == null) {
            Bundle args = new Bundle();
            args.putInt("type", forumType);
            Location location = controller.getLocationRestriction();
            if (location!=null) {
                args.putDouble(LAT, location.getLat());
                args.putDouble(LON, location.getLon());
                args.putDouble(RADIUS, location.getRadius());
            }
            CreateForumFragment createForumFragment = new CreateForumFragment();
            createForumFragment.setArguments(args);
            showInitialFragment(createForumFragment);
        }
    }
    private void openNewGroup(String groupId) {
        Intent i = new Intent(this, ForumConversationActivity.class);
        i.putExtra(GROUP_ID, groupId);
        i.putExtra(IS_NEW,true);
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
