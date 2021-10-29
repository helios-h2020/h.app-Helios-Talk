package eu.h2020.helios_social.happ.helios.talk.navdrawer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import eu.h2020.helios_social.core.context.ContextListener;
import eu.h2020.helios_social.core.context.ext.LocationContext;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.sensor.ext.LocationSensor;
import eu.h2020.helios_social.core.sensor.ext.TimeSensor;
import eu.h2020.helios_social.happ.helios.talk.BuildConfig;
import eu.h2020.helios_social.happ.helios.talk.HeliosTalkService;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.activity.RequestCodes;
import eu.h2020.helios_social.happ.helios.talk.chat.ChatListFragment;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactListFragment;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.PendingContactListActivity;
import eu.h2020.helios_social.happ.helios.talk.context.ContextController;
import eu.h2020.helios_social.happ.helios.talk.context.CreateContextActivity;
import eu.h2020.helios_social.happ.helios.talk.context.invites.InvitationListActivity;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultHandler;
import eu.h2020.helios_social.happ.helios.talk.favourites.FavouritesFragment;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.happ.helios.talk.logout.SignOutFragment;
import eu.h2020.helios_social.happ.helios.talk.network.NetworkChangeReceiver;
import eu.h2020.helios_social.happ.helios.talk.profile.ProfileActivity;
import eu.h2020.helios_social.happ.helios.talk.settings.SettingsActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.CommunicationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.context.ContextType;
import eu.h2020.helios_social.modules.groupcommunications.api.context.DBContext;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.LocationContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.SpatioTemporalContext;
import eu.h2020.helios_social.modules.groupcommunications_utils.identity.IdentityManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContextAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContextRemovedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContextRenamedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupInvitationAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupMessageReceivedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.NetworkConnectionChangedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PendingContactAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PrivateMessageReceivedEvent;

