package eu.h2020.helios_social.happ.helios.talk.context.sharing;

import android.os.Bundle;


import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContextContactSelectorController;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContextContactSelectorFragment;
import eu.h2020.helios_social.happ.helios.talk.contactselection.SelectableContactItem;
import eu.h2020.helios_social.happ.helios.talk.context.ContextControllerImpl;

import static eu.h2020.helios_social.happ.helios.talk.contactselection.ContextContactSelectorActivity.CONTEXT_ID;

public class InvitingContactsFragment extends ContextContactSelectorFragment {

	public static final String TAG = InvitingContactsFragment.class.getName();

	@Inject
	ContextControllerImpl controller;

	public static InvitingContactsFragment newInstance(String contextId) {
		Bundle args = new Bundle();
		args.putString(CONTEXT_ID, contextId);
		InvitingContactsFragment fragment = new InvitingContactsFragment();
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
		requireActivity().setTitle(R.string.context_add_members);
	}

	@Override
	protected ContextContactSelectorController<SelectableContactItem> getController() {
		return controller;
	}


	@Override
	public String getUniqueTag() {
		return TAG;
	}

}
