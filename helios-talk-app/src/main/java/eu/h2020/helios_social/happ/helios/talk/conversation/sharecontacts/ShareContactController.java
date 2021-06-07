package eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts;

import java.util.Collection;

import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;

public interface ShareContactController {

    void shareContact(PeerInfo peerInfo, Collection<ContactId> contacts, ResultExceptionHandler<Void, DbException> result);
}
