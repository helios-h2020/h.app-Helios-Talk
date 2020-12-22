package eu.h2020.helios_social.happ.helios.talk.android.system;

import eu.h2020.helios_social.happ.helios.talk.api.event.EventExecutor;
import eu.h2020.helios_social.happ.helios.talk.api.system.LocationUtils;
import eu.h2020.helios_social.happ.helios.talk.api.system.ResourceProvider;
import eu.h2020.helios_social.happ.helios.talk.api.system.SecureRandomProvider;

import java.util.concurrent.Executor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidSystemModule {

	@Provides
	@Singleton
	SecureRandomProvider provideSecureRandomProvider(
			AndroidSecureRandomProvider provider) {
		return provider;
	}

	@Provides
	LocationUtils provideLocationUtils(AndroidLocationUtils locationUtils) {
		return locationUtils;
	}

	@Provides
	@Singleton
	AndroidExecutor provideAndroidExecutor(
			AndroidExecutorImpl androidExecutor) {
		return androidExecutor;
	}

	@Provides
	@Singleton
	@EventExecutor
	Executor provideEventExecutor(AndroidExecutor androidExecutor) {
		return androidExecutor::runOnUiThread;
	}

	@Provides
	@Singleton
	ResourceProvider provideResourceProvider(AndroidResourceProvider provider) {
		return provider;
	}
}
