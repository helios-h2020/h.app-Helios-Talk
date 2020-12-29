package eu.h2020.helios_social.modules.groupcommunications.api.event;

import org.jetbrains.annotations.NotNull;

import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumType;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.AbstractMessage;

public class HeliosEvent implements AbstractMessage {

    public enum Type {
        PRIVATE(0), PUBLIC(1), SHARABLE(2);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Type fromValue(int value) {
            for (Type s : values()) if (s.value == value) return s;
            throw new IllegalArgumentException();
        }
        }

    private String eventId;
    private String contextId;
    private long timestamp;
    private String title;
    private String description;
    private double lat, lng;
    private String url;
    private Type eventType;

    public HeliosEvent(@NotNull String eventId, @NotNull String contextId, @NotNull String title,
                       long timestamp) {
        this.eventId = eventId;
        this.title = title;
        this.timestamp = timestamp;
        this.contextId = contextId;
        this.eventType = Type.PUBLIC;
    }

    public HeliosEvent setDescription(String description) {
        this.description = description;
        return this;
    }

    public HeliosEvent setEventType(Type eventType) {
        this.eventType = eventType;
        return this;
    }

    public HeliosEvent setLocation(double latitude, double longitude) {
        this.lat = latitude;
        this.lng = longitude;
        return this;
    }

    public HeliosEvent setURL(String url) {
        this.url = url;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getContextId() {
        return contextId;
    }

    public String getDescription() {
        return description;
    }

    public Type getEventType() {
        return eventType;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lng;
    }

    public String getUrl() {
        return url;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
