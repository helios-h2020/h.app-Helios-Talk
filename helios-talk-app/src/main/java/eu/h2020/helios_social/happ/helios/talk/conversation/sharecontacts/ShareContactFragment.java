package eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts;

import android.os.Bundle;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorController;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorFragment;
import eu.h2020.helios_social.happ.helios.talk.contactselection.SelectableContactItem;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.CreateForumController;

public class ShareContactFragment extends ContactSelectorFragment {

    public static final String TAG = ShareContactFragment.class.getName();

    @Inject
    CreateForumController controller;

    public static ShareContactFragment newInstance() {
        return new ShareContactFragment();
    }

    @Override
    public void injectFragment(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setTitle(R.string.share_contact);
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
