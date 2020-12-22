package eu.h2020.helios_social.happ.helios.talk.login;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultHandler;

@NotNullByDefault
public interface ChangePasswordController {

	float estimatePasswordStrength(String password);

	void changePassword(String oldPassword, String newPassword,
			ResultHandler<Boolean> resultHandler);

}
