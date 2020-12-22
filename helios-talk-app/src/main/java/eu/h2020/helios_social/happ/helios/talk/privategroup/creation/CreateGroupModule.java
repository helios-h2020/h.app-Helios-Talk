package eu.h2020.helios_social.happ.helios.talk.privategroup.creation;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;

@Module
public class CreateGroupModule {

	@ActivityScope
	@Provides
	CreateGroupController providesCreateGroupController(
			CreateGroupControllerImpl createGroupController) {
		return createGroupController;
	}

}
