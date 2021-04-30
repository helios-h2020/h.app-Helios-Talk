package eu.h2020.helios_social.happ.helios.talk.favourites;

import android.annotation.SuppressLint;
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
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentRetriever;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications_utils.Pair;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PendingContactAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ContextInvitationAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.GroupInvitationAddedEvent;
import eu.h2020.helios_social.happ.helios.talk.fragment.HeliosContextFragment;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactType;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Favourite;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static java.util.Collections.emptyList;
import static java.util.logging.Level.WARNING;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class FavouritesFragment extends HeliosContextFragment implements EventListener {

    public static final String TAG = FavouritesFragment.class.getName();
    private static final Logger LOG = Logger.getLogger(TAG);

    //private ChatListAdapter adapter;
    private HeliosTalkRecyclerView list;
    private FavListAdapter adapter;
    private FavouritesViewModel viewModel;
    private AttachmentRetriever attachmentRetriever;

    @Inject
    volatile ConversationManager conversationManager;
    @Inject
    volatile ContextualEgoNetwork egoNetwork;
    @Inject
    ViewModelProvider.Factory viewModelFactory;


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

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(FavouritesViewModel.class);
        attachmentRetriever = viewModel.getAttachmentRetriever();

        View contentView = inflater.inflate(R.layout.list,
                container, false);

        adapter = new FavListAdapter(requireContext());
        list = contentView.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        list.setEmptyImage(R.drawable.ic_no_favourites_illustration);
        list.setEmptyTitle(R.string.no_favourites);
        list.setEmptyText(getString(R.string.no_favourites_details));

        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFavourites();
        list.startPeriodicUpdate();
    }

    @SuppressLint("RestrictedApi")
    public void onResume() {
        super.onResume();
        actionBar.invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        super.onStop();
        list.showProgressBar();
        list.stopPeriodicUpdate();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void eventOccurred(Event e) {
        if (e instanceof ContextInvitationAddedEvent) {
            if (((ContextInvitationAddedEvent) e).getInvite().isIncoming())
                actionBar.invalidateOptionsMenu();
        } else if (e instanceof GroupInvitationAddedEvent) {
            if (((GroupInvitationAddedEvent) e).getInvite().isIncoming())
                actionBar.invalidateOptionsMenu();
        } else if (e instanceof PendingContactAddedEvent) {
            if (((PendingContactAddedEvent) e).getPendingContact().getPendingContactType().equals(PendingContactType.INCOMING))
                actionBar.invalidateOptionsMenu();
        }
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
                            new FavItem(R.layout.list_item_fav, fav.getAuthor(), fav.getTextMessage(), fav.getMessageId(),
                                    fav.getTimestamp());
                    if (fav.getType() == Message.Type.IMAGES) {
                        item.setAttachmentList(getAttachments(fav.getMessageId()));
                    }
                    System.out.println("FavItem: " + fav);
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

    private void displayMessageAttachments(String messageId, List<AttachmentItem> items) {
        runOnUiThreadUnlessDestroyed(() -> {
            Pair<Integer, FavItem> pair =
                    adapter.getMessageItem(messageId);
            if (pair != null) {
                ((FavItem) pair.getSecond())
                        .setAttachmentList(items);
                adapter.notifyItemChanged(pair.getFirst());
            }
        });
    }

    private void loadMessageAttachments(String messageId) {
        listener.runOnDbThread(() -> {
            try {
                attachmentRetriever.getMessageAttachments(messageId);
                displayMessageAttachments(messageId, attachmentRetriever.cacheGet(messageId));
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    public List<AttachmentItem> getAttachments(String messageId) {
        List<AttachmentItem> attachments =
                attachmentRetriever.cacheGet(messageId);
        if (attachments == null) {
            loadMessageAttachments(messageId);
            return emptyList();
        }
        return attachments;
    }


}
