package eu.h2020.helios_social.modules.groupcommunications.api.context.sharing;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

/**
 * Interface for Context Invitation Factory
 */
public interface ContextInvitationFactory<T> {

    /**
     * Creates an outgoing Context Invitation given the contact identifier and context
     *
     * @param contactId
     * @param context
     * @return
     */
    ContextInvitation createOutgoingContextInvitation(ContactId contactId,
                                                      T context);

    /**
     * Creates an incoming Context Invitation given the contact identifier and context info.
     *
     * @param contactId
     * @param contextInfo
     * @return
     */
    ContextInvitation createIncomingContextInvitation(ContactId contactId,
                                                      ContextInfo contextInfo);
}
