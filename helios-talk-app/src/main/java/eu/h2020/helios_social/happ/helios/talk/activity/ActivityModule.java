package eu.h2020.helios_social.happ.helios.talk.activity;

import android.app.Activity;

import eu.h2020.helios_social.happ.helios.talk.HeliosTalkService;
import eu.h2020.helios_social.happ.helios.talk.account.SetupController;
import eu.h2020.helios_social.happ.helios.talk.account.SetupControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.HeliosTalkController;
import eu.h2020.helios_social.happ.helios.talk.controller.HeliosTalkControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.DbController;
import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerController;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerControllerImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

	private final BaseActivity activity;

	public ActivityModule(BaseActivity activity) {
		this.activity = activity;
	}

	@ActivityScope
	@Provides
	BaseActivity provideBaseActivity() {
		return activity;
	}

	@ActivityScope
	@Provides
	Activity provideActivity() {
		return activity;
	}

	@ActivityScope
	@Provides
	SetupController provideSetupController(
			SetupControllerImpl setupController) {
		return setupController;
	}

	@ActivityScope
	@Provides
	protected HeliosTalkController provideHeliosTalkController(
			HeliosTalkControllerImpl heliosTalkController) {
		activity.addLifecycleController(heliosTalkController);
		return heliosTalkController;
	}

	@ActivityScope
	@Provides
	DbController provideDBController(DbControllerImpl dbController) {
		return dbController;
	}

	@ActivityScope
	@Provides
	NavDrawerController provideNavDrawerController(
			NavDrawerControllerImpl navDrawerController) {
		activity.addLifecycleController(navDrawerController);
		return navDrawerController;
	}

	@ActivityScope
	@Provides
	HeliosTalkService.HeliosServiceConnection provideHeliosServiceConnection() {
		return new HeliosTalkService.HeliosServiceConnection();
	}
}
