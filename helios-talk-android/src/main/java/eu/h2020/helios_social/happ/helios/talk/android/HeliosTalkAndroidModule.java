package eu.h2020.helios_social.happ.helios.talk.android;

import dagger.Module;
import eu.h2020.helios_social.happ.helios.talk.android.battery.AndroidBatteryModule;
import eu.h2020.helios_social.happ.helios.talk.android.system.AndroidSystemModule;

@Module(includes = {
		AndroidBatteryModule.class,
		AndroidSystemModule.class,
})
public class HeliosTalkAndroidModule {
}
