package eu.h2020.helios_social.happ.helios.talk.login;

import android.app.Application;

import eu.h2020.helios_social.modules.groupcommunications_utils.account.AccountManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.crypto.DecryptionException;
import eu.h2020.helios_social.modules.groupcommunications_utils.crypto.DecryptionResult;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.IoExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager.LifecycleState;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.event.LifecycleEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.LiveEvent;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.MutableLiveEvent;
import eu.h2020.helios_social.happ.android.AndroidNotificationManager;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static eu.h2020.helios_social.modules.groupcommunications_utils.crypto.DecryptionResult.SUCCESS;
import static eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager.LifecycleState.COMPACTING_DATABASE;
import static eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager.LifecycleState.MIGRATING_DATABASE;
import static eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.COMPACTING;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.MIGRATING;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.SIGNED_IN;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.SIGNED_OUT;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.STARTED;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.STARTING;
import static eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager.LifecycleState.STOPPING;

@NotNullByDefault
public class StartupViewModel extends AndroidViewModel
		implements EventListener {

	enum State {SIGNED_OUT, SIGNED_IN, STARTING, MIGRATING, COMPACTING, STARTED}

	private final AccountManager accountManager;
	private final AndroidNotificationManager notificationManager;
	private final EventBus eventBus;
	@IoExecutor
	private final Executor ioExecutor;

	private final MutableLiveEvent<DecryptionResult> passwordValidated =
			new MutableLiveEvent<>();
	private final MutableLiveEvent<Boolean> accountDeleted =
			new MutableLiveEvent<>();
	private final MutableLiveData<State> state = new MutableLiveData<>();

	@Inject
	StartupViewModel(Application app,
			AccountManager accountManager,
			LifecycleManager lifecycleManager,
			AndroidNotificationManager notificationManager,
			EventBus eventBus,
			@IoExecutor Executor ioExecutor) {
		super(app);
		this.accountManager = accountManager;
		this.notificationManager = notificationManager;
		this.eventBus = eventBus;
		this.ioExecutor = ioExecutor;

		updateState(lifecycleManager.getLifecycleState());
		eventBus.addListener(this);
	}

	@Override
	protected void onCleared() {
		eventBus.removeListener(this);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof LifecycleEvent) {
			LifecycleState s = ((LifecycleEvent) e).getLifecycleState();
			updateState(s);
		}
	}

	@UiThread
	private void updateState(LifecycleState s) {
		if (accountManager.hasDatabaseKey()) {
			if (s == STOPPING) state.setValue(SIGNED_OUT);
			else if (s.isAfter(STARTING_SERVICES)) state.setValue(STARTED);
			else if (s == MIGRATING_DATABASE) state.setValue(MIGRATING);
			else if (s == COMPACTING_DATABASE) state.setValue(COMPACTING);
			else state.setValue(STARTING);
		} else {
			state.setValue(SIGNED_OUT);
		}
	}

	boolean accountExists() {
		return accountManager.accountExists();
	}

	void clearSignInNotification() {
		notificationManager.blockSignInNotification();
		notificationManager.clearSignInNotification();
	}

	void validatePassword(String password) {
		ioExecutor.execute(() -> {
			try {
				accountManager.signIn(password);
				passwordValidated.postEvent(SUCCESS);
				state.postValue(SIGNED_IN);
			} catch (DecryptionException e) {
				passwordValidated.postEvent(e.getDecryptionResult());
			}
		});
	}

	LiveEvent<DecryptionResult> getPasswordValidated() {
		return passwordValidated;
	}

	LiveEvent<Boolean> getAccountDeleted() {
		return accountDeleted;
	}

	LiveData<State> getState() {
		return state;
	}

	@UiThread
	void deleteAccount() {
		accountManager.deleteAccount();
		accountDeleted.setEvent(true);
	}

}
