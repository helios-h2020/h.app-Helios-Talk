package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.app.Application;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import eu.h2020.helios_social.happ.helios.talk.api.db.DatabaseExecutor;
import eu.h2020.helios_social.happ.helios.talk.api.identity.IdentityManager;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.LiveEvent;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.LiveResult;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.MutableLiveEvent;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.connection.ConnectionManager;

import static eu.h2020.helios_social.happ.helios.talk.api.contact.HeliosLinkConstants.LINK_REGEX;
import static java.util.logging.Logger.getLogger;

@NotNullByDefault
public class AddContactViewModel extends AndroidViewModel {

	private final static Logger LOG =
			getLogger(AddContactViewModel.class.getName());

	private final IdentityManager identityManager;
	private final ConnectionManager connectionManager;
	private final PendingContactFactory pendingContactFactory;
	@DatabaseExecutor
	private final Executor dbExecutor;

	private final MutableLiveData<String> heliosLink =
			new MutableLiveData<>();
	private final MutableLiveEvent<Boolean> remoteLinkEntered =
			new MutableLiveEvent<>();
	private final MutableLiveData<LiveResult<Boolean>> addContactResult =
			new MutableLiveData<>();
	@Nullable
	private String remoteHeliosLink;

	@Inject
	AddContactViewModel(Application application,
			IdentityManager identityManager,
			ConnectionManager connectionManager,
			PendingContactFactory pendingContactFactory,
			@DatabaseExecutor Executor dbExecutor) {
		super(application);
		this.identityManager = identityManager;
		this.connectionManager = connectionManager;
		this.pendingContactFactory = pendingContactFactory;
		this.dbExecutor = dbExecutor;
	}

	void onCreate() {
		if (heliosLink.getValue() == null) loadHandshakeLink();
	}

	private void loadHandshakeLink() {
		dbExecutor.execute(() -> {
			heliosLink.postValue(identityManager.getHeliosLink());
		});
	}

	LiveData<String> getHeliosLink() {
		return heliosLink;
	}

	@Nullable
	String getRemoteHeliosLink() {
		return remoteHeliosLink;
	}

	void setRemoteHeliosLink(String link) {
		remoteHeliosLink = link;
	}

	boolean isValidHeliosLink(@Nullable CharSequence link) {
		return link != null && LINK_REGEX.matcher(link).find();
	}

	LiveEvent<Boolean> getRemoteHeliosLinkEntered() {
		return remoteLinkEntered;
	}

	void onRemoteLinkEntered() {
		if (remoteHeliosLink == null) throw new IllegalStateException();
		remoteLinkEntered.setEvent(true);
	}

	void addContact(String nickname, String message) {
		if (remoteHeliosLink == null) throw new IllegalStateException();
		dbExecutor.execute(() -> {

					connectionManager.sendConnectionRequest(pendingContactFactory
							.createOutgoingPendingContact(
									remoteHeliosLink.replace("helios://", ""),
									nickname,
									message));
					addContactResult.postValue(new LiveResult<>(true));
				}
		);
	}

	LiveData<LiveResult<Boolean>> getAddContactResult() {
		return addContactResult;
	}

	/*public void updatePendingContact(String name, PendingContact p) {
		dbExecutor.execute(() -> {
			try {
				contactManager.deletePendingContact(p.getId());
				addContact(name);
			} catch (NoSuchPendingContactException e) {
				logException(LOG, WARNING, e);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				addContactResult.postValue(new LiveResult<>(e));
			}
		});
	}*/

}
