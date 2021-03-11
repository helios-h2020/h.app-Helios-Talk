package eu.h2020.helios_social.happ.helios.talk.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.AndroidComponent;
import eu.h2020.helios_social.happ.helios.talk.HeliosTalkApplication;
import eu.h2020.helios_social.happ.helios.talk.DestroyableContext;
import eu.h2020.helios_social.happ.helios.talk.Localizer;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.controller.ActivityLifecycleController;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import androidx.annotation.LayoutRes;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import eu.h2020.helios_social.happ.helios.talk.TestingConstants;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;

import static android.view.WindowManager.LayoutParams.FLAG_SECURE;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class BaseActivity extends AppCompatActivity
		implements DestroyableContext {

	private final static Logger LOG = getLogger(BaseActivity.class.getName());

	protected ActivityComponent activityComponent;

	private final List<ActivityLifecycleController> lifecycleControllers =
			new ArrayList<>();
	private boolean destroyed = false;

	@Nullable
	private Toolbar toolbar = null;
	private boolean searchedForToolbar = false;

	public abstract void injectActivity(ActivityComponent component);

	public void addLifecycleController(ActivityLifecycleController alc) {
		lifecycleControllers.add(alc);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		// create the ActivityComponent *before* calling super.onCreate()
		// because it already attaches fragments which need access
		// to the component for their own injection
		AndroidComponent applicationComponent =
				((HeliosTalkApplication) getApplication())
						.getApplicationComponent();
		activityComponent = DaggerActivityComponent.builder()
				.androidComponent(applicationComponent)
				.activityModule(getActivityModule())
				//.forumModule(getForumModule())
				.build();
		injectActivity(activityComponent);
		super.onCreate(state);
		if (LOG.isLoggable(INFO)) {
			LOG.info("Creating " + getClass().getSimpleName());
		}

		// WARNING: When removing this or making it possible to turn it off,
		//          we need a solution for the app lock feature.
		//          When the app is locked by a timeout and FLAG_SECURE is not
		//          set, the app content becomes visible briefly before the
		//          unlock screen is shown.
		if (TestingConstants.PREVENT_SCREENSHOTS)
			getWindow().addFlags(FLAG_SECURE);

		for (ActivityLifecycleController alc : lifecycleControllers) {
			alc.onActivityCreate(this);
		}
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(
				Localizer.getInstance().setLocale(base));
	}

	public ActivityComponent getActivityComponent() {
		return activityComponent;
	}

	// This exists to make test overrides easier
	protected ActivityModule getActivityModule() {
		return new ActivityModule(this);
	}

	/*protected ForumModule getForumModule() {
		return new ForumModule();
	}*/

	@Override
	protected void onStart() {
		super.onStart();
		if (LOG.isLoggable(INFO)) {
			LOG.info("Starting " + getClass().getSimpleName());
		}
		for (ActivityLifecycleController alc : lifecycleControllers) {
			alc.onActivityStart();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (LOG.isLoggable(INFO)) {
			LOG.info("Resuming " + getClass().getSimpleName());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (LOG.isLoggable(INFO)) {
			LOG.info("Pausing " + getClass().getSimpleName());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (LOG.isLoggable(INFO)) {
			LOG.info("Stopping " + getClass().getSimpleName());
		}
		for (ActivityLifecycleController alc : lifecycleControllers) {
			alc.onActivityStop();
		}
	}

	protected void showInitialFragment(BaseFragment f) {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragmentContainer, f, f.getUniqueTag())
				.commit();
	}

	public void showNextFragment(BaseFragment f) {
		if (!getLifecycle().getCurrentState().isAtLeast(STARTED)) return;
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.step_next_in,
						R.anim.step_previous_out, R.anim.step_previous_in,
						R.anim.step_next_out)
				.replace(R.id.fragmentContainer, f, f.getUniqueTag())
				.addToBackStack(f.getUniqueTag())
				.commit();
	}

	protected boolean isFragmentAdded(String fragmentTag) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment f = fm.findFragmentByTag(fragmentTag);
		return f != null && f.isAdded();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (LOG.isLoggable(INFO)) {
			LOG.info("Destroying " + getClass().getSimpleName());
		}
		destroyed = true;
		for (ActivityLifecycleController alc : lifecycleControllers) {
			alc.onActivityDestroy();
		}
	}

	@Override
	public void runOnUiThreadUnlessDestroyed(Runnable r) {
		runOnUiThread(() -> {
			if (!destroyed && !isFinishing()) r.run();
		});
	}

	@UiThread
	public void handleDbException(DbException e) {
		supportFinishAfterTransition();
	}

	private void findToolbar() {
		if (searchedForToolbar) return;
		View decorView = getWindow().getDecorView();
		if (decorView instanceof ViewGroup)
			toolbar = findToolbar((ViewGroup) decorView);
		searchedForToolbar = true;
	}

	@Nullable
	private Toolbar findToolbar(ViewGroup vg) {
		// Views inside tap-safe layouts are already protected
		for (int i = 0, len = vg.getChildCount(); i < len; i++) {
			View child = vg.getChildAt(i);
			if (child instanceof Toolbar) return (Toolbar) child;
			if (child instanceof ViewGroup) {
				Toolbar toolbar = findToolbar((ViewGroup) child);
				if (toolbar != null) return toolbar;
			}
		}
		return null;
	}

	@Override
	public void setContentView(@LayoutRes int layoutRes) {
		setContentView(getLayoutInflater().inflate(layoutRes, null));
	}
}
