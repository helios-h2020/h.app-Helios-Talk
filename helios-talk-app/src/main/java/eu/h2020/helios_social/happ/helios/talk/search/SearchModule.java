package eu.h2020.helios_social.happ.helios.talk.search;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityScope;

@Module
public class SearchModule {

    @ActivityScope
    @Provides
    SearchController provideSearchController(
            SearchControllerImpl searchController) {
        return searchController;
    }
}
