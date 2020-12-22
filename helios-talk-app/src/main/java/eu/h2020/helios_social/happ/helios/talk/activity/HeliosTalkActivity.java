package eu.h2020.helios_social.happ.helios.talk.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.transition.Transition;
import android.view.Window;
import android.widget.CheckBox;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.account.UnlockActivity;
import eu.h2020.helios_social.happ.helios.talk.context.Themes;
import eu.h2020.helios_social.happ.helios.talk.controller.HeliosTalkController;
import eu.h2020.helios_social.happ.helios.talk.controller.DbController;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultHandler;
import eu.h2020.helios_social.happ.helios.talk.login.StartupActivity;
import eu.h2020.helios_social.happ.helios.talk.logout.ExitActivity;
import eu.h2020.helios_social.happ.android.LockManager;
//import eu.h2020.helios_social.helios.talk.api.context.ContextManager;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
//import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;
import static android.os.Build.VERSION.SDK_INT;
import static java.util.logging.Level.INFO;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class HeliosTalkActivity extends BaseActivity {

	private static final Logger LOG =
			Logger.getLogger(HeliosTalkActivity.class.getName());

	@Inject
	HeliosTalkController heliosTalkController;
	@Inject
	DbController dbController;
	@Inject
	protected LockManager lockManager;
	@Inject
	volatile ContextManager contextManager;
	@Inject
	volatile ContextualEgoNetwork egoNetwork;

	@Override
	public void onStart() {
		super.onStart();
		lockManager.onActivityStart();
		styleBasedOnContext(
				egoNetwork.getCurrentContext().getData().toString()
						.split("%")[1]);
	}

	protected void styleBasedOnContext(String contextId) {
		try {
			if (contextId.equals("All")) {
				setTheme(R.style.HeliosTheme);
			} else {
				Themes themes = new Themes(this);
				Integer color =
						contextManager.getContextColor(contextId);
				setTheme(themes.getTheme(color));
				if (getSupportActionBar() != null)
					getSupportActionBar().setBackgroundDrawable(
							new ColorDrawable(color));
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int request, int result,
			@Nullable Intent data) {
		super.onActivityResult(request, result, data);
		if (request == RequestCodes.REQUEST_PASSWORD) {
			// Recreate the activity so any DB tasks that failed before
			// signing in can be retried
			if (result == RESULT_OK) {
				if (LOG.isLoggable(INFO)) {
					LOG.info("Recreating " + getClass().getSimpleName()
							+ " after signing in");
				}
				recreate();
			}
		} else if (request == RequestCodes.REQUEST_UNLOCK &&
				result != RESULT_OK) {
			// We arrive here, if the user presses 'back'
			// in the Keyguard unlock screen, because UnlockActivity finishes.
			// If we don't finish here, isFinishing will be false in onResume()
			// and we launch a new UnlockActivity causing a loop.
			supportFinishAfterTransition();
			// If the result is OK, we don't need to do anything here
			// and can resume normally.
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!heliosTalkController.accountSignedIn() && !isFinishing()) {
			// Also check that the activity isn't finishing already.
			// This is possible if we finished in onActivityResult().
			// Launching another StartupActivity would cause a loop.
			LOG.info("Not signed in, launching StartupActivity");
			Intent i = new Intent(this, StartupActivity.class);
			startActivityForResult(i, RequestCodes.REQUEST_PASSWORD);
		} else if (lockManager.isLocked() && !isFinishing()) {
			// Also check that the activity isn't finishing already.
			// This is possible if we finished in onActivityResult().
			// Launching another UnlockActivity would cause a loop.
			LOG.info("Locked, launching UnlockActivity");
			Intent i = new Intent(this, UnlockActivity.class);
			startActivityForResult(i, RequestCodes.REQUEST_UNLOCK);
		} else if (SDK_INT >= 23) {
			heliosTalkController.hasDozed(new UiResultHandler<Boolean>(this) {
				@Override
				public void onResultUi(Boolean result) {
					if (result) {
						showDozeDialog(getString(R.string.warning_dozed,
								getString(R.string.app_name)));
					}
				}
			});
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		lockManager.onActivityStop();
	}

	protected boolean signedIn() {
		return heliosTalkController.accountSignedIn();
	}

	/**
	 * Sets the transition animations.
	 *
	 * @param enterTransition  used to move views into initial positions
	 * @param exitTransition   used to move views out when starting a <b>new</b> activity.
	 * @param returnTransition used when window is closing, because the activity is finishing.
	 */
	@RequiresApi(api = 21)
	public void setSceneTransitionAnimation(
			@Nullable Transition enterTransition,
			@Nullable Transition exitTransition,
			@Nullable Transition returnTransition) {
		// workaround for #1007
		if (UiUtils.isSamsung7()) {
			return;
		}
		if (enterTransition != null) UiUtils.excludeSystemUi(enterTransition);
		if (exitTransition != null) UiUtils.excludeSystemUi(exitTransition);
		if (returnTransition != null) UiUtils.excludeSystemUi(returnTransition);
		Window window = getWindow();
		window.setEnterTransition(enterTransition);
		window.setExitTransition(exitTransition);
		window.setReturnTransition(returnTransition);
	}

	/**
	 * This should be called after the content view has been added in onCreate()
	 *
	 * @param ownLayout true if the custom toolbar brings its own layout
	 * @return the Toolbar object or null if content view did not contain one
	 */
	@Nullable
	protected Toolbar setUpCustomToolbar(boolean ownLayout) {
		// Custom Toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowCustomEnabled(ownLayout);
			ab.setDisplayShowTitleEnabled(!ownLayout);
		}
		return toolbar;
	}

	protected void showDozeDialog(String message) {
		AlertDialog.Builder b =
				new AlertDialog.Builder(this, R.style.HeliosDialogTheme);
		b.setMessage(message);
		b.setView(R.layout.checkbox);
		b.setPositiveButton(R.string.fix,
				(dialog, which) -> {
					Intent i = UiUtils.getDozeWhitelistingIntent(
							HeliosTalkActivity.this);
					startActivityForResult(i,
							RequestCodes.REQUEST_DOZE_WHITELISTING);
					dialog.dismiss();
				});
		b.setNegativeButton(R.string.cancel,
				(dialog, which) -> dialog.dismiss());
		b.setOnDismissListener(dialog -> {
			CheckBox checkBox =
					((AlertDialog) dialog).findViewById(R.id.checkbox);
			if (checkBox.isChecked())
				heliosTalkController.doNotAskAgainForDozeWhiteListing();
		});
		b.show();
	}

	protected void signOut(boolean removeFromRecentApps,
			boolean deleteAccount) {
		if (heliosTalkController.accountSignedIn()) {
			// Don't use UiResultHandler because we want the result even if
			// this activity has been destroyed
			heliosTalkController.signOut(result -> runOnUiThread(
					() -> exit(removeFromRecentApps)), deleteAccount);
		} else {
			if (deleteAccount) heliosTalkController.deleteAccount();
			exit(removeFromRecentApps);
		}
	}

	private void exit(boolean removeFromRecentApps) {
		if (removeFromRecentApps) startExitActivity();
		else finishAndExit();
	}

	private void startExitActivity() {
		Intent i = new Intent(this, ExitActivity.class);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK
				| FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				| FLAG_ACTIVITY_NO_ANIMATION
				| FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
	}

	private void finishAndExit() {
		if (SDK_INT >= 21) finishAndRemoveTask();
		else supportFinishAfterTransition();
		LOG.info("Exiting");
		System.exit(0);
	}

	public void runOnDbThread(Runnable task) {
		dbController.runOnDbThread(task);
	}

	protected void finishOnUiThread() {
		runOnUiThreadUnlessDestroyed(this::supportFinishAfterTransition);
	}
}

