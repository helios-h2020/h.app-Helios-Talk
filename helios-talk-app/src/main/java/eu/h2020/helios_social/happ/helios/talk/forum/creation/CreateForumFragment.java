package eu.h2020.helios_social.happ.helios.talk.forum.creation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.logging.Logger;

import eu.h2020.helios_social.core.sensor.SensorValueListener;
import eu.h2020.helios_social.core.sensor.ext.LocationSensor;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.happ.helios.talk.profile.TagEditText;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static eu.h2020.helios_social.happ.helios.talk.context.CreateContextActivity.REQUEST_CHECK_SETTINGS;
import static java.util.logging.Logger.getLogger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateForumFragment extends BaseFragment implements SensorValueListener {
    private static final Logger LOG =
            getLogger(CreateForumFragment.class.getName());

    public final static String TAG = CreateForumFragment.class.getName();
    private static final int MAX_FORUM_NAME_LENGTH = 75;
    // Code used in requesting runtime permissions
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private CreateForumListener listener;
    private EditText nameEntry;
    private EditText lat;
    private EditText lng;
    private EditText radius;
    private TagEditText tags;
    private Button mStartUpdatesButton;
    private Button createGroupButton;
    private TextInputLayout nameLayout;
    private Spinner forum_type;
    private Spinner group_type;
    private Spinner defaultForumMemberRole;
    private ProgressBar progress;
    private LinearLayout locationLayout;
    // Tracks the status of the location updates request
    private Boolean mRequestingLocationUpdates;
    // Access the location sensor
    private LocationSensor mLocationSensor;
    private Location mCurrentLocation;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (CreateForumListener) context;
    }

    @Override
    public void injectFragment(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_create_forum, container,
                false);

        nameEntry = v.findViewById(R.id.createForumNameEntry);
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
        nameEntry.setOnEditorActionListener((v1, actionId, e) -> {
            if (actionId == IME_ACTION_DONE || UiUtils.enterPressed(actionId, e)) {
                createForum();
                return true;
            }
            return false;
        });

        nameLayout = v.findViewById(R.id.nameLayout);

        createGroupButton = v.findViewById(R.id.createForumButton);
        createGroupButton.setOnClickListener(v1 -> createForum());

        locationLayout = v.findViewById(R.id.forum_location_layout);

        tags = v.findViewById(R.id.tags);

        forum_type = v.findViewById(R.id.forum_type);
        forum_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    locationLayout.setVisibility(View.GONE);
                } else if (position == 1) {
                    locationLayout.setVisibility(View.VISIBLE);
                    enableOrDisableCreateButton();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        group_type = v.findViewById(R.id.groupType);

        lat = v.findViewById(R.id.lat);
        lng = v.findViewById(R.id.lng);
        lat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableOrDisableCreateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lng.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableOrDisableCreateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        radius = v.findViewById(R.id.radius);

        radius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableOrDisableCreateButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        progress = v.findViewById(R.id.progressBar);

        mStartUpdatesButton = v.findViewById(R.id.getCurrentLocation);

        mRequestingLocationUpdates = false;
        mStartUpdatesButton
                .setOnClickListener(l -> startUpdatesButtonHandler());

        defaultForumMemberRole = v.findViewById(R.id.default_member_role);

        // Init LocationSensor
        mLocationSensor = new LocationSensor(getActivity());
        // Only for demo UI to obtain updates to location coordinates via ValueListener
        mLocationSensor.registerValueListener(this);

        progress = v.findViewById(R.id.createForumProgressBar);

        return v;
    }

    @Override
    public String getUniqueTag() {
        return TAG;
    }

    private void enableOrDisableCreateButton() {
        if (createGroupButton == null) return; // Not created yet
        if (forum_type.getSelectedItemPosition() == 0)
            createGroupButton.setEnabled(validateName());
        else if (forum_type.getSelectedItemPosition() == 1) {
            createGroupButton.setEnabled(validateName() && validateLocation());
        }
    }

    private boolean validateName() {
        String name = nameEntry.getText().toString();
        int length = StringUtils.toUtf8(name).length;
        if (length > MAX_FORUM_NAME_LENGTH) {
            nameLayout.setError(getString(R.string.name_too_long));
            return false;
        }
        nameLayout.setError(null);
        return length > 3;
    }

    private boolean validateLocation() {
        if (!lat.getText().toString().isEmpty() && !lng.getText().toString().isEmpty() && !radius.getText().toString().isEmpty())
            return true;
        else
            return false;
    }

    private void createForum() {
        if (forum_type.getSelectedItemPosition() == 0) {
            LOG.info("Creating Named Forum!");
            if (!validateName()) return;
            UiUtils.hideSoftKeyboard(nameEntry);
            createGroupButton.setVisibility(GONE);
            progress.setVisibility(VISIBLE);

            listener.onNamedForumChosen(
                    nameEntry.getText().toString(),
                    GroupType.fromValue(2 + group_type.getSelectedItemPosition()),
                    tags.getTags(),
                    getForumMemberRole(defaultForumMemberRole.getSelectedItemPosition())
            );
        } else if (forum_type.getSelectedItemPosition() == 1) {
            LOG.info("Creating Location Forum!");
            if (!validateName() || !validateLocation()) return;
            UiUtils.hideSoftKeyboard(nameEntry);
            createGroupButton.setVisibility(GONE);
            progress.setVisibility(VISIBLE);

            listener.onLocationForumChosen(
                    nameEntry.getText().toString(),
                    GroupType.fromValue(2 + group_type.getSelectedItemPosition()),
                    tags.getTags(),
                    Double.parseDouble(lat.getText().toString()),
                    Double.parseDouble(lng.getText().toString()),
                    Integer.parseInt(radius.getText().toString()),
                    getForumMemberRole(defaultForumMemberRole.getSelectedItemPosition())
            );
        }

    }

    @Override
    public void receiveValue(Object location) {
        // updates the current location
        mCurrentLocation = (Location) location;
        LOG.info("location updates: " + mCurrentLocation);
        lat.setText(String.valueOf(mCurrentLocation.getLatitude()));
        lng.setText(String.valueOf(mCurrentLocation.getLongitude()));
        mLocationSensor.stopUpdates();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        mRequestingLocationUpdates = false;
                        if (mCurrentLocation != null) {
                            lat.setText(String.valueOf(mCurrentLocation.getLatitude()));
                            lng.setText(String.valueOf(mCurrentLocation.getLongitude()));
                            mLocationSensor.stopUpdates();
                        }
                        break;
                }
                break;
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        LOG.info("check permissions");
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void showSnackbar(final int mainTextStringId,
                              final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Handles the Start Context Updates button
     */
    public void startUpdatesButtonHandler() {
        LOG.info("starting location updates...!");
        if (!checkPermissions()) {
            requestPermissions();
        }
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            mLocationSensor.startUpdates();
        }
    }

    /**
     * Request permissions to access location
     */
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            showSnackbar(R.string.location_permission_prompt,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat
                                    .requestPermissions(
                                            getActivity(),
                                            new String[]{
                                                    Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
            } else {

            }
        }
    }

    private ForumMemberRole getForumMemberRole(int i) {
        if (i == 0) return ForumMemberRole.EDITOR;
        else return ForumMemberRole.READER;
    }
}
