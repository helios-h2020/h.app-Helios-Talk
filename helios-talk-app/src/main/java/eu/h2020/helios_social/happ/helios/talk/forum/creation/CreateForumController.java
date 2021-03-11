package eu.h2020.helios_social.happ.helios.talk.forum.creation;


import java.util.ArrayList;
import java.util.Collection;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorController;
import eu.h2020.helios_social.happ.helios.talk.contactselection.SelectableContactItem;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

@NotNullByDefault
public interface CreateForumController
        extends ContactSelectorController<SelectableContactItem> {

    void createNamedForum(String name, GroupType groupType, ArrayList<String> tags,
                          ForumMemberRole dafaultMemberRole,
                          ResultExceptionHandler<String, DbException> result);

    void createLocationForum(String name, GroupType groupType, ArrayList<String> tags,
                             double latitude, double longitude, int radius,
                             ForumMemberRole defaultMemberRole,
                             ResultExceptionHandler<String, DbException> result);

    void sendInvitation(String groupId, Collection<ContactId> contacts,
                        ResultExceptionHandler<Void, DbException> result);

}
