package eu.h2020.helios_social.happ.helios.talk.forum.creation;

import java.util.ArrayList;

import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumMemberRole;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;

interface CreateForumListener extends BaseFragment.BaseFragmentListener {

    void onNamedForumChosen(String name, GroupType groupType, ArrayList<String> tags,
                            ForumMemberRole defaultMemberRole);

    void onLocationForumChosen(String name, GroupType groupType, ArrayList<String> tags,
                               double latitude, double longitude, int radius,
                               ForumMemberRole defaultMemberRole);
}
