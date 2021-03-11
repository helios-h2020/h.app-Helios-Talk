package eu.h2020.helios_social.happ.helios.talk;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.BaseActivity;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment.BaseFragmentListener;
import eu.h2020.helios_social.happ.helios.talk.fragment.ErrorFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager.StartResult;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class StartupFailureActivity extends BaseActivity implements
		BaseFragmentListener {

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.activity_fragment_container);
		handleIntent(getIntent());
	}

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	private void handleIntent(Intent i) {
		StartResult result =
				(StartResult) i.getSerializableExtra(
						HeliosTalkService.EXTRA_START_RESULT);
		int notificationId = i.getIntExtra(HeliosTalkService.EXTRA_NOTIFICATION_ID, -1);

		// cancel notification
		if (notificationId > -1) {
			Object o = getSystemService(NOTIFICATION_SERVICE);
			NotificationManager nm = (NotificationManager) requireNonNull(o);
			nm.cancel(notificationId);
		}

		// show proper error message
		String errorMsg;
		switch (result) {
			case DATA_TOO_OLD_ERROR:
				errorMsg =
						getString(R.string.startup_failed_data_too_old_error);
				break;
			case DATA_TOO_NEW_ERROR:
				errorMsg =
						getString(R.string.startup_failed_data_too_new_error);
				break;
			case DB_ERROR:
				errorMsg = getString(R.string.startup_failed_db_error);
				break;
			case SERVICE_ERROR:
				errorMsg = getString(R.string.startup_failed_service_error);
				break;
			default:
				throw new IllegalArgumentException();
		}
		showInitialFragment(ErrorFragment.newInstance(errorMsg));
	}

	@Override
	public void runOnDbThread(@NonNull Runnable runnable) {
		throw new UnsupportedOperationException();
	}

}
