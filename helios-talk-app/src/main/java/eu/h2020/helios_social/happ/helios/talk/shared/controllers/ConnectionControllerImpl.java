package eu.h2020.helios_social.happ.helios.talk.shared.controllers;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.connection.ConnectionManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.InvalidActionException;
import eu.h2020.helios_social.modules.groupcommunications_utils.identity.IdentityManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static java.util.logging.Level.WARNING;

public class ConnectionControllerImpl extends DbControllerImpl implements ConnectionController {
    private static Logger LOG = Logger.getLogger(ConnectionControllerImpl.class.getName());
    private final String DEFAULT_MESSAGE = "Hello! I would like to add you to my connections!";

    private ConnectionManager connectionManager;
    private PendingContactFactory pendingContactFactory;
    private IdentityManager identityManager;

    @Inject
    public ConnectionControllerImpl(@DatabaseExecutor Executor dbExecutor, LifecycleManager lifecycleManager,
                                    ConnectionManager connectionManager, PendingContactFactory pendingContactFactory,
                                    IdentityManager identityManager) {
        super(dbExecutor, lifecycleManager);
        this.connectionManager = connectionManager;
        this.pendingContactFactory = pendingContactFactory;
        this.identityManager = identityManager;
    }

    @Override
    public void sendConnectionRequest(PeerInfo peerInfo, UiResultExceptionHandler<Void, Exception> handler) {
        runOnDbThread(() -> {
            try {
                long start = now();
                if (identityManager.getIdentity().getNetworkId().equals(peerInfo.getPeerId().getId()))
                    throw new InvalidActionException("Invalid Action! You try to send connection request to yourself!");
                connectionManager.sendConnectionRequest(pendingContactFactory
                                                                .createOutgoingPendingContact(
                                                                        peerInfo.getPeerId().getId(),
                                                                        peerInfo.getAlias(),
                                                                        DEFAULT_MESSAGE));
                logDuration(LOG, "Send connection request...", start);
                handler.onResult(null);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }
}
