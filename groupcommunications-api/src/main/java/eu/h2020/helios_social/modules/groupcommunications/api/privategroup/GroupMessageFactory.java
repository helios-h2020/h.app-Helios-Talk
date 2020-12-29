package eu.h2020.helios_social.modules.groupcommunications.api.privategroup;

import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupMessage;

public interface GroupMessageFactory {

    /**
     * Greates new Group Message
     *
     * @param groupId   group identifier
     * @param text      message body
     * @param timestamp
     * @param funnyName user's pseudonym for the group
     * @param fakeId    user's pseudo-identifier for the group
     * @return
     */
    GroupMessage createGroupMessage(String groupId,
                                    String text, long timestamp, String funnyName, String fakeId);
}
