package eu.h2020.helios_social.happ.helios.talk.controller;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface DbController {

	void runOnDbThread(Runnable task);
}
