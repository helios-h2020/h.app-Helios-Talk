package eu.h2020.helios_social.happ.helios.talk.splash;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.IntRange;

import eu.h2020.helios_social.happ.helios.talk.BuildConfig;
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
/*		if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
*//*			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy
					.Builder()
					.detectAll()             // Checks for all violations
					.penaltyLog()            // Output violations via logging
					.build()
			);*//*

			StrictMode.setVmPolicy(new StrictMode.VmPolicy
					.Builder()
					.detectNonSdkApiUsage()  // Detect private API usage
					.penaltyLog()            // Output violations via logging
					.build()
			);
		}*/

		Bundle extras = getIntent().getExtras();
		String password;

		if (extras != null) {
			password = extras.getString("PASSWORD");
			accountManager.setUserPassword(password);
			// and get whatever type user account id is
		}
		if (SDK_INT >= 21) {
			getWindow().setExitTransition(new Fade());
		}

		setPreferencesDefaults();

		setContentView(R.layout.splash);


		if (isConnectedToInternet()) {
			start();
		} else{
			final AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle("No Internet Connection!")
					.setMessage(R.string.no_internet_connection)
					.setCancelable(false)
					.setPositiveButton("ok", (dialogInterface, i) -> {
						// Build intent that displays the App settings screen.
						if (isConnectedToInternet()) {
							start();
						} else {
							Toast.makeText(this,"You are not connected to the Internet.",Toast.LENGTH_SHORT).show();
						}
					}).create();


			dialog.setOnShowListener(new DialogInterface.OnShowListener() {

				@Override
				public void onShow(DialogInterface dialogInterface) {

					Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
					button.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View view) {
							if (isConnectedToInternet()) {
								start();
								dialog.dismiss();
							} else {
								showToast("You are not connected to the Internet.");
							}
							//Dismiss once everything is OK.
						}
					});
				}
			});
			dialog.show();
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

	//check internet connection
	public boolean isConnectedToInternet() {
		boolean result = false; // Returns connection type. 0: none; 1: mobile data; 2: wifi
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
			if (capabilities != null) {
				if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
					result = true;
				} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
					result = true;
				} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
					result = true;
				}
			}
		}
		return result;
	}

	public void start(){
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

	public void showToast(String message){
		Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
	}
}
