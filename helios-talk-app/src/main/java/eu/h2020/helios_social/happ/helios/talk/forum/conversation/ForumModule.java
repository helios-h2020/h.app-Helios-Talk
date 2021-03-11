package eu.h2020.helios_social.happ.helios.talk.forum.conversation;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;
import eu.h2020.helios_social.happ.helios.talk.activity.BaseActivity;

@Module
public class ForumModule {

    @ActivityScope
    @Provides
    ForumController provideForumController(ForumControllerImpl forumController) {
        return forumController;
    }

}
