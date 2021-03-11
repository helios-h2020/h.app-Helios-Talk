package eu.h2020.helios_social.happ.helios.talk.login;

import android.content.Intent;
import android.os.Bundle;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.HeliosTalkService;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.account.SetupActivity;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.BaseActivity;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment.BaseFragmentListener;
import eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.SIGNED_IN;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.SIGNED_OUT;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.STARTED;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.STARTING;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class StartupActivity extends BaseActivity implements
		BaseFragmentListener {

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private StartupViewModel viewModel;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_fragment_container);

		viewModel = ViewModelProviders.of(this, viewModelFactory)
				.get(StartupViewModel.class);
		if (!viewModel.accountExists()) {
			// TODO ideally we would not have to delete the account again
			// The account needs to deleted again to remove the database folder,
			// because if it exists, we assume the database also exists
			// and when clearing app data, the folder does not get deleted.
			viewModel.deleteAccount();
			onAccountDeleted();
			return;
		}
		viewModel.getAccountDeleted().observeEvent(this, deleted -> {
			if (deleted) onAccountDeleted();
		});
		viewModel.getState().observe(this, this::onStateChanged);

	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.clearSignInNotification();
	}

	@Override
	public void onBackPressed() {
		// Move task and activity to the background instead of showing another
		// password prompt.
		// onActivityResult() won't be called in HeliosTalkActivity
		moveTaskToBack(true);
	}

	private void onStateChanged(State state) {
		if (state == SIGNED_OUT) {
			// Configuration changes such as screen rotation
			// can cause this to get called again.
			// Don't replace the fragment in that case to not lose view state.
			if (!isFragmentAdded(PasswordFragment.TAG)) {
				showInitialFragment(new PasswordFragment());
			}
		} else if (state == SIGNED_IN || state == STARTING) {
			startService(new Intent(this, HeliosTalkService.class));
			// Only show OpenDatabaseFragment if not already visible.
			if (!isFragmentAdded(OpenDatabaseFragment.TAG)) {
				showNextFragment(new OpenDatabaseFragment());
			}
		} else if (state == STARTED) {
			setResult(RESULT_OK);
			supportFinishAfterTransition();
			overridePendingTransition(R.anim.screen_new_in,
					R.anim.screen_old_out);
		}
	}

	private void onAccountDeleted() {
		setResult(RESULT_CANCELED);
		finish();
		Intent i = new Intent(this, SetupActivity.class);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP |
				FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_TASK_ON_HOME);
		startActivity(i);
	}

	@Override
	public void runOnDbThread(Runnable runnable) {
		// we don't need this and shouldn't be forced to implement it
		throw new UnsupportedOperationException();
	}

}
