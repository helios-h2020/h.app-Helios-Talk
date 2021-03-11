package eu.h2020.helios_social.happ.helios.talk.forum.membership;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;

@Module
public class ForumMembershipModule {

    @ActivityScope
    @Provides
    ForumMemberListController providesForumMemberListController(
            ForumMemberListControllerImpl forumMemberListController) {
        return forumMemberListController;
    }
}
