package eu.h2020.helios_social.happ.helios.talk.contactselection;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contact.BaseContactListAdapter;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class ContextContactSelectorFragment extends
		BaseContextContactSelectorFragment<SelectableContactItem, ContextContactSelectorAdapter>
		implements
		BaseContactListAdapter.OnContactClickListener<SelectableContactItem> {

	public static final String TAG = ContactSelectorFragment.class.getName();

	private Menu menu;

	@Override
	protected ContextContactSelectorAdapter getAdapter(Context context,
			BaseContactListAdapter.OnContactClickListener<SelectableContactItem> listener) {
		return new ContextContactSelectorAdapter(context, listener);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.contact_selection_actions, menu);
		super.onCreateOptionsMenu(menu, inflater);
		this.menu = menu;
		// hide sharing action initially, if no contact is selected
		onSelectionChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_contacts_selected:
				selectedContacts = adapter.getSelectedContactIds();
				listener.contactsSelected(selectedContacts);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSelectionChanged() {
		if (menu == null) return;
		MenuItem item = menu.findItem(R.id.action_contacts_selected);
		if (item == null) return;

		selectedContacts = adapter.getSelectedContactIds();
		if (selectedContacts.size() > 0) {
			item.setVisible(true);
		} else {
			item.setVisible(false);
		}
	}

}
