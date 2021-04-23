package eu.h2020.helios_social.happ.helios.talk.search;

import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.controller.DbController;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;

public interface SearchController extends DbController {

    /**
     * Sends a text query and returns the queryId
     *
     * @param q
     * @return
     */
    String sendTextQuery(String q);

    /**
     * Sends a location query and returns the queryId
     *
     * @param lat
     * @param lng
     * @param radius
     * @return
     */
    String sendLocationQuery(double lat, double lng, int radius);

    List<String> getLocalForums();

    void joinPublicForum(Forum forum);
}
