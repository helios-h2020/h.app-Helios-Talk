package eu.h2020.helios_social.happ.helios.talk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.vanniktech.emoji.RecentEmoji;

import org.jetbrains.annotations.NotNull;

import eu.h2020.helios_social.happ.helios.talk.api.FeatureFlags;
import eu.h2020.helios_social.happ.helios.talk.api.crypto.KeyStrengthener;
import eu.h2020.helios_social.happ.helios.talk.api.db.DatabaseConfig;

import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.android.DozeWatchdog;
import eu.h2020.helios_social.happ.android.LockManager;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.api.lifecycle.LifecycleManager;
import eu.h2020.helios_social.happ.helios.talk.account.LockManagerImpl;
import eu.h2020.helios_social.happ.helios.talk.login.LoginModule;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.ViewModelModule;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import eu.h2020.helios_social.modules.groupcommunications.preferences.SharedPreferencesHelper;
import eu.h2020.helios_social.modules.groupcommunications.utils.ContextualEgoNetworkConfig;
import eu.h2020.helios_social.modules.groupcommunications.utils.InternalStorageConfig;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Build.VERSION.SDK_INT;

@Module(includes = {
		LoginModule.class,
		ViewModelModule.class
})
public class AppModule {

	private static final String PREF_FILE_NAME = "peer-info";

	static class EagerSingletons {
		@Inject
		AndroidNotificationManager androidNotificationManager;
		@Inject
		DozeWatchdog dozeWatchdog;
		@Inject
		LockManager lockManager;
		@Inject
		RecentEmoji recentEmoji;
	}

	private final Application application;

	public AppModule(Application application) {
		this.application = application;
	}

	@Provides
	@Singleton
	Application providesApplication() {
		return application;
	}

	@Provides
	@Singleton
	DatabaseConfig provideDatabaseConfig(Application app) {
		//FIXME: StrictMode
		StrictMode.ThreadPolicy tp = StrictMode.allowThreadDiskReads();
		StrictMode.allowThreadDiskWrites();
		File dbDir = app.getApplicationContext().getDir("db", MODE_PRIVATE);
		File keyDir = app.getApplicationContext().getDir("key", MODE_PRIVATE);
		StrictMode.setThreadPolicy(tp);
		KeyStrengthener keyStrengthener = SDK_INT >= 23
				? new AndroidKeyStrengthener() : null;
		return new AndroidDatabaseConfig(dbDir, keyDir, keyStrengthener);
	}

	@Provides
	InternalStorageConfig providesInternalStorageConfig(
			Application app) {
		File internalStorageDir =
				app.getApplicationContext().getDir("egonetwork", MODE_PRIVATE);
		return new ContextualEgoNetworkConfig(internalStorageDir);
	}


	@Provides
	SharedPreferences provideSharedPreferences(Application app) {
		// FIXME unify this with getDefaultSharedPreferences()
		return app.getSharedPreferences("db", MODE_PRIVATE);
	}

	@Provides
	@Singleton
	SharedPreferencesHelper provideUsernameHelper(@NotNull Application app) {
		SharedPreferences prefManager = app.getApplicationContext()
				.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		return new SharedPreferencesHelper(prefManager);
	}


	@Provides
	@Singleton
	AndroidNotificationManager provideAndroidNotificationManager(
			LifecycleManager lifecycleManager, EventBus eventBus,
			AndroidNotificationManagerImpl notificationManager) {
		lifecycleManager.registerService(notificationManager);
		eventBus.addListener(notificationManager);
		return notificationManager;
	}

	@Provides
	@Singleton
	DozeWatchdog provideDozeWatchdog(LifecycleManager lifecycleManager) {
		DozeWatchdogImpl dozeWatchdog = new DozeWatchdogImpl(application);
		lifecycleManager.registerService(dozeWatchdog);
		return dozeWatchdog;
	}

	@Provides
	@Singleton
	LockManager provideLockManager(LifecycleManager lifecycleManager,
			EventBus eventBus, LockManagerImpl lockManager) {
		lifecycleManager.registerService(lockManager);
		eventBus.addListener(lockManager);
		return lockManager;
	}

	@Provides
	@Singleton
	RecentEmoji provideRecentEmoji(LifecycleManager lifecycleManager,
			RecentEmojiImpl recentEmoji) {
		lifecycleManager.registerOpenDatabaseHook(recentEmoji);
		return recentEmoji;
	}

	@Provides
	FeatureFlags provideFeatureFlags() {
		return () -> TestingConstants.IS_DEBUG_BUILD;
	}
}
