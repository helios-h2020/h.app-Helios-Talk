package eu.h2020.helios_social.happ.helios.talk.favourites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.fragment.HeliosContextFragment;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Favourite;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;

import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logDuration;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logException;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.now;
import static java.util.logging.Level.WARNING;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class FavouritesFragment extends HeliosContextFragment {

	public static final String TAG = FavouritesFragment.class.getName();
	private static final Logger LOG = Logger.getLogger(TAG);

	//private ChatListAdapter adapter;
	private HeliosTalkRecyclerView list;
	private FavListAdapter adapter;

	@Inject
	volatile ConversationManager conversationManager;
	@Inject
	volatile ContextualEgoNetwork egoNetwork;


	public static FavouritesFragment newInstance() {
		Bundle args = new Bundle();
		FavouritesFragment fragment = new FavouritesFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View contentView = inflater.inflate(R.layout.list,
				container, false);

		adapter = new FavListAdapter(requireContext());
		list = contentView.findViewById(R.id.list);
		list.setLayoutManager(new LinearLayoutManager(requireContext()));
		list.setAdapter(adapter);
		list.setEmptyImage(R.drawable.ic_star_disable);
		list.setEmptyText(getString(R.string.no_favourites));

		return contentView;
	}

	@Override
	public void onStart() {
		super.onStart();
		loadFavourites();
		list.startPeriodicUpdate();
	}

	@Override
	public void onStop() {
		super.onStop();
		list.showProgressBar();
		list.stopPeriodicUpdate();
	}

	private void loadFavourites() {
		int revision = adapter.getRevision();
		listener.runOnDbThread(() -> {
			try {
				long start = now();
				List<FavItem> favs = new ArrayList<>();

				String context =
						egoNetwork.getCurrentContext().getData().toString()
								.split("%")[1];
				Collection<Favourite> favourites;
				favourites = conversationManager.getFavourites(context);
				for (Favourite fav : favourites) {
					FavItem item =
							new FavItem(fav.getMessageId(), fav.getAuthor(),
									fav.getTimestamp(), fav.getTextMessage());
					favs.add(item);
				}
				logDuration(LOG, "Favourites Full load", start);
				displayFavourites(revision, favs);
			} catch (DbException | FormatException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	private void displayFavourites(int revision, List<FavItem> favs) {
		runOnUiThreadUnlessDestroyed(() -> {
			if (revision == adapter.getRevision()) {
				adapter.incrementRevision();
				if (favs.isEmpty()) list.showData();
				else adapter.replaceAll(favs);
			} else {
				loadFavourites();
			}
		});
	}

}
