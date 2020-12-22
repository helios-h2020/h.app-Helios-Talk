package eu.h2020.helios_social.happ.helios.talk.controller;

import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultHandler;

public interface HeliosTalkController extends ActivityLifecycleController {

	void startAndBindService();

	boolean accountSignedIn();

	/**
	 * Returns true via the handler when the app has dozed
	 * without being white-listed.
	 */
	void hasDozed(ResultHandler<Boolean> handler);

	void doNotAskAgainForDozeWhiteListing();

	void signOut(ResultHandler<Void> eventHandler, boolean deleteAccount);

	void deleteAccount();

}
