package eu.h2020.helios_social.happ.helios.talk.privategroup.creation;

import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;

interface CreateGroupListener extends BaseFragment.BaseFragmentListener {

	void onGroupNameChosen(String name);
}
