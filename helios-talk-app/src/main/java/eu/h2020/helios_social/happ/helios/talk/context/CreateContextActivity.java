package eu.h2020.helios_social.happ.helios.talk.context;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Calendar;
import java.util.Objects;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.sensor.SensorValueListener;
import eu.h2020.helios_social.core.sensor.ext.LocationSensor;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.SpatioTemporalContext;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextFactory;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.GeneralContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.LocationContextProxy;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.widget.Toast.LENGTH_LONG;
import static eu.h2020.helios_social.core.context.ext.TimeContext.REPEAT_DAILY;
import static eu.h2020.helios_social.core.context.ext.TimeContext.REPEAT_NONE;
import static eu.h2020.helios_social.core.context.ext.TimeContext.REPEAT_WEEKDAYS;
import static eu.h2020.helios_social.core.context.ext.TimeContext.REPEAT_WEEKENDS;
import static eu.h2020.helios_social.core.context.ext.TimeContext.REPEAT_WEEKLY;
import static eu.h2020.helios_social.modules.groupcommunications.context.ContextConstants.MAX_CONTEXT_NAME_LENGTH;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateContextActivity extends HeliosTalkActivity
		implements ColorPickerDialogListener,
		SensorValueListener {

	private static final Logger LOG =
			Logger.getLogger(CreateContextActivity.class.getName());

	private TextInputLayout nameEntryLayout;
	private EditText nameEntry;
	private Button createContextButton;
	private ProgressBar progress;
	private Button colorPickerButton;
	private Integer color;

	@Inject
	volatile ContextualEgoNetwork egoNetwork;
	@Inject
	volatile ContextManager contextManager;
	@Inject
	EventBus eventBus;
	@Inject
	ContextController controller;
	@Inject
	ContextFactory contextFactory;

	// Code used in requesting runtime permissions
	private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

	// Constant used in the location settings dialog
	public static final int REQUEST_CHECK_SETTINGS = 0x1;

	// Represents a geographical location
	private Location mCurrentLocation;

	// UI Widgets
	private Button mStartUpdatesButton;
	private TextView latitudeTextView;
	private TextView longitudeTextView;
	private TextView radiusTextView;
	private LinearLayout contextLocationLayout;
	private LinearLayout contextTimeLayout;
	private TextInputEditText startTime;
	private TextInputEditText endTime;
	private TimePickerDialog picker;
	private Spinner contextType;
	private Long startTimeMSec;
	private Long endTimeMSec;
	private Spinner repeatTypeSpinner;
	private int repeatTypeValue;

	// Tracks the status of the location updates request
	private Boolean mRequestingLocationUpdates;
	// Access the location sensor
	private LocationSensor mLocationSensor;


	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_context);

		color = getColor(R.color.helios_default_context_color);

		nameEntryLayout = findViewById(R.id.createContextNameLayout);
		nameEntry = findViewById(R.id.createContextNameEntry);
		nameEntry.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int lengthBefore, int lengthAfter) {
				enableOrDisableCreateButton();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		nameEntry.setOnEditorActionListener((v, actionId, e) -> {
			if (actionId == IME_ACTION_DONE ||
					UiUtils.enterPressed(actionId, e)) {
				createContext();
				return true;
			}
			return false;
		});

		// Locate the UI widgets.
		mStartUpdatesButton = findViewById(R.id.getCurrentLocation);
		latitudeTextView = findViewById(R.id.lat);
		longitudeTextView = findViewById(R.id.lng);
		radiusTextView = findViewById(R.id.radiusEntry);
		// Time UI widgets
		startTime = findViewById(R.id.start_time);
		endTime = findViewById(R.id.end_time);


		colorPickerButton = findViewById(R.id.ColorPickerButton);
		colorPickerButton.setOnClickListener(v -> pickContextColor());

		createContextButton = findViewById(R.id.createContextButton);
		createContextButton.setOnClickListener(v -> createContext());

		contextLocationLayout = findViewById(R.id.context_location_layout);
		contextTimeLayout = findViewById(R.id.context_time_layout);

		contextType = findViewById(R.id.context_type);
		contextType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
					contextLocationLayout.setVisibility(View.GONE);
					contextTimeLayout.setVisibility(View.GONE);
					startTime.setText("");
					endTime.setText("");
					startTimeMSec = null;
					endTimeMSec = null;
					latitudeTextView.setText("");
					longitudeTextView.setText("");
					radiusTextView.setText("");
				} else if (position == 1) {
					contextLocationLayout.setVisibility(View.VISIBLE);
					contextTimeLayout.setVisibility(View.GONE);
					startTime.setText("");
					endTime.setText("");
					startTimeMSec = null;
					endTimeMSec = null;
				} else if (position == 2) {
					contextLocationLayout.setVisibility(View.VISIBLE);
					contextTimeLayout.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		repeatTypeSpinner = findViewById(R.id.repeat_type_spinner);

		repeatTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
					repeatTypeValue = REPEAT_DAILY;
				} else if (position == 1) {
					repeatTypeValue = REPEAT_WEEKLY;
				} else if (position == 2) {
					repeatTypeValue = REPEAT_WEEKDAYS;
				} else if (position == 3) {
					repeatTypeValue = REPEAT_WEEKENDS;
				} else if (position == 4) {
					repeatTypeValue = REPEAT_NONE;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		progress = findViewById(R.id.createContextProgressBar);



		mStartUpdatesButton
				.setOnClickListener(v -> startUpdatesButtonHandler());

		mRequestingLocationUpdates = false;


		// Init LocationSensor
		mLocationSensor = new LocationSensor(this);
		// Only for demo UI to obtain updates to location coordinates via ValueListener
		mLocationSensor.registerValueListener(this);


		startTime.setInputType(InputType.TYPE_NULL);
		startTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Calendar cldr = Calendar.getInstance();
				int hour = cldr.get(Calendar.HOUR_OF_DAY);
				int minutes = cldr.get(Calendar.MINUTE);
				// time picker dialog
				picker = new TimePickerDialog(CreateContextActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
								startTime.setText(sHour + ":" + sMinute);
								startTimeMSec = timeToMilisec(sHour,sMinute);
							}
						}, hour, minutes, true);
				picker.show();
			}
		});

		endTime.setInputType(InputType.TYPE_NULL);
		endTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Calendar cldr = Calendar.getInstance();
				int hour = cldr.get(Calendar.HOUR_OF_DAY);
				int minutes = cldr.get(Calendar.MINUTE);
				// time picker dialog
				picker = new TimePickerDialog(CreateContextActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
								endTime.setText(sHour + ":" + sMinute);
								endTimeMSec = timeToMilisec(sHour,sMinute);
							}
						}, hour, minutes, true);
				picker.show();
			}
		});
	}

	private Long timeToMilisec(int hourOfDay, int minute){
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTimeInMillis();
	}

	/**
	 * This method implements the SensorValueListener interface receiveValue method, which
	 * obtains values from the location sensor.
	 *
	 * @param location - a Location value
	 */
	@Override
	public void receiveValue(Object location) {
		// updates the current location
		mCurrentLocation = (Location) location;
		Log.i("Context", "Receive Value");
		updateUI();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_CHECK_SETTINGS:
				switch (resultCode) {
					case Activity.RESULT_OK:
						break;
					case Activity.RESULT_CANCELED:
						mRequestingLocationUpdates = false;
						updateUI();
						break;
				}
				break;
		}
	}

	/**
	 * Handles the Start Context Updates button
	 */
	public void startUpdatesButtonHandler() {
		if (!mRequestingLocationUpdates) {
			mRequestingLocationUpdates = true;
			mLocationSensor.startUpdates();
			LOG.info("updateLocationStarted");
		}
	}

	/**
	 * Updates all UI fields.
	 */
	private void updateUI() {
		updateContextUI();
	}


	/**
	 * Sets values for the UI fields
	 */
	private void updateContextUI() {
		if (mCurrentLocation != null) {
			latitudeTextView.setText(
					String.valueOf(mCurrentLocation.getLatitude()));
			longitudeTextView
					.setText(String.valueOf(mCurrentLocation.getLongitude()));
			mLocationSensor.stopUpdates();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!checkPermissions()) {
			requestPermissions();
		}
	}

	private void pickContextColor() {

		Themes themes = new Themes(this);
		ColorPickerDialog.newBuilder()
				.setDialogType(ColorPickerDialog.TYPE_PRESETS)
				.setPresets(themes.getColors())
				.setShowAlphaSlider(false)
				.setAllowCustom(false)
				.setShowColorShades(false)
				.setColor(getColor(R.color.helios_default_context_color))
				.show(this);
	}

	private void enableOrDisableCreateButton() {
		if (createContextButton == null) return; // Not created yet
		createContextButton.setEnabled(validateName());
	}

	private boolean validateName() {
		String name = nameEntry.getText().toString();
		int length = StringUtils.toUtf8(name).length;
		if (length > MAX_CONTEXT_NAME_LENGTH) {
			nameEntryLayout.setError(getString(R.string.name_too_long));
			return false;
		}
		nameEntryLayout.setError(null);
		return length > 0;
	}

	private boolean validateInputs() {
		int activeItem = contextType.getSelectedItemPosition();
		if (activeItem==0) {
			boolean validateNameBoolean = validateName();
			return validateNameBoolean;
		} else if (activeItem==1){
			boolean validateNameBoolean = validateName();
			boolean validateLocationBoolean = validateLocation();
			return (validateNameBoolean && validateLocationBoolean);
		} else if (activeItem==2){
			boolean validateNameBoolean = validateName();
			boolean validateLocationBoolean = validateLocation();
			boolean validateTimeBoolean = validateTime();
			return (validateNameBoolean && validateLocationBoolean && validateTimeBoolean);
		}
		return false;
	}

	private boolean validateLocation(){
		Double lat = latitudeTextView.getText().toString().equals("") ? null :
				Double.parseDouble(latitudeTextView.getText().toString());
		Double lng = longitudeTextView.getText().toString().equals("") ? null :
				Double.parseDouble(longitudeTextView.getText().toString());
		return (lat != null && lng != null);
	}

	private boolean validateTime(){
		if (startTimeMSec != null && endTimeMSec!=null){
			return startTimeMSec < endTimeMSec;
		}
		return false;
	}

	@Override
	public void onBackPressed() {

		int count = getSupportFragmentManager().getBackStackEntryCount();
		if (count == 0) {
			super.onBackPressed();
			//additional code
		} else {
			getSupportFragmentManager().popBackStack();
		}
	}

	private void createContext() {
		if (!validateInputs()) {
			Toast.makeText(this,"Invalid inputs",Toast.LENGTH_SHORT).show();
			return;
		}
		UiUtils.hideSoftKeyboard(nameEntry);
		createContextButton.setVisibility(GONE);
		progress.setVisibility(VISIBLE);
		String name = nameEntry.getText().toString();

		Double lat = latitudeTextView.getText().toString().equals("") ? null :
				Double.parseDouble(latitudeTextView.getText().toString());
		Double lng = longitudeTextView.getText().toString().equals("") ? null :
				Double.parseDouble(longitudeTextView.getText().toString());

		Integer radius = radiusTextView.getText().toString().equals("") ? 200 :
				Integer.parseInt(radiusTextView.getText().toString());

		if (lat != null && lng != null && startTimeMSec != null && endTimeMSec != null) {
			SpatioTemporalContext spatioTemporalContext =
					contextFactory.createSpatioTemporalContext("", color, startTimeMSec,
							endTimeMSec, repeatTypeValue, lat, lng, radius, name);
			controller.storeSpatioTemporalContext(spatioTemporalContext,
					new UiResultExceptionHandler<String, DbException>(
							this) {
						@Override
						public void onResultUi(String name) {
						}

						@Override
						public void onExceptionUi(DbException exception) {
							handleDbException(exception);
						}
					});
		}else if (lat != null && lng != null) {
			LocationContextProxy locationContextProxy =
					contextFactory.createLocationContext("", color, lat, lng,
							radius, name);
			controller.storeLocationContext(locationContextProxy,
					new UiResultExceptionHandler<String, DbException>(
							this) {
						@Override
						public void onResultUi(String name) {
						}

						@Override
						public void onExceptionUi(DbException exception) {
							handleDbException(exception);
						}
					});
		} else {
			// leave public name empty
			GeneralContextProxy generalContextProxy =
					contextFactory.createContext("", color, name);
			controller.storeGeneralContext(generalContextProxy,
					new UiResultExceptionHandler<String, DbException>(
							this) {
						@Override
						public void onResultUi(String name) {
						}

						@Override
						public void onExceptionUi(DbException exception) {
							handleDbException(exception);
						}
					});
		}

		Toast.makeText(CreateContextActivity.this,
				R.string.context_created_toast,
				LENGTH_LONG)
				.show();
		Intent intent = new Intent(this, NavDrawerActivity.class);
		intent.setFlags(
				FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public void onColorSelected(int dialogId, int color) {
		Toast.makeText(this, "Selected Color: #" +
						Integer.toHexString(color),
				Toast.LENGTH_SHORT).show();
		this.color = color;
		Drawable unwrappedDrawable =
				AppCompatResources.getDrawable(this, R.drawable.color_picker);
		Drawable wrappedDrawable = DrawableCompat.wrap(Objects.requireNonNull(unwrappedDrawable));
		wrappedDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		colorPickerButton.setBackground(wrappedDrawable);
	}

	@Override
	public void onDialogDismissed(int dialogId) {

	}


	private void showSnackbar(final int mainTextStringId,
			final int actionStringId,
			View.OnClickListener listener) {
		Snackbar.make(
				findViewById(android.R.id.content),
				getString(mainTextStringId),
				Snackbar.LENGTH_INDEFINITE)
				.setAction(getString(actionStringId), listener).show();
	}

	/**
	 * Return the current state of the permissions needed.
	 */
	private boolean checkPermissions() {
		int permissionState = ActivityCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION);
		return permissionState == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * Request permissions to access location
	 */
	private void requestPermissions() {
		boolean shouldProvideRationale =
				ActivityCompat.shouldShowRequestPermissionRationale(this,
						Manifest.permission.ACCESS_FINE_LOCATION);

		if (shouldProvideRationale) {
			showSnackbar(R.string.location_permission_prompt,
					android.R.string.ok, view -> {
						// Request permission
						ActivityCompat
								.requestPermissions(
										CreateContextActivity.this,
										new String[] {
												Manifest.permission.ACCESS_FINE_LOCATION},
										REQUEST_PERMISSIONS_REQUEST_CODE);
					});
		} else {
			ActivityCompat.requestPermissions(CreateContextActivity.this,
					new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
					REQUEST_PERMISSIONS_REQUEST_CODE);
		}
	}

	/**
	 * Callback received when a permissions request has been completed.
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode,
			@NonNull String[] permissions,
			@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		LOG.info("onRequestPermissionResult");
		if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
			if (grantResults.length <= 0) {
				LOG.info("User interaction was cancelled.");
			} else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if (mRequestingLocationUpdates) {
					LOG.info(
							"Permission granted, updates requested, starting location updates");
					mLocationSensor.startUpdates();
				}
			}
		}
	}
}
