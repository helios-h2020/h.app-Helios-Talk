package eu.h2020.helios_social.modules.groupcommunications.api.sharing;

import eu.h2020.helios_social.modules.groupcommunications.api.messaging.AbstractMessage;

/**
 * A class for Request
 */
public class Request implements AbstractMessage {

    public enum Type {
        PROFILE
    }

    private String contextId;
    private Type requestType;

    public Request(String contextId, Type requestType) {
        this.contextId = contextId;
        this.requestType = requestType;
    }

    public String getContextId() {
        return contextId;
    }

    public Type getRequestType() {
        return requestType;
    }
}
