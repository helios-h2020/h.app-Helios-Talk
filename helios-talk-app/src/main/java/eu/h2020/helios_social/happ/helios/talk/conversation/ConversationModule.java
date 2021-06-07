package eu.h2020.helios_social.happ.helios.talk.conversation;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;
import eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts.ShareContactController;
import eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts.ShareContactControllerImpl;

@Module
public class ConversationModule {

    @ActivityScope
    @Provides
    ShareContactController provideShareContactController(ShareContactControllerImpl shareContactController) {
        return shareContactController;
    }
}
