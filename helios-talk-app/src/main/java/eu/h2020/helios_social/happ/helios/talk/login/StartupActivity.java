package eu.h2020.helios_social.happ.helios.talk.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import eu.h2020.helios_social.modules.groupcommunications_utils.account.AccountManager;
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


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
	@Inject
	AccountManager accountManager;
	private StartupViewModel viewModel;
	protected ArrayList<String> relay_addresses_list;
	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_fragment_container);

		new JsonTask().execute(getResources().getString(R.string.relay_addresses_link));
		// set relay addresses and pass them to shared preferences. Messaging libp2p gets those
		// addresses and establishes the connection.
		SharedPreferences nodePrefs = getSharedPreferences("helios-node-libp2p-prefs", MODE_PRIVATE);
		SharedPreferences.Editor editor = nodePrefs.edit();
		editor.putString("swarmKeyProtocol", getResources().getString(R.string.swarmKeyProtocol));
		editor.putString("swarmKeyEncoding", getResources().getString(R.string.swarmKeyEncoding));
		editor.putString("swarmKeyData", getResources().getString(R.string.swarmKeyData));
		editor.commit();

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
		Log.d("state is: ", String.valueOf(state));
		if (state == SIGNED_OUT) {
			// Configuration changes such as screen rotation
			// can cause this to get called again.
			// Don't replace the fragment in that case to not lose view state.
			if (accountManager.getUserPassword()!=null){
				Log.d("validating the password",accountManager.getUserPassword());
				viewModel.validatePassword(accountManager.getUserPassword());
			}
			else if (!isFragmentAdded(PasswordFragment.TAG)) {
				showInitialFragment(new PasswordFragment());
			}
		} else if (state == SIGNED_IN || state == STARTING) {
			Log.d("get the database password", String.valueOf(accountManager.getDatabaseKey()));
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


	private class JsonTask extends AsyncTask<String, String, String> {

		protected void onPreExecute() {
			super.onPreExecute();

		}

		protected String doInBackground(String... params) {


			HttpURLConnection connection = null;
			BufferedReader reader = null;

			try {
				URL url = new URL(params[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();


				InputStream stream = connection.getInputStream();

				reader = new BufferedReader(new InputStreamReader(stream));

				StringBuffer buffer = new StringBuffer();
				String line = "";

				while ((line = reader.readLine()) != null) {
					buffer.append(line+"\n");
					Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

				}

				return buffer.toString();


			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				if (result==null) return;
				JSONObject json = new JSONObject(result);
				Log.d("jsonResult = ", String.valueOf(json));

				relay_addresses_list = new ArrayList<>();
				JSONArray st = json.getJSONArray("url");
				for(int i=0;i<st.length();i++)
				{
					relay_addresses_list.add(st.getString(i));
				}
				SharedPreferences nodePrefs = getSharedPreferences("helios-node-libp2p-prefs", MODE_PRIVATE);
				SharedPreferences.Editor editor = nodePrefs.edit();
				if(relay_addresses_list != null) {
					Log.d("TAG", "set relay addresses: " + String.valueOf(relay_addresses_list));
					HashSet<String> addresses = new HashSet<String>(relay_addresses_list);
					editor.putStringSet("relayAddresses", addresses);
					editor.commit();
				} else {
					Log.d("TAG", "No relay addresses");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}




}
