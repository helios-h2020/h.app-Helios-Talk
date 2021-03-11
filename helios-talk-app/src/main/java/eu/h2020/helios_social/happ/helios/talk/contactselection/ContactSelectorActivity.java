package eu.h2020.helios_social.happ.helios.talk.contactselection;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class ContactSelectorActivity
		extends HeliosTalkActivity
		implements BaseFragment.BaseFragmentListener, ContactSelectorListener {

	protected final static String CONTACTS = "contacts";

	// Subclasses may initialise the group ID in different places
	protected String groupId;
	protected Collection<ContactId> contacts;

	@Override
	public void onCreate(@Nullable Bundle bundle) {
		super.onCreate(bundle);

		setContentView(getLayout());

		if (bundle != null) {
			// restore group ID if it was saved
			if (bundle.getString(GROUP_ID) != null)
				groupId = bundle.getString(GROUP_ID);
			// restore selected contacts if a selection was saved
			ArrayList<String> stringContacts =
					bundle.getStringArrayList(CONTACTS);
			if (stringContacts != null) {
				contacts = getContactsFromStrings(stringContacts);
			}
		}
	}

	@LayoutRes
	protected int getLayout() {
		return R.layout.activity_fragment_container;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (groupId != null) {
			// save the group ID here regardless of how subclasses initialize it
			outState.putString(GROUP_ID, groupId);
		}
		if (contacts != null) {
			outState.putStringArrayList(CONTACTS,
					getContactsFromIds(contacts));
		}
	}

	@CallSuper
	@UiThread
	@Override
	public void contactsSelected(Collection<ContactId> contacts) {
		this.contacts = contacts;
	}

	static ArrayList<String> getContactsFromIds(
			Collection<ContactId> contacts) {
		// transform ContactIds to Integers so they can be added to a bundle
		ArrayList<String> stringContacts = new ArrayList<>(contacts.size());
		for (ContactId contactId : contacts) {
			stringContacts.add(contactId.getId());
		}
		return stringContacts;
	}

	static Collection<ContactId> getContactsFromStrings(
			ArrayList<String> stringContacts) {
		// turn contact integers from a bundle back to ContactIds
		List<ContactId> contacts = new ArrayList<>(stringContacts.size());
		for (String c : stringContacts) {
			contacts.add(new ContactId(c));
		}
		return contacts;
	}


}
