package eu.h2020.helios_social.happ.helios.talk.controller;

import android.app.Activity;

public interface ActivityLifecycleController {

	void onActivityCreate(Activity activity);

	void onActivityStart();

	void onActivityStop();

	void onActivityDestroy();
}
