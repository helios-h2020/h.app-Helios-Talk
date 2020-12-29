package eu.h2020.helios_social.modules.groupcommunications.api.event.sharing;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.event.HeliosEvent;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;

/**
 * Interface for Sharing Event Manager
 */
public interface SharingEventManager {

    /**
     * Shares event in a private conversation
     *
     * @param contactId
     * @param groupId
     * @param heliosEvent
     */
    void shareEvent(ContactId contactId, String groupId, HeliosEvent heliosEvent);

    /**
     * Shares event in a group conversation
     *
     * @param group
     * @param heliosEvent
     */
    void shareEvent(Group group, HeliosEvent heliosEvent);
}
