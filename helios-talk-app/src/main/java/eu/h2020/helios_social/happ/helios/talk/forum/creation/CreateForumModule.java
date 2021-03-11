package eu.h2020.helios_social.happ.helios.talk.forum.creation;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;

@Module
public class CreateForumModule {

	@ActivityScope
	@Provides
    CreateForumController providesCreateForumController(
			CreateForumControllerImpl createGroupController) {
		return createGroupController;
	}

}
