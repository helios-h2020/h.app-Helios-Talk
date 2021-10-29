package eu.h2020.helios_social.happ.helios.talk.context;

import android.util.Log;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import eu.h2020.helios_social.core.context.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.SpatioTemporalContext;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContextContactSelectorControllerImpl;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.ContextInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.ContextInvitationFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.SharingContextManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.GeneralContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.LocationContextProxy;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static java.util.logging.Level.WARNING;

public class ContextControllerImpl extends
        ContextContactSelectorControllerImpl
        implements ContextController {

    private static final Logger LOG =
            Logger.getLogger(ContextController.class.getName());

    private final ContextualEgoNetwork egoNetwork;
    private final SharingContextManager sharingContextManager;
    private final ContextInvitationFactory contextInvitationFactory;

    @Inject
    public ContextControllerImpl(@DatabaseExecutor Executor dbExecutor,
                                 LifecycleManager lifecycleManager, ContextManager contextManager,
                                 ContextualEgoNetwork egoNetwork, ContactManager contactManager,
                                 SharingContextManager sharingContextManager,
                                 ContextInvitationFactory contextInvitationFactory) {
        super(dbExecutor, lifecycleManager, contactManager, contextManager);
        this.egoNetwork = egoNetwork;
        this.sharingContextManager = sharingContextManager;
        this.contextInvitationFactory = contextInvitationFactory;
    }

    @Override
    public void invite(String contextId, Collection<ContactId> contacts,
                       ExceptionHandler<DbException> handler) {
        runOnDbThread(() -> {
            try {
                Context context = contextManager.getContext(contextId, true);

                for (ContactId contact : contacts) {
                    sharingContextManager.sendContextInvitation(
                            contextInvitationFactory
                                    .createOutgoingContextInvitation(contact,
                                                                     context)
                    );
                }
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            } catch (FormatException fe) {
                logException(LOG, WARNING, fe);
            }
        });
    }

    @Override
    public String getCurrentContext() {
        return egoNetwork.getCurrentContext().getData().toString()
                .split("%")[1];
    }

    @Override
    public Context getContext(String contextId)
            throws DbException, FormatException {
        return contextManager.getContext(contextId);

    }

    @Override
    public int getPendingIncomingContextInvitations()
            throws DbException {
        return contextManager.pendingIncomingContextInvitations();

    }

    @Override
    public void storeLocationContext(LocationContextProxy locationContext,
                                     ResultExceptionHandler<String, DbException> handler) {
        runOnDbThread(() -> {
            try {
                long start = now();
                contextManager.addContext(locationContext);
                logDuration(LOG, "Storing new location context", start);
                egoNetwork.setCurrent(egoNetwork
                                              .getOrCreateContext(locationContext.getName() + "%" +
                                                                          locationContext.getId()));
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    public void storeSpatioTemporalContext(SpatioTemporalContext spatioTemporalContext, ResultExceptionHandler<String, DbException> handler) {
        runOnDbThread(() -> {
            try {
                long start = now();
                contextManager.addContext(spatioTemporalContext);
                logDuration(LOG, "Storing new spatiotemporal context", start);
                egoNetwork.setCurrent(egoNetwork
                        .getOrCreateContext(spatioTemporalContext.getName() + "%" +
                                spatioTemporalContext.getId()));
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    public void storeGeneralContext(GeneralContextProxy generalContext,
                                    ResultExceptionHandler<String, DbException> handler) {
        runOnDbThread(() -> {
            try {
                long start = now();
                contextManager.addContext(generalContext);
                logDuration(LOG, "Storing new general context", start);
                LOG.info("EGO NETWORK " + egoNetwork);
                LOG.info("CNAME " + generalContext.getName());
                LOG.info("CID " + generalContext.getId());
                egoNetwork.setCurrent(egoNetwork.getOrCreateContext(generalContext.getName() + "%" +
                                                                            generalContext.getId()));
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    public void deleteContext(String contextId,
                              ResultExceptionHandler<Void, DbException> handler) {
        runOnDbThread(() -> {
            try {
                long start = now();
                egoNetwork.removeContext(egoNetwork.getCurrentContext());
                contextManager.removeContext(contextId);

                logDuration(LOG, "Removing context", start);
                handler.onResult(null);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });

    }

    @Override
    public void setContextPrivateName(String contextId, String name, ResultExceptionHandler<Void, DbException> handler) {
        runOnDbThread(()-> {
            try {
                long start = now();
                Log.d("settingContextPrivateName: ","true");
                contextManager.setContextPrivateName(contextId,name);

            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    public Integer getContextColor(String contextId) throws DbException {
        Integer color = contextManager.getContextColor(
                contextId
        );
        return color;

    }

    @Override
    public String getContextPrivateName(String contextId) throws DbException {
        String privateName = contextManager.getContextPrivateName(
                contextId
        );
        return privateName;

    }

    @Override
    public String getContextName(String contextId) throws DbException {
        String name = contextManager.getContextName(
                contextId
        );
        return name;
    }

    @Override
    public void setContextName(String contextId, String name, ResultExceptionHandler<Void, DbException> handler) throws DbException {
        runOnDbThread(()-> {
            try {
                long start = now();
                contextManager.setContextName(contextId,name);

            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    protected boolean isDisabled(String contextId, Contact c,
                                 Collection<ContextInvitation> contextInvitations)
            throws DbException {
        boolean isInvited = contextInvitations.stream().anyMatch(invite -> {
            return invite.getContactId().equals(c.getId());
        });
        return contextManager.belongsToContext(c.getId(), contextId) ||
                isInvited;
    }
}