import static android.widget.Toast.LENGTH_LONG;
import static androidx.core.view.GravityCompat.START;
import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static eu.h2020.helios_social.happ.helios.talk.navdrawer.IntentRouter.handleExternalIntent;
import static eu.h2020.helios_social.happ.helios.talk.network.Constants.CONNECTIVITY_ACTION;
import static eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager.LifecycleState.RUNNING;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Logger.getLogger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class NavDrawerActivity extends HeliosTalkActivity implements
        BaseFragment.BaseFragmentListener,
        NavigationView.OnNavigationItemSelectedListener, EventListener, ContextListener/*, SensorValueListener*/ {

    // Code used in requesting runtime permissions
    private static final int REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 35;
//    private static final int REQUEST_BACKGROUND_PERMISSIONS_REQUEST_CODE = 35;

    private static final int REQUEST_ACCESS_MEDIA_METADATA = 0;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String[] MEDIA_LOCATION_PERMISSION = {
            Manifest.permission.ACCESS_MEDIA_LOCATION
    };

    private static final String[] MEDIA_AND_LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Constant used in the location settings dialog
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Tracks the status of the location updates request
    private Boolean mRequestingLocationUpdates;
    // Access the location sensor
    private LocationSensor mLocationSensor;
    private TimeSensor timeSensor;

    private ArrayList<LocationContextProxy> locationContexts;
    private ArrayList<SpatioTemporalContext> spatioTemporalContexts;

    // Represents a geographical location
     private Location currentLocation;

    private static final Logger LOG =
            getLogger(NavDrawerActivity.class.getName());

    private ActionBarDrawerToggle drawerToggle;
    @Inject
    protected ContactManager contactManager;
    @Inject
    protected GroupManager groupManager;
    @Inject
    protected ContextController contextController;
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
    @Inject
    CommunicationManager communicationManager;
    @Inject
    ConversationManager conversationManager;

    private DrawerLayout drawerLayout;
    private NavigationView navigation;
    private SubMenu contextMenu;
    private SubMenu contextMenu2;

    private BottomNavigationView bottomNav;
    private ArrayList<DBContext> contexts;
    private LayoutInflater layoutInflater;
    private PopupWindow popupWindow;
    private ListView lst;
    private ArrayList<DBContext> contexts_list;
    private ArrayList<String> list_items;
    IntentFilter intentFilter;
    NetworkChangeReceiver receiver;
    private boolean isInitialNetworkChange=true;
    private DBContext newContextBackgroundActivation = null;
    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        exitIfStartupFailed(getIntent());
        setContentView(R.layout.activity_nav_drawer);

        mRequestingLocationUpdates = false;


        Toolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigation = findViewById(R.id.navigation);
        MenuItem menuNav = navigation.getMenu().findItem(R.id.contexts);
        navigation.setItemIconTintList(null);
        contextMenu = menuNav.getSubMenu();

        eventBus.addListener(this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver(eventBus, communicationManager);
        registerReceiver(receiver, intentFilter);
        LOG.info("receiverRegistered");
        receiver.setInitial(true);

        setSupportActionBar(toolbar);
        ActionBar actionBar = requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowPosition = displayMetrics.widthPixels/5;
        // when a user clicks on the toolbar title, a window that consists of the contexts appears
        toolbar.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                layoutInflater = (LayoutInflater) getApplication().getSystemService(LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") LinearLayout contextsLinearLayout  = (LinearLayout) layoutInflater.inflate(R.layout.context_list,null);
                // set window background color and shape
                contextsLinearLayout.setBackground(getResources().getDrawable(R.drawable.popup_shape,NavDrawerActivity.this.getTheme()));
                // set text color
                TextView tvTitle = contextsLinearLayout.findViewById(R.id.contexts);
                tvTitle.setTextColor(getResources().getColor(R.color.m_grey_500,NavDrawerActivity.this.getTheme()));
                lst = contextsLinearLayout.findViewById(R.id.listView1);

                // get context list
                contexts_list = new ArrayList<>();
                list_items = new ArrayList<>();
                try {
                    contexts_list = (ArrayList<DBContext>) contextManager.getContexts();
                    for (int i = 0; i < contexts_list.size(); i++) {
                        DBContext c = contexts_list.get(i);

                        String cname = c.getPrivateName();
                        if (cname.equals("All")) cname = "No Context";
                        list_items.add(cname);
                    }
                } catch (
                        DbException e) {
                    e.printStackTrace();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(NavDrawerActivity.this,R.layout.list_item_simple,list_items);
                lst.setAdapter(adapter);
                popupWindow = new PopupWindow(contextsLinearLayout,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT,true);

                // alter context when a click occurred
                lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        DBContext current = contexts_list.get(i);
                        egoNetwork.setCurrent(egoNetwork.getOrCreateContext(
                                current.getName() + "%" + current.getId()));

                        styleBasedOnContext(current.getId());
                        bottomNav.setSelectedItemId(R.id.nav_conversations);
                        //loadContexts(current.getId());
                        navigation.setCheckedItem(i);
                        // navigation.invalidate();
                        popupWindow.dismiss();
                    }
                });
                // set the position of popup window
                popupWindow.showAsDropDown(toolbar,Math.round(windowPosition),0);

                // warning is for visually impaired people
                contextsLinearLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        popupWindow.dismiss();
                        return false;
                    }
                });
            }
        });
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                                                 R.string.nav_drawer_open_description,
                                                 R.string.nav_drawer_close_description);
        drawerLayout.addDrawerListener(drawerToggle);

        navigation.setNavigationItemSelectedListener(this);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        lockManager.isLockable().observe(this, this::setLockVisible);

        if (lifecycleManager.getLifecycleState().isAfter(RUNNING)) {
            //showSignOutFragment();
        } else if (state == null) {
            startFragment(ChatListFragment.newInstance(),
                          0);
        }
        if (state == null) {
            // do not call this again when there's existing state
            onNewIntent(getIntent());
        }

        LOG.info("verifyStoragePermissions");

        // Init LocationSensor
        mLocationSensor = new LocationSensor(this);
        timeSensor = new TimeSensor(3000);
        if (signedIn()) {

            //timeSensor.registerValueListener(this);
            //mLocationSensor.registerValueListener(this);
            LOG.info("loadLocationContextsAndRegisterSensors");
            loadLocationContextsAndRegisterSensors();
            loadSpatiotemporalContextsAndRegisterSensors();
            timeSensor.startUpdates();
            if (checkPermissions()) {
                LOG.info("locationSensorStartedUpdates");
                mLocationSensor.startUpdates();

            } else {
                verifyStoragePermissions();
            }
        }
        String message = getIntent().getExtras() != null ? getIntent().getExtras().getString("message") : null;
        if (message != null) ToastMessage(message);

