package eu.h2020.helios_social.happ.helios.talk.search;

import java.util.List;

import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.resourcediscovery.queries.Queryable;

public class ForumResultItem implements ResultItem {

    private String group_id, title;
    private List<String> tags;
    private boolean isLocal;
    private double distance;
    private Forum item;

    public ForumResultItem(Forum forum, double distance, boolean isLocal) {
        this.group_id = forum.getId();
        this.title = forum.getName();
        this.isLocal = isLocal;
        this.tags = forum.getTags();
        this.distance = distance;
        this.item = forum;
    }

    @Override
    public String getId() {
        return group_id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public boolean isLocal() {
        return isLocal;
    }

    @Override
    public boolean showDistance() {
        return false;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public Queryable getItem() {
        return item;
    }
}
