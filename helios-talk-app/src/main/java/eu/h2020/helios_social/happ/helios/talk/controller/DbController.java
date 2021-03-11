package eu.h2020.helios_social.happ.helios.talk.controller;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface DbController {

	void runOnDbThread(Runnable task);
}
