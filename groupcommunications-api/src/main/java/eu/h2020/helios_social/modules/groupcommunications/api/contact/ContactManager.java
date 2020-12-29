package eu.h2020.helios_social.modules.groupcommunications.api.contact;

import java.util.Collection;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

public interface ContactManager {

    /**
     * Stores  a new contact
     *
     * @param contact
     * @throws DbException
     */
    public void addContact(Contact contact) throws DbException;

    /**
     * Stores a new pending contact
     *
     * @param pendingContact
     * @throws DbException
     */
    public void addPendingContact(PendingContact pendingContact)
            throws DbException;

    /**
     * Returns a pending contact based on the given contact identifier
     *
     * @param pendingContactId
     * @return
     * @throws DbException
     */
    PendingContact getPendingContact(ContactId pendingContactId)
            throws DbException;

    /**
     * Returns a contact baded on the given contact identifier
     *
     * @param contactId
     * @return
     * @throws DbException
     */
    Contact getContact(ContactId contactId)
            throws DbException;

    /**
     * Returns true if the contact with the given identifier extists
     *
     * @param contactId
     * @return
     * @throws DbException
     */
    boolean contactExists(ContactId contactId)
            throws DbException;

    /**
     * Deletes contact with the given identifier
     *
     * @param contactId
     * @throws DbException
     */
    public void deleteContact(ContactId contactId) throws DbException;

    /**
     * Deletes the pending contact with the given identifier
     *
     * @param contactId
     * @throws DbException
     */
    void deletePendingContact(ContactId contactId) throws DbException;

    /**
     * Deletes all contacts
     */
    public void deleteAllContacts();

    /**
     * Returns all contacts
     *
     * @return
     * @throws DbException
     */
    Collection<Contact> getContacts() throws DbException;

    /**
     * Returns all contacts of the given context
     *
     * @param contextId
     * @return
     * @throws DbException
     */
    Collection<Contact> getContacts(
            String contextId) throws DbException;

	/**
	 * Returns all pending contacts
	 * @return
	 * @throws DbException
	 */
	public Collection<PendingContact> getPendingContacts() throws DbException;

}
