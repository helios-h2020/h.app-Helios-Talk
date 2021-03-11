package eu.h2020.helios_social.happ.helios.talk.navdrawer;

import android.app.Activity;
import android.content.Context;

import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.Settings;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsManager;
import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultHandler;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsConsts.SETTINGS_NAMESPACE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.happ.helios.talk.controller.HeliosTalkControllerImpl.DOZE_ASK_AGAIN;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class NavDrawerControllerImpl extends DbControllerImpl
		implements NavDrawerController, EventListener {

	private static final Logger LOG =
			getLogger(NavDrawerControllerImpl.class.getName());

	private static final String EXPIRY_DATE_WARNING = "expiryDateWarning";

	private final SettingsManager settingsManager;
	private final EventBus eventBus;


	@Inject
	NavDrawerControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			SettingsManager settingsManager, EventBus eventBus) {
		super(dbExecutor, lifecycleManager);
		this.settingsManager = settingsManager;
		this.eventBus = eventBus;
	}

	@Override
	public void onActivityCreate(Activity activity) {
//		listener = (TransportStateListener) activity;
	}

	@Override
	public void onActivityStart() {
		eventBus.addListener(this);
	}

	@Override
	public void onActivityStop() {
		eventBus.removeListener(this);
	}

	@Override
	public void onActivityDestroy() {
	}

	@Override
	public void eventOccurred(Event e) {
		/*if (e instanceof TransportEnabledEvent) {
			TransportId id = ((TransportEnabledEvent) e).getTransportId();
			if (LOG.isLoggable(INFO)) {
				LOG.info("TransportEnabledEvent: " + id.getString());
			}
			listener.stateUpdate(id, true);
		} else if (e instanceof TransportDisabledEvent) {
			TransportId id = ((TransportDisabledEvent) e).getTransportId();
			if (LOG.isLoggable(INFO)) {
				LOG.info("TransportDisabledEvent: " + id.getString());
			}
			listener.stateUpdate(id, false);
		}*/
	}

	@Override
	public void shouldAskForDozeWhitelisting(Context ctx,
			ResultHandler<Boolean> handler) {
		// check this first, to hit the DbThread only when really necessary
		if (!UiUtils.needsDozeWhitelisting(ctx)) {
			handler.onResult(false);
			return;
		}
		runOnDbThread(() -> {
			try {
				Settings settings =
						settingsManager.getSettings(
								SETTINGS_NAMESPACE);
				boolean ask = settings.getBoolean(DOZE_ASK_AGAIN, true);
				handler.onResult(ask);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onResult(true);
			}
		});
	}

}
