package eu.h2020.helios_social.modules.groupcommunications.api.privategroup;

import java.util.Collection;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;

/**
 * Interface for PrivateGroupManager
 */
public interface PrivateGroupManager<T> {

    /**
     * Adds a new Private Group the user has joined/created
     *
     * @param privateGroup
     * @return
     */
    void addPrivateGroup(PrivateGroup privateGroup)
            throws DbException, FormatException;

    /**
     * Stores a new private group, the user joined/created
     *
     * @param txn
     * @param privateGroup
     * @throws FormatException
     * @throws DbException
     */
    void addPrivateGroup(T txn,
                         PrivateGroup privateGroup)
            throws FormatException, DbException;

    /**
     * Leaves a private group that was previously joined
     *
     * @param privateGroup
     */
    void leavePrivateGroup(PrivateGroup privateGroup);


    /**
     * Returns the private group with the given identifier
     *
     * @param gid group unique Identifier
     * @return
     */
    PrivateGroup getPrivateGroup(String gid)
            throws FormatException, DbException;

    /**
     * Returns all private groups in the given context, the user has joined
     *
     * @param contextId
     * @return
     */
    Collection<PrivateGroup> getPrivateGroups(String contextId)
            throws DbException, FormatException;

    Collection<PrivateGroup> getPrivateGroups()
            throws DbException, FormatException;

    /**
     * Returns all private groups the user has joined
     *
     * @param txn
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<PrivateGroup> getPrivateGroups(T txn)
            throws DbException, FormatException;

    /**
     * Returns all contact members of the given Private Group
     *
     * @param groupId
     * @return s collection of Private Group Members
     */
    Collection<Contact> getMembers(String groupId)
            throws DbException, FormatException;

    /**
     * Returns all contact members of the given Private Group
     *
     * @param txn
     * @param groupId
     * @return
     * @throws DbException
     * @throws FormatException
     */
    Collection<Contact> getMembers(T txn, String groupId)
            throws DbException, FormatException;

    /**
     * Adds a member to the given private group
     *
     * @param groupId
     * @param contactId
     * @throws DbException
     * @throws FormatException
     */
    void addMember(String groupId, ContactId contactId)
            throws DbException, FormatException;

    /**
     * Adds a member to the given private group
     *
     * @param txn
     * @param groupId
     * @param contactId
     * @throws DbException
     * @throws FormatException
     */
    void addMember(T txn, String groupId, ContactId contactId)
            throws DbException, FormatException;

}
