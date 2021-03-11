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

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class ContextContactSelectorActivity extends HeliosTalkActivity
		implements BaseFragment.BaseFragmentListener, ContactSelectorListener {
	protected final static String CONTACTS = "contacts";
	public static final String CONTEXT_ID = "contextId";

	// Subclasses may initialise the group ID in different places
	protected String contextId;
	protected Collection<ContactId> contacts;

	@Override
	public void onCreate(@Nullable Bundle bundle) {
		super.onCreate(bundle);

		setContentView(getLayout());

		if (bundle != null) {
			// restore context ID if it was saved
			if (bundle.getString(CONTEXT_ID) != null)
				contextId = bundle.getString(CONTEXT_ID);
			// restore selected contacts if a selection was saved
			ArrayList<String> stringContacts =
					bundle.getStringArrayList(CONTACTS);
			if (stringContacts != null) {
				contacts = getContactsFromStrings(stringContacts);
			}
		}
	}

	@CallSuper
	@UiThread
	@Override
	public void contactsSelected(Collection<ContactId> contacts) {
		this.contacts = contacts;
	}

	@LayoutRes
	protected int getLayout() {
		return R.layout.activity_fragment_container;
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
