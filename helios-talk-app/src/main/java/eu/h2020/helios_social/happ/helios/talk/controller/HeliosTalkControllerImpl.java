package eu.h2020.helios_social.happ.helios.talk.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;

import eu.h2020.helios_social.happ.helios.talk.api.account.AccountManager;
import eu.h2020.helios_social.happ.helios.talk.api.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.happ.helios.talk.api.lifecycle.LifecycleManager;
import eu.h2020.helios_social.happ.helios.talk.api.settings.Settings;
import eu.h2020.helios_social.happ.helios.talk.api.settings.SettingsManager;
import eu.h2020.helios_social.happ.helios.talk.HeliosTalkService;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultHandler;
import eu.h2020.helios_social.happ.android.DozeWatchdog;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import eu.h2020.helios_social.happ.helios.talk.settings.SettingsFragment;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static java.util.logging.Level.WARNING;
import static eu.h2020.helios_social.happ.helios.talk.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logException;

public class HeliosTalkControllerImpl implements HeliosTalkController {

	private static final Logger LOG =
			Logger.getLogger(HeliosTalkControllerImpl.class.getName());

	public static final String DOZE_ASK_AGAIN = "dozeAskAgain";

	private final HeliosTalkService.HeliosServiceConnection serviceConnection;
	private final AccountManager accountManager;
	private final LifecycleManager lifecycleManager;
	private final Executor databaseExecutor;
	private final SettingsManager settingsManager;
	private final DozeWatchdog dozeWatchdog;
	private final Activity activity;

	private boolean bound = false;

	@Inject
	HeliosTalkControllerImpl(HeliosTalkService.HeliosServiceConnection serviceConnection,
			AccountManager accountManager,
			LifecycleManager lifecycleManager,
			@DatabaseExecutor Executor databaseExecutor,
			SettingsManager settingsManager, DozeWatchdog dozeWatchdog,
			Activity activity) {
		this.serviceConnection = serviceConnection;
		this.accountManager = accountManager;
		this.lifecycleManager = lifecycleManager;
		this.databaseExecutor = databaseExecutor;
		this.settingsManager = settingsManager;
		this.dozeWatchdog = dozeWatchdog;
		this.activity = activity;
	}

	@Override
	@CallSuper
	public void onActivityCreate(Activity activity) {
		if (accountManager.hasDatabaseKey()) startAndBindService();
	}

	@Override
	public void onActivityStart() {
	}

	@Override
	public void onActivityStop() {
	}

	@Override
	@CallSuper
	public void onActivityDestroy() {
		unbindService();
	}

	@Override
	public void startAndBindService() {
		activity.startService(new Intent(activity, HeliosTalkService.class));
		bound = activity.bindService(new Intent(activity, HeliosTalkService.class),
				serviceConnection, 0);
	}

	@Override
	public boolean accountSignedIn() {
		return accountManager.hasDatabaseKey() &&
				lifecycleManager.getLifecycleState().isAfter(STARTING_SERVICES);
	}

	@Override
	public void hasDozed(ResultHandler<Boolean> handler) {
		if (!dozeWatchdog.getAndResetDozeFlag()
				|| !UiUtils.needsDozeWhitelisting(activity)) {
			handler.onResult(false);
			return;
		}
		databaseExecutor.execute(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(
								SettingsFragment.SETTINGS_NAMESPACE);
				boolean ask = settings.getBoolean(DOZE_ASK_AGAIN, true);
				handler.onResult(ask);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	@Override
	public void doNotAskAgainForDozeWhiteListing() {
		databaseExecutor.execute(() -> {
			try {
				Settings settings = new Settings();
				settings.putBoolean(DOZE_ASK_AGAIN, false);
				settingsManager.mergeSettings(settings, SettingsFragment.SETTINGS_NAMESPACE);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	@Override
	public void signOut(ResultHandler<Void> eventHandler,
			boolean deleteAccount) {
		new Thread(() -> {
			try {
				// Wait for the service to finish starting up
				IBinder binder = serviceConnection.waitForBinder();
				HeliosTalkService service =
						((HeliosTalkService.HeliosTalkBinder) binder).getService();
				service.waitForStartup();
				// Shut down the service and wait for it to shut down
				LOG.info("Shutting down service");
				service.shutdown();
				service.waitForShutdown();
			} catch (InterruptedException e) {
				LOG.warning("Interrupted while waiting for service");
			} finally {
				if (deleteAccount) accountManager.deleteAccount();
			}
			eventHandler.onResult(null);
		}).start();
	}

	@Override
	public void deleteAccount() {
		accountManager.deleteAccount();
	}

	private void unbindService() {
		if (bound) activity.unbindService(serviceConnection);
	}

}
