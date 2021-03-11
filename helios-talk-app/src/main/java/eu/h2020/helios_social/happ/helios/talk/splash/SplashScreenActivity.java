package eu.h2020.helios_social.happ.helios.talk.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;

import eu.h2020.helios_social.modules.groupcommunications_utils.account.AccountManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.BaseActivity;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.HeliosTalkApplication;
import eu.h2020.helios_social.happ.helios.talk.android.system.AndroidExecutor;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION.SDK_INT;
import static androidx.preference.PreferenceManager.setDefaultValues;
import static java.util.logging.Logger.getLogger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SplashScreenActivity extends BaseActivity {

	private static final Logger LOG =
			getLogger(SplashScreenActivity.class.getName());

	@Inject
	protected AccountManager accountManager;
	@Inject
	protected AndroidExecutor androidExecutor;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);

		if (SDK_INT >= 21) {
			getWindow().setExitTransition(new Fade());
		}

		setPreferencesDefaults();

		setContentView(R.layout.splash);

		if (accountManager.hasDatabaseKey()) {
			startNextActivity(HeliosTalkApplication.ENTRY_ACTIVITY);
			finish();
		} else {
			new Handler().postDelayed(() -> {
				startNextActivity(HeliosTalkApplication.ENTRY_ACTIVITY);
				supportFinishAfterTransition();
			}, 500);
		}
	}

	private void startNextActivity(Class<? extends Activity> activityClass) {
		Intent i = new Intent(this, activityClass);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	private void setPreferencesDefaults() {
		androidExecutor.runOnBackgroundThread(
				() -> setDefaultValues(SplashScreenActivity.this,
						R.xml.panic_preferences, false));
	}

}
