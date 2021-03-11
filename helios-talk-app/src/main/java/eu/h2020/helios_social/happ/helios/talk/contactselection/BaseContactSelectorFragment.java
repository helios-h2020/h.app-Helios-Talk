package eu.h2020.helios_social.happ.helios.talk.contactselection;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.LinearLayoutManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contact.BaseContactListAdapter;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactItemViewHolder;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

import static eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorActivity.CONTACTS;
import static eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorActivity.getContactsFromIds;
import static eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorActivity.getContactsFromStrings;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.GROUP_ID;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public abstract class BaseContactSelectorFragment<I extends SelectableContactItem, A extends BaseContactSelectorAdapter<I, ? extends ContactItemViewHolder<I>>>
		extends BaseFragment
		implements BaseContactListAdapter.OnContactClickListener<I> {

	protected HeliosTalkRecyclerView list;
	protected A adapter;
	protected Collection<ContactId> selectedContacts = new ArrayList<>();
	protected ContactSelectorListener listener;

	private String groupId;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (ContactSelectorListener) context;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = requireArguments();
		groupId = args.getString(GROUP_ID);
		if (groupId == null) throw new IllegalStateException("No GroupId");
	}

	@Override
	@CallSuper
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View contentView = inflater.inflate(R.layout.list, container, false);

		list = contentView.findViewById(R.id.list);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.setEmptyImage(R.drawable.ic_empty_state_contact_list);
		list.setEmptyText(getString(R.string.no_contacts_selector));
		list.setEmptyAction(getString(R.string.no_contacts_selector_action));
		adapter = getAdapter(requireContext(), this);
		list.setAdapter(adapter);

		// restore selected contacts if available
		if (savedInstanceState != null) {
			ArrayList<String> stringContacts =
					savedInstanceState.getStringArrayList(CONTACTS);
			if (stringContacts != null) {
				selectedContacts = getContactsFromStrings(stringContacts);
			}
		}
		return contentView;
	}

	protected abstract A getAdapter(Context context,
			BaseContactListAdapter.OnContactClickListener<I> listener);

	@Override
	public void onStart() {
		super.onStart();
		loadContacts(selectedContacts);
	}

	@Override
	public void onStop() {
		super.onStop();
		adapter.clear();
		list.showProgressBar();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (adapter != null) {
			selectedContacts = adapter.getSelectedContactIds();
			outState.putStringArrayList(CONTACTS,
					getContactsFromIds(selectedContacts));
		}
	}

	@Override
	public void onItemClick(View view, I item) {
		item.toggleSelected();
		adapter.notifyItemChanged(adapter.findItemPosition(item), item);
		onSelectionChanged();
	}

	private void loadContacts(Collection<ContactId> selection) {
		getController().loadContacts(groupId, selection,
				new UiResultExceptionHandler<Collection<I>, DbException>(
						this) {
					@Override
					public void onResultUi(Collection<I> contacts) {
						if (contacts.isEmpty()) list.showData();
						else adapter.addAll(contacts);
						onSelectionChanged();
					}

					@Override
					public void onExceptionUi(DbException exception) {
						handleDbException(exception);
					}
				});
	}

	protected abstract void onSelectionChanged();

	protected abstract ContactSelectorController<I> getController();

}
