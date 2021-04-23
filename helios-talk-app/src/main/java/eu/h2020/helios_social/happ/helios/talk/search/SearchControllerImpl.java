package eu.h2020.helios_social.happ.helios.talk.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.inject.Inject;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.controller.DbController;
import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.SharingGroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.resourcediscovery.EntityType;
import eu.h2020.helios_social.modules.groupcommunications.api.resourcediscovery.queries.LocationQuery;
import eu.h2020.helios_social.modules.groupcommunications.api.resourcediscovery.queries.QueryManager;
import eu.h2020.helios_social.modules.groupcommunications.api.resourcediscovery.queries.TextQuery;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;

public class SearchControllerImpl extends DbControllerImpl implements SearchController {

    private final QueryManager queryManager;
    private final ContextualEgoNetwork egoNetwork;
    private final GroupManager groupManager;
    private final SharingGroupManager sharingGroupManager;

    @Inject
    public SearchControllerImpl(@DatabaseExecutor Executor dbExecutor, LifecycleManager lifecycleManager,
                                QueryManager queryManager, ContextualEgoNetwork egoNetwork,
                                GroupManager groupManager, SharingGroupManager sharingGroupManager) {
        super(dbExecutor, lifecycleManager);
        this.queryManager = queryManager;
        this.egoNetwork = egoNetwork;
        this.groupManager = groupManager;
        this.sharingGroupManager = sharingGroupManager;
    }

    @Override
    public String sendTextQuery(String q) {
        try {
            TextQuery query = createTextQuery(q);
            queryManager.sendQuery(query);
            return query.getQueryId();
        } catch (DbException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String sendLocationQuery(double lat, double lng, int radius) {
        try {
            LocationQuery query = createLocationQuery(lat, lng, radius);
            queryManager.sendQuery(query);
            return query.getQueryId();
        } catch (DbException e) {
            e.printStackTrace();
            return null;
        }
    }

    private LocationQuery createLocationQuery(double lat, double lng, int radius) {
        long timpestamp = System.currentTimeMillis();
        String contextId = egoNetwork.getCurrentContext().getData().toString().split("%")[1];
        return new LocationQuery(UUID.randomUUID().toString(), contextId, lat, lng, radius / 1.0, EntityType.FORUM, timpestamp, 2);
    }

    private TextQuery createTextQuery(String q) {
        long timpestamp = System.currentTimeMillis();
        String contextId = egoNetwork.getCurrentContext().getData().toString().split("%")[1];
        return new TextQuery(UUID.randomUUID().toString(), contextId, q, EntityType.FORUM, timpestamp, 2);
    }

    @Override
    public List<String> getLocalForums() {
        try {
            String contextId = egoNetwork.getCurrentContext().getData().toString().split("%")[1];
            Collection<Forum> groups = groupManager.getForums(contextId);
            return groups.stream().map(group -> group.getId()).collect(Collectors.toList());
        } catch (FormatException | DbException e) {
            e.printStackTrace();
            return new ArrayList();
        }
    }

    @Override
    public void joinPublicForum(Forum forum) {
        runOnDbThread(() -> {
            try {
                sharingGroupManager.joinForum(forum);
            } catch (DbException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }
        });
    }


}
