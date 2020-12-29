package eu.h2020.helios_social.modules.groupcommunications.api.forum;


import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

/**
 * A Sharable Location Forum
 */
public class LocationForum extends Forum {

    private double latitude;
    private double longitude;
    private int radius;

    public LocationForum(@NotNull String groupId, @NotNull String contextId,
                         @NotNull String name, String password,
                         @NotNull Collection<String> moderators, GroupType groupType,
                         Collection<String> tags, ForumMemberRole defaultMemberRole,
                         double latitude, double longitude,
                         int radius) {
        super(groupId, contextId, name, password, moderators, groupType,
                tags, defaultMemberRole);
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }
}
