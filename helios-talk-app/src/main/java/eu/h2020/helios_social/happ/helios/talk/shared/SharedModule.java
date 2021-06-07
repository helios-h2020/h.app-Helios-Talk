package eu.h2020.helios_social.happ.helios.talk.shared;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;
import eu.h2020.helios_social.happ.helios.talk.shared.controllers.ConnectionController;
import eu.h2020.helios_social.happ.helios.talk.shared.controllers.ConnectionControllerImpl;

@Module
public class SharedModule {

    @ActivityScope
    @Provides
    ConnectionController provideConnectionController(ConnectionControllerImpl connectionController) {
        return connectionController;
    }

}
