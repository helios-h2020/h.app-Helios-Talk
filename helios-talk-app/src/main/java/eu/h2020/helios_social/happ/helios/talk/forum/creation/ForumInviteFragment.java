package eu.h2020.helios_social.happ.helios.talk.forum.creation;

import android.os.Bundle;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorController;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorFragment;
import eu.h2020.helios_social.happ.helios.talk.contactselection.SelectableContactItem;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ForumInviteFragment extends ContactSelectorFragment {

	public static final String TAG = ForumInviteFragment.class.getName();

	@Inject
    CreateForumController controller;

	public static ForumInviteFragment newInstance(String groupId) {
		Bundle args = new Bundle();
		args.putString(GROUP_ID, groupId);
		ForumInviteFragment fragment = new ForumInviteFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requireActivity().setTitle(R.string.groups_invite_members);
	}

	@Override
	protected ContactSelectorController<SelectableContactItem> getController() {
		return controller;
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

}
