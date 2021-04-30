package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.app.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PendingContactAddedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.PendingContactRemovedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.connection.ConnectionManager;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

@NotNullByDefault
public class PendingContactListViewModel extends AndroidViewModel
		implements EventListener {

	private final Logger LOG =
			getLogger(PendingContactListViewModel.class.getName());

	@DatabaseExecutor
	private final Executor dbExecutor;
	private final ContactManager contactManager;
	private final ConnectionManager connectionManager;
	private final EventBus eventBus;

	private final MutableLiveData<Collection<PendingContactItem>>
			pendingContacts = new MutableLiveData<>();

	@Inject
	PendingContactListViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			ContactManager contactManager,
			ConnectionManager connectionManager,
			EventBus eventBus) {
		super(application);
		this.dbExecutor = dbExecutor;
		this.contactManager = contactManager;
		this.connectionManager = connectionManager;
		this.eventBus = eventBus;
		this.eventBus.addListener(this);
	}

	void onCreate() {
		if (pendingContacts.getValue() == null) loadPendingContacts();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof PendingContactRemovedEvent ||
				e instanceof PendingContactAddedEvent) {
			loadPendingContacts();
		}
	}

	private void loadPendingContacts() {
		dbExecutor.execute(() -> {
			try {
				Collection<PendingContact> pContacts =
						contactManager.getPendingContacts();
				List<PendingContactItem> items =
						new ArrayList<>(pContacts.size());
				for (PendingContact p : pContacts) {
					items.add(new PendingContactItem(p));
				}
				pendingContacts.postValue(items);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	LiveData<Collection<PendingContactItem>> getPendingContacts() {
		return pendingContacts;
	}

	void removePendingContact(PendingContact pendingContact) {
		dbExecutor.execute(() -> {
			connectionManager.rejectConnectionRequest(pendingContact);
		});
	}

	void addPendingContact(PendingContact pendingContact) {
		dbExecutor.execute(() -> {
			connectionManager.acceptConnectionRequest(pendingContact);
		});
	}

}
