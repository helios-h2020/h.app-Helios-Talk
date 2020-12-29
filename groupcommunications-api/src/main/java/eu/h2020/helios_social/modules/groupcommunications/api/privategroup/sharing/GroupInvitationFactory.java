package eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing;

import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

public interface GroupInvitationFactory {

    /**
     * Creates an incoming Group Invitation given the contact identifier and the corresponding
     * group info.
     *
     * @param contactId
     * @param groupInfo
     * @return
     */
    GroupInvitation createIncomingGroupInvitation(ContactId contactId,
                                                  GroupInfo groupInfo);

    /**
     * Creates an outgoing Group Invitation given the contact identifier and corresponding group.
     *
     * @param contactId
     * @param group
     * @return
     */
    GroupInvitation createOutgoingGroupInvitation(ContactId contactId,
                                                  Group group);
}
