package eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorController;
import eu.h2020.helios_social.happ.helios.talk.contactselection.SelectableContactItem;
import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.ContactInfo;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingManager;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;
import eu.h2020.helios_social.modules.groupcommunications.api.privateconversation.PrivateMessageFactory;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;

public class ShareContactControllerImpl extends DbControllerImpl
        implements ContactSelectorController<SelectableContactItem>, ShareContactController {
    private static Logger LOG = Logger.getLogger(ShareContactController.class.getName());

    private final ContactManager contactManager;
    private final ContextualEgoNetwork egoNetwork;
    private final PrivateMessageFactory privateMessageFactory;
    private final ConversationManager conversationManager;
    private final MessagingManager messagingManager;

    @Inject
    public ShareContactControllerImpl(@DatabaseExecutor Executor dbExecutor,
                                      LifecycleManager lifecycleManager, ContactManager contactManager,
                                      ContextualEgoNetwork egoNetwork, PrivateMessageFactory privateMessageFactory,
                                      ConversationManager conversationManager, MessagingManager messagingManager) {
        super(dbExecutor, lifecycleManager);
        this.contactManager = contactManager;
        this.egoNetwork = egoNetwork;
        this.privateMessageFactory = privateMessageFactory;
        this.conversationManager = conversationManager;
        this.messagingManager = messagingManager;
    }

    @Override
    public void shareContact(PeerInfo peerInfo, Collection<ContactId> contacts, ResultExceptionHandler<Void, DbException> result) {
        runOnDbThread(() -> {
            try {
                String currentContext =
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1];
                long timestamp = System.currentTimeMillis();
                for (ContactId c : contacts) {
                    Group contactGroup = conversationManager.getContactGroup(c, currentContext);
                    ContactInfo contactInfo = (ContactInfo) privateMessageFactory
                            .createShareContactMessage(contactGroup.getId(), timestamp, peerInfo);
                    messagingManager.sendPrivateMessage(c, currentContext, contactInfo);
                }
                result.onResult(null);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                result.onException(e);
            }
        });
    }

    @Override
    public void loadContacts(String groupId, Collection<ContactId> selection, ResultExceptionHandler<Collection<SelectableContactItem>, DbException> handler) {
        runOnDbThread(() -> {
            try {
                Collection<SelectableContactItem> contacts = new ArrayList<>();
                String currentContext =
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1];
                for (Contact c : contactManager.getContacts(currentContext)) {
                    // was this contact already selected?
                    boolean selected =
                            selection.contains(c.getId());
                    contacts.add(new SelectableContactItem(c, selected, false));
                }
                handler.onResult(contacts);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }
}
