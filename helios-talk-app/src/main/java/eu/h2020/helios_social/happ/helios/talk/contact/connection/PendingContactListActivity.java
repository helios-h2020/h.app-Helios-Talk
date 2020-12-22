package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkSnackbarBuilder;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContact;


@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class PendingContactListActivity extends HeliosTalkActivity
		implements PendingContactListener {

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private PendingContactListViewModel viewModel;
	private PendingContactListAdapter adapter;
	private HeliosTalkRecyclerView list;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.list);

		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayHomeAsUpEnabled(true);
		}

		viewModel = ViewModelProviders.of(this, viewModelFactory)
				.get(PendingContactListViewModel.class);
		viewModel.onCreate();
		viewModel.getPendingContacts()
				.observe(this, this::onPendingContactsChanged);

		adapter = new PendingContactListAdapter(this, this,
				PendingContactItem.class);
		list = findViewById(R.id.list);
		list.setEmptyText(R.string.no_pending_connections);
		list.setLayoutManager(new LinearLayoutManager(this));
		list.setAdapter(adapter);
		list.showProgressBar();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPendingContactItemDelete(PendingContactItem item) {
		// show warning dialog
		OnClickListener removeListener = (dialog, which) ->
				removePendingContact(item.getPendingContact());
		AlertDialog.Builder builder = new AlertDialog.Builder(
				PendingContactListActivity.this, R.style.HeliosDialogTheme);
		builder.setTitle(
				getString(R.string.dialog_title_remove_pending_contact));
		builder.setMessage(
				getString(R.string.dialog_message_remove_pending_contact));
		builder.setNegativeButton(R.string.delete, removeListener);
		builder.setPositiveButton(R.string.cancel, null);
		// re-enable remove button when dialog is dismissed/canceled
		builder.setOnDismissListener(dialog -> adapter.notifyDataSetChanged());
		builder.show();
	}

	@Override
	public void onPendingContactItemConfirm(PendingContactItem item) {
		viewModel.addPendingContact(item.getPendingContact());
	}

	private void removePendingContact(PendingContact pendingContact) {
		viewModel.removePendingContact(pendingContact);
	}

	private void onPendingContactsChanged(
			Collection<PendingContactItem> items) {
		if (items.isEmpty()) {
			if (adapter.isEmpty()) {
				list.showData();  // hides progress bar, shows empty text
			} else {
				// all previous contacts have been removed, so we can finish
				supportFinishAfterTransition();
			}
		} else {
			adapter.setItems(items);
		}
	}

}
