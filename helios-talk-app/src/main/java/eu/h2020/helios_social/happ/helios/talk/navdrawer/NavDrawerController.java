package eu.h2020.helios_social.happ.helios.talk.navdrawer;

import android.content.Context;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.controller.ActivityLifecycleController;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultHandler;

@NotNullByDefault
public interface NavDrawerController extends ActivityLifecycleController {

	void shouldAskForDozeWhitelisting(Context ctx,
			ResultHandler<Boolean> handler);

}
