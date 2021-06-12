package eu.h2020.helios_social.happ.helios.talk.search;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.widget.SearchView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.conversation.ForumConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.CreateForumActivity;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.LocationForum;
import eu.h2020.helios_social.modules.groupcommunications.api.resourcediscovery.queries.Queryable;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.QueryResultsReceivedEvent;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Logger.getLogger;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SearchActivity extends HeliosTalkActivity implements EventListener, SearchView.OnQueryTextListener, ResultsActionListener {
    private static final Logger LOG =
            getLogger(SearchActivity.class.getName());

    // Code used in requesting runtime permissions
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static int RADIUS_THRESHOLD = 100000;

    private ArrayList<String> searchOptions;
    private ArrayList<ResultItem> results;

    private ListView optionsListView;
    private ArrayAdapter adapter;
    private SearchView searchView;
    private ResultListAdapter resultListAdapter;
    private ProgressBar progress;
    private HeliosTalkRecyclerView resultsList;
    private Toolbar toolbar;
    private String queryID;
    private boolean isLocationQueryInProgress;
    private FusedLocationProviderClient fusedLocationClient;


    @Inject
    SearchController searchController;
    @Inject
    volatile EventBus eventBus;

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_search);

        toolbar = requireNonNull(setUpCustomToolbar(true));

        searchOptions = new ArrayList();

        results = new ArrayList();
        searchOptions.add("Find forums nearby");
        searchOptions.add("Find forums by name");
        searchOptions.add("Create new forum");
        optionsListView = findViewById(R.id.searchOptions);
        adapter = new ArrayAdapter<String>(this, R.layout.list_search_option_item, R.id.option, searchOptions);
        optionsListView.setAdapter(adapter);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        optionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (searchOptions.get(position).equals("Create new forum")) {
                    isLocationQueryInProgress = false;
                    Intent intent = new Intent(SearchActivity.this, CreateForumActivity.class);
                    startActivity(intent);
                } else if (searchOptions.get(position).equals("Find forums nearby")) {
                    searchForumsByLastKnownLocation();

                } else {
                    LOG.info("Submitting Text Query: " + searchView.getQuery().toString());
                    onQueryTextSubmit(searchView.getQuery().toString());
                }

            }
        });

        progress = findViewById(R.id.progress_bar);
        progress.setVisibility(View.INVISIBLE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        resultListAdapter = new ResultListAdapter(this, this);

        resultsList = findViewById(R.id.results_list);
        resultsList.setLayoutManager(new LinearLayoutManager(this));
        resultsList.setAdapter(resultListAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventBus.removeListener(this);
        resultListAdapter.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    public void searchForumsByLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LOG.info("Permissions are missing");
            requestPermissions();
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LOG.info("Submitting Location Query: " + location.getLatitude() + ", " + location.getLongitude());
                                sumbitLocationQuery(location.getLatitude(), location.getLongitude());
                            }
                        }
                    });
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                    Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            showSnackbar(R.string.location_permission_search_prompt,
                         android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat
                                    .requestPermissions(
                                            SearchActivity.this,
                                            new String[]{
                                                    Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                                              new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                              REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private boolean checkPermissions() {
        LOG.info("check location permissions");
        int permissionState = ActivityCompat.checkSelfPermission(this,
                                                                 Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
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

    @Override
    public void eventOccurred(Event e) {
        if (e instanceof QueryResultsReceivedEvent) {
            QueryResultsReceivedEvent queryResultsReceivedEvent = (QueryResultsReceivedEvent) e;
            LOG.info("UPDATES ON QUERY RESPONSE RECEIVED");
            LOG.info(queryResultsReceivedEvent.getQueryResponse().getQueryId());
            LOG.info(queryID);
            if (queryResultsReceivedEvent.getQueryResponse().getQueryId().equals(queryID)) {
                results.clear();
                LOG.info("RESULTS SIZE? " + results.size());
                List<String> localForums = searchController.getLocalForums();
                Map<String, Double> scores = queryResultsReceivedEvent.getQueryResponse().getScores();
                for (Queryable q : queryResultsReceivedEvent.getQueryResponse().getEntities().values()) {
                    if (q instanceof LocationForum) {
                        LocationForum forum = (LocationForum) q;
                        results.add(new LocationForumResultItem(
                                forum,
                                scores.get(q.getId()),
                                localForums.contains(forum.getId()),
                                isLocationQueryInProgress)
                        );
                    } else {
                        Forum forum = (Forum) q;
                        results.add(new ForumResultItem(forum, scores.get(q.getId()), localForums.contains(forum.getId())));
                    }
                }
                resultListAdapter.replaceAll(results);
                resultListAdapter.notifyDataSetChanged();
                resultsList.showData();
            }
        }
    }

    private void sumbitLocationQuery(double lat, double lng) {
        isLocationQueryInProgress = true;
        if (results.size() > 0) {
            results.clear();
            resultListAdapter.clear();
            resultListAdapter.notifyDataSetChanged();
            resultsList.showData();
            resultsList.showProgressBar();

        }
        queryID = searchController.sendLocationQuery(lat, lng, RADIUS_THRESHOLD);

        progress.setVisibility(View.VISIBLE);
        optionsListView.setVisibility(View.GONE);

        progress.postOnAnimationDelayed(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.INVISIBLE);
            }
        }, 30000);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (results.size() > 0) {
            results.clear();
            resultListAdapter.clear();
            resultListAdapter.notifyDataSetChanged();
            resultsList.showData();
            resultsList.showProgressBar();
        }
        isLocationQueryInProgress = false;
        queryID = searchController.sendTextQuery(query);

        progress.setVisibility(View.VISIBLE);
        optionsListView.setVisibility(View.GONE);
        progress.postOnAnimationDelayed(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.INVISIBLE);
            }
        }, 30000);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (optionsListView.getVisibility() == View.GONE) {
            progress.setVisibility(View.INVISIBLE);
            optionsListView.setVisibility(View.VISIBLE);
            results.clear();
        }
        if (newText.length() > 0 && adapter.getCount() == 3) {
            searchOptions.clear();
            searchOptions.add("Find forums by name");
            adapter.notifyDataSetInvalidated();
        } else if (newText.length() == 0 && searchOptions.size() != 3) {
            searchOptions.clear();
            searchOptions.add("Find forums nearby");
            searchOptions.add("Find forums by name");
            searchOptions.add("Create new forum");
            adapter.notifyDataSetInvalidated();
        }
        return true;
    }

    @Override
    public void onJoinPublicForum(Forum forum) {
        searchController.joinPublicForum(forum);
        Intent i = new Intent(this, ForumConversationActivity.class);
        i.putExtra(GROUP_ID, forum.getId());
        startActivity(i);
        finish();
    }

    @Override
    public void onOpenPublicForum(Forum forum) {
        Intent i = new Intent(this, ForumConversationActivity.class);
        i.putExtra(GROUP_ID, forum.getId());
        startActivity(i);
        finish();
    }
}
