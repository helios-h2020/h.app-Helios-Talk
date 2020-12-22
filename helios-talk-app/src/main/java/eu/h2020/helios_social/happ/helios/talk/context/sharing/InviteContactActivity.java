package eu.h2020.helios_social.happ.helios.talk.context.sharing;

import android.content.Intent;
import android.os.Bundle;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContextContactSelectorActivity;
import eu.h2020.helios_social.happ.helios.talk.context.ContextController;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

public abstract class InviteContactActivity extends
		ContextContactSelectorActivity {
	private static final Logger LOG =
			Logger.getLogger(InviteContactActivity.class.getName());

	@Inject
	ContextualEgoNetwork egoNetwork;
	@Inject
	ContextController controller;

	@Override
	public void onCreate(@Nullable Bundle bundle) {
		super.onCreate(bundle);

		Intent i = getIntent();
		String c = i.getStringExtra(CONTEXT_ID);
		if (c == null) c = egoNetwork.getCurrentContext().getData().toString()
				.split("%")[1];
		this.contextId = c;
	}

	@UiThread
	@Override
	public void contactsSelected(Collection<ContactId> contacts) {
		super.contactsSelected(contacts);
		invite(contacts);
	}

	@Override
	public void onBackPressed() {
		int count = getSupportFragmentManager().getBackStackEntryCount();
		if (count == 0) {
			super.onBackPressed();
			//additional code
		} else {
			getSupportFragmentManager().popBackStack();
		}
	}

	abstract void invite(Collection<ContactId> contacts);

}
