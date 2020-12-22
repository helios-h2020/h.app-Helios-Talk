package eu.h2020.helios_social.happ.helios.talk.navdrawer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.profile.ProfileActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.happ.helios.talk.api.event.Event;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventListener;
import eu.h2020.helios_social.happ.helios.talk.api.identity.IdentityManager;
import eu.h2020.helios_social.happ.helios.talk.api.lifecycle.LifecycleManager;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.api.sync.event.ContextAddedEvent;
import eu.h2020.helios_social.happ.helios.talk.api.sync.event.ContextRemovedEvent;
import eu.h2020.helios_social.happ.helios.talk.chat.ChatListFragment;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactListFragment;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultHandler;
import eu.h2020.helios_social.happ.helios.talk.favourites.FavouritesFragment;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.happ.helios.talk.logout.SignOutFragment;
import eu.h2020.helios_social.happ.helios.talk.settings.SettingsActivity;

import static androidx.core.view.GravityCompat.START;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import eu.h2020.helios_social.core.context.ext.LocationContext;
import eu.h2020.helios_social.core.sensor.ext.LocationSensor;
import eu.h2020.helios_social.happ.helios.talk.HeliosTalkService;
import eu.h2020.helios_social.happ.helios.talk.activity.RequestCodes;
import eu.h2020.helios_social.modules.groupcommunications.api.context.DBContext;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Logger.getLogger;
import static eu.h2020.helios_social.happ.helios.talk.api.lifecycle.LifecycleManager.LifecycleState.RUNNING;
import static eu.h2020.helios_social.happ.helios.talk.navdrawer.IntentRouter.handleExternalIntent;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class NavDrawerActivity extends HeliosTalkActivity implements
		BaseFragment.BaseFragmentListener,
		NavigationView.OnNavigationItemSelectedListener, EventListener {

	// Code used in requesting runtime permissions
	private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

	// Constant used in the location settings dialog
	private static final int REQUEST_CHECK_SETTINGS = 0x1;

	// Tracks the status of the location updates request
	private Boolean mRequestingLocationUpdates;
	// Access the location sensor
	private LocationSensor mLocationSensor;
	private ArrayList<LocationContext> locationContexts;

	// Represents a geographical location
	private Location currentLocation;

	private static final Logger LOG =
			getLogger(NavDrawerActivity.class.getName());

	private ActionBarDrawerToggle drawerToggle;

	@Inject
	NavDrawerController controller;
	@Inject
	LifecycleManager lifecycleManager;
	@Inject
	volatile ContextualEgoNetwork egoNetwork;
	@Inject
	volatile ContextManager contextManager;
	@Inject
	EventBus eventBus;
	@Inject
	IdentityManager identityManager;


	private DrawerLayout drawerLayout;
	private NavigationView navigation;
	private SubMenu contextMenu;

	private BottomNavigationView bottomNav;
	private ArrayList<DBContext> contexts;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		exitIfStartupFailed(getIntent());
		setContentView(R.layout.activity_nav_drawer);
		locationContexts = new ArrayList<>();
		mRequestingLocationUpdates = false;

		Toolbar toolbar = findViewById(R.id.toolbar);
		drawerLayout = findViewById(R.id.drawer_layout);
		navigation = findViewById(R.id.navigation);
		MenuItem menuNav = navigation.getMenu().getItem(0);
		contextMenu = menuNav.getSubMenu();
		// Init LocationSensor
		/*mLocationSensor = new LocationSensor(this);
		// Only for demo UI to obtain updates to location coordinates via ValueListener
		mLocationSensor.registerValueListener(this);

		mLocationSensor.startUpdates();*/
		updateContexts("All");

		eventBus.addListener(this);

		//GridView transportsView = findViewById(R.id.transportsView);

		setSupportActionBar(toolbar);
		ActionBar actionBar = requireNonNull(getSupportActionBar());
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
				R.string.nav_drawer_open_description,
				R.string.nav_drawer_close_description);
		drawerLayout.addDrawerListener(drawerToggle);

		navigation.setNavigationItemSelectedListener(this);

		bottomNav = findViewById(R.id.bottom_navigation);
		bottomNav.setOnNavigationItemSelectedListener(navListener);

		//initializeTransports(getLayoutInflater());
		//transportsView.setAdapter(transportsAdapter);

		lockManager.isLockable().observe(this, this::setLockVisible);

		if (lifecycleManager.getLifecycleState().isAfter(RUNNING)) {
			showSignOutFragment();
		} else if (state == null) {
			startFragment(ChatListFragment.newInstance(),
					0);
		}
		if (state == null) {
			// do not call this again when there's existing state
			onNewIntent(getIntent());
		}
	}

	public void updateContexts(String id) {
		contextMenu.clear();
		contexts = new ArrayList();
		try {
			contexts = (ArrayList) contextManager.getContexts();
			LOG.info("Updating contexts menu: " + contexts.size());
			for (int i = 0; i < contexts.size(); i++) {
				DBContext c = contexts.get(i);
				boolean active = false;
				if (c.getId().equals(id))
					active = true;
				contextMenu.add(0, i,
						Menu.CATEGORY_SECONDARY,
						c.getName())
						.setIcon(R.drawable.ic_context_2)
						.setChecked(active);

				/*if (findLocationContextByName(locationContexts,
						c.getContextName()) == null &&
						c.getLatitude() != null) {
					LocationContext lc = new LocationContext(
							contexts.get(i).getContextName(),
							c.getLatitude(), c.getLongitude(),
							c.getRadius()
					);
					lc.registerContextListener(this);
					mLocationSensor.registerValueListener(lc);
					locationContexts.add(lc);
			}*/
			}
			contextMenu.setGroupCheckable(0, true, true);
		} catch (
				DbException e) {
			e.printStackTrace();
		}
		navigation.invalidate();
	}

	private BottomNavigationView.OnNavigationItemSelectedListener navListener =
			new BottomNavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(
						@NonNull MenuItem item) {
					BaseFragment selectedFragment = null;
					switch (item.getItemId()) {
						case R.id.nav_conversations:
							selectedFragment = ChatListFragment.newInstance();
							break;
						case R.id.nav_favourites:
							selectedFragment =
									FavouritesFragment.newInstance();
							break;
						case R.id.nav_contacts:
							selectedFragment =
									ContactListFragment.newInstance();
							break;
					}

					getSupportFragmentManager().beginTransaction()
							.replace(R.id.fragmentContainer,
									selectedFragment).commit();
					return true;
				}
			};

	@Override
	@SuppressLint("NewApi")
	public void onStart() {
		super.onStart();
		/*updateTransports();
		lockManager.checkIfLockable();
		if (mRequestingLocationUpdates && checkPermissions()) {
			mLocationSensor.startUpdates();
		} else if (!checkPermissions()) {
			requestPermissions();
		}*/
	}

	@Override
	public void onResume() {
		super.onResume();
		System.out.println("ONRESUME");
		styleBasedOnContext(
				egoNetwork.getCurrentContext().getData().toString()
						.split("%")[1]);
		/*if (mRequestingLocationUpdates && checkPermissions()) {
			mLocationSensor.startUpdates();
		} else if (!checkPermissions()) {
			requestPermissions();
		}*/
	}


	@Override
	protected void onActivityResult(int request, int result,
			@Nullable Intent data) {
		super.onActivityResult(request, result, data);
		if (request == RequestCodes.REQUEST_PASSWORD && result == RESULT_OK) {
			controller.shouldAskForDozeWhitelisting(this,
					new UiResultHandler<Boolean>(this) {
						@Override
						public void onResultUi(Boolean ask) {
							if (ask) {
								showDozeDialog(
										getString(R.string.setup_doze_intro));
							}
						}
					});
		}

		switch (request) {
			case REQUEST_CHECK_SETTINGS:
				switch (request) {
					case Activity.RESULT_OK:
						break;
					case Activity.RESULT_CANCELED:
						mRequestingLocationUpdates = false;
						break;
				}
				break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// will call System.exit()
		exitIfStartupFailed(intent);

		if ("helios-content".equals(intent.getScheme())) {
			//handleContentIntent(intent);
		} else {
			handleExternalIntent(this, intent);
		}
	}

	private void exitIfStartupFailed(Intent intent) {
		if (intent.getBooleanExtra(HeliosTalkService.EXTRA_STARTUP_FAILED,
				false)) {
			finish();
			LOG.info("Exiting");
			System.exit(0);
		}
	}

	private void loadFragment(int fragmentId) {
		// TODO re-use fragments from the manager when possible (#606)
		if (R.id.nav_btn_profile == fragmentId) {
			startActivity(new Intent(this, ProfileActivity.class));
		} else if (R.id.nav_btn_settings == fragmentId) {
			startActivity(new Intent(this, SettingsActivity.class));
		} else if (R.id.nav_btn_stats == fragmentId) {
			//startActivity(new Intent(this, StatsActivity.class));
		} else if (R.id.nav_btn_signout == fragmentId) {
			signOut();
		} else {
			int index = contextMenu.findItem(fragmentId).getItemId();

			DBContext current = contexts.get(index);
			egoNetwork.setCurrent(egoNetwork.getOrCreateContext(
					current.getName() + "%" + current.getId()));

			bottomNav.setSelectedItemId(R.id.nav_conversations);
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		drawerLayout.closeDrawer(START);
		clearBackStack();
		if (item.getItemId() == R.id.nav_btn_lock) {
			lockManager.setLocked(true);
			ActivityCompat.finishAfterTransition(this);
			return false;
		} else {
			loadFragment(item.getItemId());
			// Don't display the Settings item as checked
			return item.getItemId() != R.id.nav_btn_settings;
		}
	}

	@Override
	public void onBackPressed() {
		/*if (drawerLayout.isDrawerOpen(START)) {
			drawerLayout.closeDrawer(START);
		} else {
			FragmentManager fm = getSupportFragmentManager();
			if (fm.findFragmentByTag(SignOutFragment.TAG) != null) {
				finish();
			} else if (fm.getBackStackEntryCount() == 0
					&&
					fm.findFragmentByTag(ChatListFragment.TAG) == null) {
				/*
				 * This makes sure that the first fragment (ContactListFragment) the
				 * user sees is the same as the last fragment the user sees before
				 * exiting. This models the typical Google navigation behaviour such
				 * as in Gmail/Inbox.
				 */
			/*	startFragment(ChatListFragment.newInstance(),
						0);
			} else {
				super.onBackPressed();
			}
		}*/
	}

	@Override
	public void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	private void showSignOutFragment() {
		drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
		startFragment(new SignOutFragment());
	}

	private void signOut() {
		egoNetwork.save();
		drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
		signOut(false, false);
		finish();
	}

	private void startFragment(BaseFragment fragment, int itemId) {
		navigation.setCheckedItem(itemId);
		startFragment(fragment);
	}

	private void startFragment(BaseFragment fragment) {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0)
			startFragment(fragment, false);
		else startFragment(fragment, true);
	}

	private void startFragment(BaseFragment fragment,
			boolean isAddedToBackStack) {
		FragmentTransaction trans =
				getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.fade_in,
								R.anim.fade_out, R.anim.fade_in,
								R.anim.fade_out)
						.replace(R.id.fragmentContainer, fragment,
								fragment.getUniqueTag());
		if (isAddedToBackStack) {
			trans.addToBackStack(fragment.getUniqueTag());
		}
		trans.commit();
	}

	private void clearBackStack() {
		getSupportFragmentManager().popBackStackImmediate(null,
				POP_BACK_STACK_INCLUSIVE);
	}

	@Override
	public void handleDbException(DbException e) {
		// Do nothing for now
	}

	private void setLockVisible(boolean visible) {
		MenuItem item = navigation.getMenu().findItem(R.id.nav_btn_lock);
		if (item != null) item.setVisible(visible);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContextAddedEvent) {
			LOG.info("CONTEXT ADDED: " +
					((ContextAddedEvent) e).getContext().getName());
			updateContexts(((ContextAddedEvent) e).getContext().getId());
		} else if (e instanceof ContextRemovedEvent) {
			updateContexts("All");
		}
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
	 * Implements the ContextLister interface contextChanged method, which called when context active value changed.
	 *
	 * @param active - a boolean value
	 */
	/*@Override
	public void contextChanged(boolean active) {
		if (active) {
			String activeContext =
					findActiveLocationContext(locationContexts).getName();
			LOG.info("Context changed " + activeContext);
			if (!egoNetwork.getCurrentContext()
					.equals(egoNetwork.getOrCreateContext(activeContext))) {
				egoNetwork.setCurrent(
						egoNetwork.getOrCreateContext(activeContext));
				String prompt = "Your context has Changed! " + activeContext +
						" is now active!";
				Toast.makeText(NavDrawerActivity.this,
						prompt,
						LENGTH_LONG)
						.show();
				startFragment(ChatListFragment.newInstance());
			}
		}
	}*/


	/**
	 * This method implements the SensorValueListener interface receiveValue method, which
	 * obtains values from the location sensor.
	 */
	/*@Override
	public void receiveValue(Object location) {
		// updates the current location
		currentLocation = (Location) location;
		LOG.info("Location Receive Value");
	}

	private LocationContext findActiveLocationContext(
			Collection<LocationContext> listOfLocationContexts) {
		return listOfLocationContexts.stream()
				.filter(lc -> lc.isActive())
				.findFirst().orElse(null);
	}

	private LocationContext findLocationContextByName(
			Collection<LocationContext> listOfLocationContexts, String name) {
		return listOfLocationContexts.stream()
				.filter(lc -> name.equals(lc.getName()))
				.findFirst().orElse(null);
	}*/
	@Override
	protected void onPause() {
		super.onPause();
		// Remove location updates
		//mLocationSensor.stopUpdates();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Remove location updates
		//mLocationSensor.stopUpdates();
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
					android.R.string.ok, new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// Request permission
							ActivityCompat
									.requestPermissions(NavDrawerActivity.this,
											new String[] {
													Manifest.permission.ACCESS_FINE_LOCATION},
											REQUEST_PERMISSIONS_REQUEST_CODE);
						}
					});
		} else {
			ActivityCompat.requestPermissions(NavDrawerActivity.this,
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
				// Permission denied.
				/*showSnackbar(R.string.permission_denied_explanation,
						R.string.settings, new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								// Build intent that displays the App settings screen.
								Intent intent = new Intent();
								intent.setAction(
										Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								Uri uri = Uri.fromParts("package",
										BuildConfig.APPLICATION_ID, null);
								intent.setData(uri);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						});*/
			}
		}
	}
}
