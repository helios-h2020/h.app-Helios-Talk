package eu.h2020.helios_social.happ.helios.talk.search;

import java.util.List;

import eu.h2020.helios_social.modules.groupcommunications.api.resourcediscovery.queries.Queryable;

public interface ResultItem {

    String getId();

    String getTitle();

    double getDistance();

    boolean isLocal();

    boolean showDistance();

    List<String> getTags();

    Queryable getItem();


}
