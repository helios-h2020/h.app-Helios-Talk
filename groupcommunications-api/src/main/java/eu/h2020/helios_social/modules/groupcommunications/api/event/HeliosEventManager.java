package eu.h2020.helios_social.modules.groupcommunications.api.event;

import java.util.Collection;

import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

/**
 * Interface for Helios Event Manager
 */
public interface HeliosEventManager {

    /**
     * Stores a Helios Event in database.
     *
     * @param event
     * @throws DbException
     */
    void addEvent(HeliosEvent event) throws DbException;

    /**
     * Returns true if the event already exists
     *
     * @param eventId
     * @return
     * @throws DbException
     */
    boolean containsEvent(String eventId) throws DbException;

    /**
     * Deletes an event given its identifier
     *
     * @param eventId
     * @throws DbException
     */
    void removeEvent(String eventId) throws DbException;

    /**
     * Returns an event given its identifier
     *
     * @param eventId
     * @return
     * @throws DbException
     */
    HeliosEvent getEvent(String eventId) throws DbException;

    /**
     * Returns all events in the given context
     *
     * @param contextId
     * @return
     * @throws DbException
     */
    Collection<HeliosEvent> getEvents(String contextId) throws DbException;
}
