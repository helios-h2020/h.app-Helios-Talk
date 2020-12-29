package eu.h2020.helios_social.modules.groupcommunications.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.AbstractMessage;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerId;
import eu.h2020.helios_social.modules.groupcommunications.api.utils.GroupId;

/**
 * Interface for GCS's Communication Manager
 */
public interface CommunicationManager<T> {

    /**
     * Sends a message to the defined contact. Different type of messages are supported based on
     * the defined protocol identifier. Note that, all messages need to implement AbstractMessage
     * interface
     *
     * @param protocolId
     * @param contactId
     * @param message
     */
    void sendDirectMessage(String protocolId, ContactId contactId,
                           AbstractMessage message)
            throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Sends a message to the defined peer. Different type of messages are supported based on
     * the defined protocol identifier. Note that, all messages need to implement AbstractMessage
     * interface
     *
     * @param protocolId
     * @param peerId
     * @param message
     */
    void sendDirectMessage(String protocolId, PeerId peerId,
                           AbstractMessage message)
            throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Sends a group message given the group identifier and password.
     *
     * @param groupId
     * @param password
     * @param message
     */
    void sendGroupMessage(String groupId, String password,
                          AbstractMessage message);

    /**
     * Registers a receiver for the given protocol ID
     *
     * @param protocolId
     * @param receiver
     */
    void registerReceiver(String protocolId, T receiver);

    /**
     * User announces herself in the given Tag
     *
     * @param tag
     */
    void announceTag(String tag);

    /**
     * User observes the given tag
     *
     * @param tag
     */
    void observeTag(String tag);

    /**
     * User unannouce the given tag
     *
     * @param tag
     */
    void unannounceTag(String tag);

    /**
     * User unobserve the given tag
     *
     * @param tag
     */
    void unobserveTag(String tag);

    /**
     * Subscribes to a group conversation given the group identifier and password.
     *
     * @param groupId
     * @param password
     */
    void subscribe(String groupId, String password);

    /**
     * Unsubscribes from a group conversation given the group identifier and password.
     *
     * @param groupId
     * @param password
     */
    void unsubscribe(String groupId, String password);
}
