package eu.h2020.helios_social.happ.helios.talk.privategroup.creation;

import android.content.Intent;
import android.os.Bundle;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.PrivateGroupConversationActivity;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateGroupActivity extends HeliosTalkActivity
        implements CreateGroupListener {

    @Inject
    CreateGroupController controller;
    private String IS_NEW="isNew";

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_fragment_container);

        if (bundle == null) {
            showInitialFragment(new CreateGroupFragment());
        }
    }

    @Override
    public void onGroupNameChosen(String name) {
        controller.createGroup(name,
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

    private void openNewGroup(String groupId) {
        Intent i = new Intent(this, PrivateGroupConversationActivity.class);
        i.putExtra(GROUP_ID, groupId);
        i.putExtra(IS_NEW,true);
        startActivity(i);
        finish();
    }
}
