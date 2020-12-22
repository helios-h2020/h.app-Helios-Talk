package eu.h2020.helios_social.happ.helios.talk.context;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;

@Module
public class CreateContextModule {

	@ActivityScope
	@Provides
	ContextController provideContextController(
			ContextControllerImpl contextController) {
		return contextController;
	}
}
