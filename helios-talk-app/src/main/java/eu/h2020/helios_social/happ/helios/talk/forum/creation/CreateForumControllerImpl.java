package eu.h2020.helios_social.happ.helios.talk.forum.creation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import eu.h2020.helios_social.core.context.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.modules.groupcommunications.api.context.ContextType;
import eu.h2020.helios_social.modules.groupcommunications.api.context.DBContext;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.LocationContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.SpatioTemporalContext;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.SharingGroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitationFactory;
import eu.h2020.helios_social.modules.groupcommunications_utils.util.Location;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;

@Immutable
@NotNullByDefault
class CreateForumControllerImpl extends ContactSelectorControllerImpl
        implements CreateForumController {

    private static final Logger LOG =
            Logger.getLogger(CreateForumControllerImpl.class.getName());

    private final GroupFactory groupFactory;
    private final GroupManager groupManager;
    private final GroupInvitationFactory groupInvitationFactory;
    private final SharingGroupManager sharingGroupManager;
    private final EventBus eventBus;
    private final ContextManager contextManager;

    @Inject
    CreateForumControllerImpl(@DatabaseExecutor Executor dbExecutor,
                              LifecycleManager lifecycleManager, ContactManager contactManager,
                              GroupFactory groupFactory, GroupManager groupManager,
                              ContextualEgoNetwork egoNetwork,
                              GroupInvitationFactory groupInvitationFactory,
                              SharingGroupManager sharingGroupManager, EventBus eventBus, ContextManager contextManager) {
        super(dbExecutor, lifecycleManager, contactManager, egoNetwork);
        this.groupFactory = groupFactory;
        this.groupManager = groupManager;
        this.groupInvitationFactory = groupInvitationFactory;
        this.sharingGroupManager = sharingGroupManager;
        this.eventBus = eventBus;
        this.contextManager = contextManager;
    }

    @Override
    public void createNamedForum(String name, GroupType forumType, ArrayList<String> tags,
                                 ForumMemberRole dafaultMemberRole,
                                 ResultExceptionHandler<String, DbException> handler) {
        runOnDbThread(() -> {
            LOG.info("Creating and Adding forum to database...");
            try {
                String currentContextId =
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1];
                Forum forum =
                        groupFactory.createNamedForum(
                                name,
                                currentContextId,
                                forumType,
                                tags,
                                dafaultMemberRole
                        );
                groupManager.addGroup(forum);
                handler.onResult(forum.getId());
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            } catch (FormatException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void createLocationForum(String name, GroupType forumType, ArrayList<String> tags,
                                    double latitude, double longitude, int radius,
                                    ForumMemberRole defaultMemberRole,
                                    ResultExceptionHandler<String, DbException> handler) {
        runOnDbThread(() -> {
            LOG.info("Creating and Adding forum to database...");
            try {
                String currentContextId =
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1];
                Forum forum =
                        groupFactory.createLocationForum(
                                name,
                                currentContextId,
                                forumType,
                                tags,
                                defaultMemberRole, latitude, longitude, radius
                        );
                groupManager.addGroup(forum);
                handler.onResult(forum.getId());
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            } catch (FormatException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected boolean isDisabled(String groupId, Contact contact)
            throws DbException, FormatException {
        return !groupManager.isInvitationAllowed(groupId,
                                                 GroupType.PublicForum);
    }

    @Override
    public void sendInvitation(String groupId, Collection<ContactId> contactIds,
                               ResultExceptionHandler<Void, DbException> handler) {
        runOnDbThread(() -> {
            try {
                Forum forum = (Forum)
                        groupManager.getGroup(groupId, GroupType.PublicForum);
                for (ContactId c : contactIds) {
                    GroupInvitation groupInvitation = groupInvitationFactory
                            .createOutgoingGroupInvitation(c, forum);
                    sharingGroupManager.sendGroupInvitation(groupInvitation);
                }
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            } catch (FormatException e) {
                logException(LOG, WARNING, e);
                e.printStackTrace();
            }
        });
    }

    @Override
    public Location getLocationRestriction() {
        String currentContextId =
                egoNetwork.getCurrentContext().getData().toString()
                        .split("%")[1];

        try {
           Collection<DBContext> contexts = contextManager.getContexts();
           DBContext context =contexts.stream()
                    .filter(ctx -> currentContextId.equals(ctx.getId()))
                    .findFirst().orElse(null);
           if (context!=null){
               if (context.getContextType() == ContextType.LOCATION){
                   LocationContextProxy locationContextProxy = (LocationContextProxy) contextManager.getContext(currentContextId);
                   return new Location(locationContextProxy.getLat(),locationContextProxy.getLon(),locationContextProxy.getRadius());
               } else if (context.getContextType() == ContextType.SPATIOTEMPORAL){
                   SpatioTemporalContext spatioTemporalContext = (SpatioTemporalContext) contextManager.getContext(currentContextId);
                   return new Location(spatioTemporalContext.getLat(),spatioTemporalContext.getLon(),spatioTemporalContext.getRadius());
               }
           }
        } catch (DbException | FormatException e) {
            e.printStackTrace();
        }
        return null;
    }
}