/*        if (signedIn()) {
            // CustomViewTarget target = new CustomViewTarget(R.id.contactIcon, this);
            new ShowcaseView.Builder(this)
                    .setTarget(new ActionViewTarget(this, ActionViewTarget.Type.HOME))
                    .setContentTitle("ShowcaseView")
                    .setContentText("This is highlighting the Home button")
                    .hideOnTouchOutside()
                    .build();
        }*/
    }


    public void updateConnection() {
        egoNetwork.save();
        WorkManager.getInstance(getApplication()).cancelAllWork();
        drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
        reconnect();
    }



    private void ToastMessage(String message) {
        Toast.makeText(
                this,
                message,
                LENGTH_LONG
        ).show();
    }

    public void loadContexts(String id) {
        LOG.info("loading contexts...");
        contextMenu.clear();
        LOG.info("menu cleared");
        contexts = new ArrayList<>();
        try {
            contexts = (ArrayList<DBContext>) contextManager.getContexts();
            LOG.info("Updating contexts menu: " + contexts.size());
            int activeItem = 0;
            for (int i = 0; i < contexts.size(); i++) {
                DBContext c = contexts.get(i);
                // display the private name of the context
                String cname = c.getPrivateName();
                if (c.getId().equals(id)) activeItem = i;
                if (cname.equals("All")) cname = "No Context";
                contextMenu.add(0, i,
                                Menu.CATEGORY_SECONDARY,
                                cname)
                        .setIcon(R.drawable.ic_context_3);

                if (c.getContextType() == ContextType.GENERAL){
                    contextMenu.findItem(i).setIcon(buildContextStateDrawable(true, R.drawable.ic_context_3));
                }
                else if (c.getContextType()== ContextType.LOCATION){
                    LocationContextProxy locationContextProxy =locationContexts.stream()
                            .filter(context -> c.getId().equals(context.getId()))
                            .findFirst().orElse(null);
                    if (locationContextProxy!=null){
                        LOG.info("found a location context"+locationContextProxy.getPrivateName());
                        contextMenu.findItem(i).setIcon(buildContextStateDrawable(locationContextProxy.isActive(), R.drawable.ic_context_3));
                    }
                }
                else if (c.getContextType()== ContextType.SPATIOTEMPORAL){
                    SpatioTemporalContext spatioTemporalContext =spatioTemporalContexts.stream()
                            .filter(context -> c.getId().equals(context.getId()))
                            .findFirst().orElse(null);
                    if (spatioTemporalContext!=null){
                        LOG.info("found a spatiotemporal context"+spatioTemporalContext.getPrivateName());
                        contextMenu.findItem(i).setIcon(buildContextStateDrawable(spatioTemporalContext.isActive(), R.drawable.ic_context_3));
                    }
                }


            }
            contextMenu.setGroupCheckable(0, true, true);

            navigation.setCheckedItem(activeItem);
        } catch (
                DbException e) {
            e.printStackTrace();
        }
        navigation.invalidate();
        setContextUnreadMessagesIndicator();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
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
                    if (selectedFragment==null){
                        selectedFragment = ChatListFragment.newInstance();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer,
                                     selectedFragment).commit();
                    return true;
                }
            };

    @Override
    public void onStart() {
        super.onStart();

//        eventBus.addListener(this);
//        isInitialNetworkChange=true;
//        LOG.info("receiverRegistered");
//        receiver.setInitial(true);
        LOG.info("onStartRunning");
        if (signedIn()) {
            if (newContextBackgroundActivation!=null){
                egoNetwork.setCurrent(egoNetwork.getOrCreateContext(
                        newContextBackgroundActivation.getName() + "%" + newContextBackgroundActivation.getId()));
            }
            String cid = egoNetwork.getCurrentContext().getData().toString().split("%")[1];


            if (locationContexts==null) {
                loadLocationContextsAndRegisterSensors();
            }
            if (spatioTemporalContexts==null) {
                loadSpatiotemporalContextsAndRegisterSensors();
            }


            loadContexts(cid);

        }
    }


    private Drawable buildCounterDrawable(int count, Drawable backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.counter_menu_item_layout, null);
        view.setBackground(backgroundImageId);
        // just a red cycle, without any number
        if (count == -1){
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("");
        } else if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);

            if (count > 9) textView.setText("9+");

            else {
                String countString = String.valueOf(count);
                textView.setText(countString);
            }
        }
