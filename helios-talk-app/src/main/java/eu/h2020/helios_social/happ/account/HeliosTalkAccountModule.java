package eu.h2020.helios_social.happ.account;

import eu.h2020.helios_social.happ.helios.talk.api.account.AccountManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class HeliosTalkAccountModule {

	@Provides
	@Singleton
	AccountManager provideAccountManager(HeliosTalkAccountManager accountManager) {
		return accountManager;
	}
}
