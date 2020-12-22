package eu.h2020.helios_social.happ.helios.talk.android;

import eu.h2020.helios_social.happ.helios.talk.android.battery.AndroidBatteryModule;

public interface HeliosTalkAndroidEagerSingletons {

	void inject(AndroidBatteryModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				HeliosTalkAndroidEagerSingletons c) {
			c.inject(new AndroidBatteryModule.EagerSingletons());
		}
	}
}
