package eu.h2020.helios_social.happ.helios.talk.android.battery;

import eu.h2020.helios_social.happ.helios.talk.api.battery.BatteryManager;
import eu.h2020.helios_social.happ.helios.talk.api.lifecycle.LifecycleManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidBatteryModule {

	public static class EagerSingletons {
		@Inject
		BatteryManager batteryManager;
	}

	@Provides
	@Singleton
	BatteryManager provideBatteryManager(LifecycleManager lifecycleManager,
			AndroidBatteryManager batteryManager) {
		lifecycleManager.registerService(batteryManager);
		return batteryManager;
	}
}
