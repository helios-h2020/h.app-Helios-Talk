package eu.h2020.helios_social.happ.account;

import android.app.Application;
import android.content.SharedPreferences;

import eu.h2020.helios_social.happ.helios.talk.api.crypto.CryptoComponent;
import eu.h2020.helios_social.happ.helios.talk.api.db.DatabaseConfig;
import eu.h2020.helios_social.happ.helios.talk.api.identity.IdentityManager;

import eu.h2020.helios_social.happ.helios.talk.Localizer;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.android.account.AndroidAccountManager;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import javax.inject.Inject;

class HeliosTalkAccountManager extends AndroidAccountManager {

	@Inject
	HeliosTalkAccountManager(DatabaseConfig databaseConfig, CryptoComponent crypto,
			IdentityManager identityManager, SharedPreferences prefs,
			Application app) {
		super(databaseConfig, crypto, identityManager, prefs, app);
	}

	@Override
	public void deleteAccount() {
		synchronized (stateChangeLock) {
			super.deleteAccount();
			Localizer.reinitialize();
			UiUtils.setTheme(appContext,
					appContext.getString(R.string.pref_theme_dark_value));
		}
	}
}
