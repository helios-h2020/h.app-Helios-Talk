package eu.h2020.helios_social.happ.helios.talk.privategroup.membership;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;


@Module
public class GroupMembershipModule {

    @ActivityScope
    @Provides
    GroupMemberListController providesGroupMemberListController(
            GroupMemberListControllerImpl groupMemberListController) {
        return groupMemberListController;
    }
}
