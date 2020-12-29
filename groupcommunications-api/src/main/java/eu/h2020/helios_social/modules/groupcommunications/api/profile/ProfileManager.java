package eu.h2020.helios_social.modules.groupcommunications.api.profile;

import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

/**
 * Interface for Profile Manager
 */
public interface ProfileManager<T> {

    /**
     * Stores a profile in database.
     *
     * @param profile
     * @throws DbException
     */
    void addProfile(Profile profile) throws DbException;

    /**
     * Updates the profile
     *
     * @param profile
     * @throws DbException
     */
    void updateProfile(Profile profile) throws DbException;

    /**
     * Returns true if the user has already defined a Profile in the context with the given
     * identifier.
     *
     * @param contextId
     * @return
     * @throws DbException
     */
    boolean containsProfile(String contextId) throws DbException;

    /**
     * Deletes profile given the context identifier
     *
     * @param contextId
     * @throws DbException
     */
    void removeProfile(String contextId) throws DbException;

    /**
     * Returns user profile in the given context
     *
     * @param contextId
     * @return
     * @throws DbException
     */
    Profile getProfile(String contextId) throws DbException;
}
