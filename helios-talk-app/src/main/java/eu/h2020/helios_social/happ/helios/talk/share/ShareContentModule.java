package eu.h2020.helios_social.happ.helios.talk.share;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;

@Module
public class ShareContentModule {

    @ActivityScope
    @Provides
    ShareContentController provideShareContentController(ShareContentControllerImpl shareContentController) {
        return shareContentController;
    }
}