// Converts 14 dip into its equivalent px
        float dip = 32f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        )+1;
        view.measure(
                View.MeasureSpec.makeMeasureSpec((int)px, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((int)px, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);

    }

    private Drawable buildContextStateDrawable(boolean isActive, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.context_state_layout, null);
        view.setBackgroundResource(backgroundImageId);
        ImageView iv = (ImageView) view.findViewById(R.id.stateBackground);
        if (isActive) iv.setBackgroundResource(R.drawable.contact_connected);
        else iv.setBackgroundResource(R.drawable.contact_disconnected);


        // Converts 14 dip into its equivalent px
        float dip = 32f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        )+1;
        view.measure(
                View.MeasureSpec.makeMeasureSpec((int)px, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((int)px, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);

    }

    @Override
    public void onResume() {
        super.onResume();
        LOG.info("isInitial" + isInitialNetworkChange);
        LOG.info("needRestart" + communicationManager.getNeedRestart());
        if (communicationManager.getNeedRestart()){
                updateConnection();
        }
        if (signedIn()) {
            setConnectAndConnectionNotificationsIndicator();
            setContextUnreadMessagesIndicator();
        }

    }

    private void setConnectAndConnectionNotificationsIndicator() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        Menu menu = navigationView.getMenu();
        MenuItem friendRequests = menu.findItem(R.id.nav_btn_connect);
        int incomingConnectionRequests = 0;
        try {
            incomingConnectionRequests = contactManager.pendingIncomingConnectionRequests();
        } catch (DbException e) {
            e.printStackTrace();
        }
        int inviteCounter = 0;
        try {
            inviteCounter += contextController.getPendingIncomingContextInvitations();
        } catch (DbException e) {
            e.printStackTrace();
        }
        try {
            inviteCounter += groupManager.pendingIncomingGroupInvitations();
        } catch (DbException e) {
            e.printStackTrace();
        }
        try {
            inviteCounter += groupManager.pendingIncomingGroupAccessRequest();
        } catch (DbException e) {
            e.printStackTrace();
        }

        friendRequests.setIcon(buildCounterDrawable(incomingConnectionRequests,
                getDrawable(R.drawable.ic_connect_with_peers_nav_bar)));
        MenuItem invites = menu.findItem(R.id.nav_btn_invitations);
        invites.setIcon(buildCounterDrawable(inviteCounter, getDrawable(R.drawable.ic_notifications)));
    }


    private void setContextUnreadMessagesIndicator() {
        try {
            contexts = (ArrayList<DBContext>) contextManager.getContexts();
            for (int i = 0; i < contexts.size(); i++) {
                DBContext c = contexts.get(i);
                int unreadMessagesInContext = 0;
                unreadMessagesInContext = contextManager.countUnreadMessagesInContext(c.getId());
                if (contextMenu.findItem(i)!=null) {
                    MenuItem contextMenuItem = contextMenu.getItem(i);
                    contextMenuItem.setIcon(buildCounterDrawable(unreadMessagesInContext, contextMenuItem.getIcon()));
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
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

        if (request == REQUEST_CHECK_SETTINGS) {
            switch (request) {
                case Activity.RESULT_OK:
                    break;
                case Activity.RESULT_CANCELED:
                    mRequestingLocationUpdates = false;
                    break;
            }
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
        /*} else if (R.id.nav_btn_stats == fragmentId) {
            //startActivity(new Intent(this, StatsActivity.class));*/
        }
        else if (R.id.nav_btn_connect == fragmentId){
                startActivity(new Intent(this, PendingContactListActivity.class));
        }
        else if (R.id.nav_btn_invitations == fragmentId){
            startActivity(new Intent(this, InvitationListActivity.class));

        }
        else if (R.id.nav_btn_create_context == fragmentId) {
            startActivity(new Intent(this, CreateContextActivity.class));
        } else if (R.id.nav_btn_signout == fragmentId) {
            signOut();
        } else {
            int index = contextMenu.findItem(fragmentId).getItemId();
//            MenuItem contextMenuItem = contextMenu.getItem(index);
//            contextMenuItem.setIcon(buildCounterDrawable(0, R.drawable.ic_context_3));
            DBContext current = contexts.get(index);
            egoNetwork.setCurrent(egoNetwork.getOrCreateContext(
                    current.getName() + "%" + current.getId()));

            styleBasedOnContext(current.getId());
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
        WorkManager.getInstance(getApplication()).cancelAllWork();
        drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
        signOut(false, false);
        finish();
    }

    private void startFragment(BaseFragment fragment, int itemId) {
        navigation.setCheckedItem(itemId);
        startFragment(fragment);
    }

    private void startFragment(BaseFragment fragment) {
        startFragment(fragment, getSupportFragmentManager().getBackStackEntryCount() != 0);
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
        LOG.info("event_occured");
        if (e instanceof ContextAddedEvent) {
            LOG.info("CONTEXT ADDED: " +
                             requireNonNull(((ContextAddedEvent) e).getContext()).getName());

            updateSensorsOnAddedContext(requireNonNull(((ContextAddedEvent) e).getContext()));
            loadContexts(requireNonNull(((ContextAddedEvent) e).getContext()).getId());
        } else if (e instanceof ContextRemovedEvent) {
            updateSensorsOnRemovedContext(((ContextRemovedEvent) e).getContextId());
            loadContexts("All");
        } else if (e instanceof ContextRenamedEvent){
            String cid = egoNetwork.getCurrentContext().getData().toString().split("%")[1];
            LOG.info("contextRenamed" + cid);
            loadContexts(cid);
        } else if (e instanceof PrivateMessageReceivedEvent) {
            setContextUnreadMessagesIndicator();
        } else if (e instanceof GroupMessageReceivedEvent) {
            setContextUnreadMessagesIndicator();
        } else if (e instanceof PendingContactAddedEvent ){
            setConnectAndConnectionNotificationsIndicator();
        } else if (e instanceof GroupInvitationAddedEvent ){
            setConnectAndConnectionNotificationsIndicator();
        } else if (e instanceof NetworkConnectionChangedEvent){
            LOG.info("connection changed event");
            if (!isInitialNetworkChange){
                NetworkConnectionChangedEvent nc =
                        (NetworkConnectionChangedEvent) e;
                if (nc.isConnected()) {
                    updateConnection();
                    //Toast.makeText(this,"Reconnecting to TALK...",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
                }
            } else {
                isInitialNetworkChange = false;
            }

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
     * Implements the ContextLister interface contextChanged method, which called when context
     * active value changed.
     *
     * @param active - a boolean value
     */
	@Override
	public void contextChanged(boolean active) {
	    LOG.info("get context that changed state");
        DBContext dbContext = getChangedContext();
        LOG.info("contextStateChanged");
        updateContextStateIndicator(dbContext);
        if (active){
            int positionOnMenu = getMenuItemPositionFromContextId(dbContext.getId());
            LOG.info("got position on menu");
            changeSelectedContext(positionOnMenu);
            LOG.info("updated selected context");
        } else {
            LOG.info("updated state indicators");
            DBContext activatedContext = getLatestActivatedContext();
            LOG.info("got latest activated context");
            int positionOnMenu = getMenuItemPositionFromContextId(activatedContext.getId());
            LOG.info("got position on menu");
            changeSelectedContext(positionOnMenu);
            LOG.info("updated selected context");
        }
	}

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onStop() {
        super.onStop();
        LOG.info("onStopRunning");
//        eventBus.removeListener(this);
//        isInitialNetworkChange=true;
//        LOG.info("receiverUnregistered");
//        unregisterReceiver(receiver);
        // Remove location updates
        //mLocationSensor.stopUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventBus.removeListener(this);
        LOG.info("receiverUnregistered");
        unregisterReceiver(receiver);
        // Remove location updates
        if (mLocationSensor!=null) mLocationSensor.stopUpdates();
        if (timeSensor!=null) timeSensor.stopUpdates();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ContextCompat.checkSelfPermission(this,
                                                                 Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionState2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_MEDIA_LOCATION);
        return (permissionState == PackageManager.PERMISSION_GRANTED) && (permissionState2 == PackageManager.PERMISSION_GRANTED);
    }



    public void verifyStoragePermissions() {
        // Check if we have write permission
        if (ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                                                  Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            LOG.info("readWriteAndLocationPermissionsGranted");
            mLocationSensor.startUpdates();
            verifyMetadataPermissions();
            return;
        }

        //check if access to metadata has been granted.
        boolean p0 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean p1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean p2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (p1 && p2 && p0) {
            LOG.info("PermissionsGranted");
            verifyMetadataPermissions();
            return;
        } else if (p2){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Access Storage Permission!")
                        .setMessage(R.string.profiling_storage_permissions)
                        .setPositiveButton("ok", (dialogInterface, i) -> {
                            ActivityCompat.requestPermissions(
                                    NavDrawerActivity.this,
                                    PERMISSIONS_STORAGE,
                                    REQUEST_EXTERNAL_STORAGE);
                        })
                        .setNegativeButton("cancel", (dialog, i) -> dialog.dismiss()).create().show();

            } else {
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE);
            }
        } else if (p1 && p0){
            LOG.info("READ_WRITEGranted");
            ActivityCompat.requestPermissions(NavDrawerActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(NavDrawerActivity.this,
                    MEDIA_AND_LOCATION_PERMISSIONS,
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }

    }

    public void verifyMetadataPermissions() {
        //check if access to metadata has been granted.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Access to Media Metadata!")
                    .setMessage(R.string.profiling_metadata_permissions)
                    .setPositiveButton("ok", (dialogInterface, i) -> ActivityCompat.requestPermissions(
                            NavDrawerActivity.this,
                            MEDIA_LOCATION_PERMISSION,
                            REQUEST_ACCESS_MEDIA_METADATA))
                    .setNegativeButton("cancel", (dialog, i) -> dialog.dismiss()).create().show();

        } else {
            LOG.info("requestMediaPermissions");
            ActivityCompat.requestPermissions(
                    this,
                    MEDIA_LOCATION_PERMISSION,
                    REQUEST_ACCESS_MEDIA_METADATA);
        }

    }

    /**
     * Request permissions to access location
     */
/*    private void requestPermissions() {
            ActivityCompat.requestPermissions(NavDrawerActivity.this,
                                              new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE);
    }*/


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LOG.info("onRequestPermissionResult");
        if (requestCode == REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE || requestCode == REQUEST_PERMISSIONS_REQUEST_CODE){
            if (grantResults.length <= 0) {
                LOG.info("User interaction was cancelled.");
            }
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if (mRequestingLocationUpdates) {
                    LOG.info(
                            "Permission granted, updates requested, starting location updates");

                    mLocationSensor.startUpdates();

                //}
            }
//            else {
//                new AlertDialog.Builder(this)
//                        .setTitle("Access Location Permission!")
//                        .setMessage(R.string.location_permission_prompt)
//                        .setCancelable(false)
//                        .setPositiveButton("ok", (dialogInterface, i) -> {
//                            // Build intent that displays the App settings screen.
//                            Intent intent = new Intent();
//                            intent.setAction(
//                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package",
//                                    BuildConfig.APPLICATION_ID, null);
//                            intent.setData(uri);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                        }).create().show();
//            }
        }
    }

    public void updateSensorsOnRemovedContext(String contextId){
        LocationContextProxy removedContext =locationContexts.stream()
                .filter(context -> contextId.equals(context.getId()))
                .findFirst().orElse(null);
        if (removedContext!=null){
            removedContext.unregisterContextListener(this);
            mLocationSensor.unregisterValueListener(removedContext);
            LOG.info("locationContextsBeforeRemove"+locationContexts.toString());
            locationContexts.remove(removedContext);
            LOG.info("locationContextsAfterRemove"+locationContexts.toString());
            return;
        }
        SpatioTemporalContext removedSpatioTemporalContext =spatioTemporalContexts.stream()
                .filter(context -> contextId.equals(context.getId()))
                .findFirst().orElse(null);
        if (removedSpatioTemporalContext!=null){
            removedSpatioTemporalContext.unregisterContextListener(this);
            mLocationSensor.unregisterValueListener(removedSpatioTemporalContext.getContextA());
            timeSensor.unregisterValueListener(removedSpatioTemporalContext.getContextB());
            LOG.info("spatioTemporalContextsBeforeRemove"+spatioTemporalContexts.toString());
            spatioTemporalContexts.remove(removedSpatioTemporalContext);
            LOG.info("spatioTemporalContextsAfterRemove"+spatioTemporalContexts.toString());
            return;
        }
    }


    public void updateSensorsOnAddedContext(DBContext context){
        LOG.info("updateSensorsOnAddedContext");
        if (context.contextType.equals(ContextType.LOCATION)) {
            try {
                LocationContextProxy locationContextProxy = (LocationContextProxy) contextManager.getContext(context.getId());
                locationContexts.add(locationContextProxy);
                locationContextProxy.registerContextListener(this);
                mLocationSensor.registerValueListener(locationContextProxy);
                LOG.info("registerValueListener");

            } catch (DbException | FormatException dbException) {
                dbException.printStackTrace();
            }
        }
        else if(context.contextType.equals(ContextType.SPATIOTEMPORAL)){
            try {
                SpatioTemporalContext spatioTemporalContext = (SpatioTemporalContext) contextManager.getContext(context.getId());
                spatioTemporalContexts.add(spatioTemporalContext);
                spatioTemporalContext.registerContextListener(this);
                mLocationSensor.registerValueListener((LocationContext) spatioTemporalContext.getContextA());
                timeSensor.registerValueListener(spatioTemporalContext.getContextB());
            } catch (DbException | FormatException dbException) {
                dbException.printStackTrace();
            }
        }
        LOG.info("checkPermissions");
        if (checkPermissions()) {
            LOG.info("locationSensorStartedUpdates");
            mLocationSensor.startUpdates();

        } else {
            LOG.info("HasNotPermissions");
            verifyStoragePermissions();
        }
    }

    private void loadLocationContextsAndRegisterSensors(){
        // Register location listeners for the contexts;
        try {
            locationContexts = (ArrayList<LocationContextProxy>) contextManager.getLocationContexts();
            for (LocationContextProxy locationContextProxy : locationContexts) {
                locationContextProxy.registerContextListener(this);
                mLocationSensor.registerValueListener(locationContextProxy);
                LOG.info("locationSensorRegistered: lat: " + locationContextProxy.getLat() + ",lon: " + locationContextProxy.getLon() );
            }
        } catch (DbException | FormatException e) {
            e.printStackTrace();
        }
    }

    private void loadSpatiotemporalContextsAndRegisterSensors(){
        // Register spatiotemporal listeners for the contexts;
        try {
            spatioTemporalContexts = (ArrayList<SpatioTemporalContext>) contextManager.getSpatiotemporalContexts();
            for (SpatioTemporalContext spatioTemporalContext : spatioTemporalContexts) {
                spatioTemporalContext.registerContextListener(this);
                mLocationSensor.registerValueListener((LocationContext) spatioTemporalContext.getContextA());
                timeSensor.registerValueListener(spatioTemporalContext.getContextB());
                LOG.info("SpatioTemporalSensorRegistered: lat: " + spatioTemporalContext.getLat() + ",lon: " + spatioTemporalContext.getLon() +
                        ", startTime: " + spatioTemporalContext.getStartTime() + ", endTime: " + spatioTemporalContext.getEndTime());
            }
        } catch (DbException | FormatException e) {
            e.printStackTrace();
        }
    }

    private DBContext getLatestActivatedContext() {
        long maxTimestamp = 0;
        int latestActivatedContextPosition = 0;
        for (int i = 0; i < contexts.size(); i++) {
            DBContext c = contexts.get(i);
            if (c.getContextType() == ContextType.LOCATION) {
                LocationContextProxy locationContextProxy = locationContexts.stream()
                        .filter(context -> c.getId().equals(context.getId()))
                        .findFirst().orElse(null);
                LOG.info("context: "+ locationContextProxy.getPrivateName() +", timestamp: " + locationContextProxy.getContextStateChangeTimestamp() + ", isActive: " + locationContextProxy.isActive());
                if (locationContextProxy != null) {
                    if (locationContextProxy.getContextStateChangeTimestamp() > maxTimestamp && locationContextProxy.isActive()) {
                        latestActivatedContextPosition = i;
                        maxTimestamp = locationContextProxy.getContextStateChangeTimestamp();
                    }
                }
            } else if (c.getContextType() == ContextType.SPATIOTEMPORAL) {
                SpatioTemporalContext spatioTemporalContext = spatioTemporalContexts.stream()
                        .filter(context -> c.getId().equals(context.getId()))
                        .findFirst().orElse(null);
                LOG.info("context: "+ spatioTemporalContext.getPrivateName() +", timestamp: " + spatioTemporalContext.getContextStateChangeTimestamp() + ", isActive: " + spatioTemporalContext.isActive());
                if (spatioTemporalContext != null) {
                    if (spatioTemporalContext.getContextStateChangeTimestamp() > maxTimestamp && spatioTemporalContext.isActive()) {
                        latestActivatedContextPosition = i;
                        maxTimestamp = spatioTemporalContext.getContextStateChangeTimestamp();
                    }
                }
            }
        }
        return contexts.get(latestActivatedContextPosition);
    }

    private DBContext getChangedContext() {
        long maxTimestamp = 0;
        int latestChangedContextPosition = 0;
        for (int i = 0; i < contexts.size(); i++) {
            DBContext c = contexts.get(i);
            if (c.getContextType() == ContextType.LOCATION) {
                LocationContextProxy locationContextProxy = locationContexts.stream()
                        .filter(context -> c.getId().equals(context.getId()))
                        .findFirst().orElse(null);
                if (locationContextProxy != null) {
                    if (locationContextProxy.getContextStateChangeTimestamp() > maxTimestamp) {
                        latestChangedContextPosition = i;
                        maxTimestamp = locationContextProxy.getContextStateChangeTimestamp();
                    }
                }
            } else if (c.getContextType() == ContextType.SPATIOTEMPORAL) {
                SpatioTemporalContext spatioTemporalContext = spatioTemporalContexts.stream()
                        .filter(context -> c.getId().equals(context.getId()))
                        .findFirst().orElse(null);
                if (spatioTemporalContext != null) {
                    if (spatioTemporalContext.getContextStateChangeTimestamp() > maxTimestamp) {
                        latestChangedContextPosition = i;
                        maxTimestamp = spatioTemporalContext.getContextStateChangeTimestamp();
                    }
                }
            }
        }
        return contexts.get(latestChangedContextPosition);
    }

    private int getMenuItemPositionFromContextId(String contextId){
        int pos = 0;
        for (int i = 0; i < contexts.size(); i++) {
            DBContext c = contexts.get(i);
            if (c.getId().equals(contextId)) pos = i;
        }
        return pos;
    }

    private void changeSelectedContext(int pos){

        DBContext current = contexts.get(pos);

        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)){
            LOG.info("Lifecycle.State.STARTED");
            runOnUiThreadUnlessDestroyed(() -> {
                egoNetwork.setCurrent(egoNetwork.getOrCreateContext(
                        current.getName() + "%" + current.getId()));
                styleBasedOnContext(current.getId());
                bottomNav.setSelectedItemId(R.id.nav_conversations);
                navigation.setCheckedItem(pos);
            });


        }
        else{
            newContextBackgroundActivation = current;
        }

    }

    private void setContextStateIndicator(DBContext c){
        int pos = getMenuItemPositionFromContextId(c.getId());
        if (c.getContextType()== ContextType.LOCATION){
            LocationContextProxy locationContextProxy =locationContexts.stream()
                    .filter(context -> c.getId().equals(context.getId()))
                    .findFirst().orElse(null);
            if (locationContextProxy!=null){
                LOG.info("found a location context"+locationContextProxy.getPrivateName());
                contextMenu.findItem(pos).setIcon(buildContextStateDrawable(locationContextProxy.isActive(), R.drawable.ic_context_3));
            }
        }
        else if (c.getContextType()== ContextType.SPATIOTEMPORAL){
            SpatioTemporalContext spatioTemporalContext =spatioTemporalContexts.stream()
                    .filter(context ->c.getId().equals(context.getId()))
                    .findFirst().orElse(null);
            if (spatioTemporalContext!=null){
                LOG.info("found a spatiotemporal context"+spatioTemporalContext.getPrivateName());
                contextMenu.findItem(pos).setIcon(buildContextStateDrawable(spatioTemporalContext.isActive(), R.drawable.ic_context_3));
            }
        }
    }

    private void updateContextStateIndicator(DBContext context) {
        LOG.info("updateContextStateIndicator");
        LOG.info("lifecycle: " + getLifecycle().getCurrentState());
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)){
            LOG.info("isresumed");
            runOnUiThreadUnlessDestroyed(() -> {
                LOG.info("setContextStateIndicator");
                setContextStateIndicator(context);
            });
        }
    }

    // ViewPager Adapter class
    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }
        @Override
        public Fragment getItem(int i) {
            return mList.get(i);
        }
        @Override
        public int getCount() {
            return mList.size();
        }
        public void addFragment(Fragment fragment) {
            mList.add(fragment);
        }
    }

}
