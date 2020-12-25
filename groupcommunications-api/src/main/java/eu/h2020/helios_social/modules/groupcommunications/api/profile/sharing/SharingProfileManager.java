package eu.h2020.helios_social.modules.groupcommunications.api.profile.sharing;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

public interface SharingProfileManager {

    void sendProfileRequest(ContactId contactId, String contextId);

    void sendProfileResponse(ContactId contactId, String contextId) throws DbException;
}
