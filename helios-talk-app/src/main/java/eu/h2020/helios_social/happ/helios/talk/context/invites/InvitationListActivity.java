package eu.h2020.helios_social.happ.helios.talk.context.invites;

import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.ContextInvitation;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitation;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.widget.Toast.LENGTH_LONG;


@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class InvitationListActivity extends HeliosTalkActivity
		implements InvitationListener {

	@Inject
	ViewModelProvider.Factory viewModelFactory;
	@Inject
	ContextualEgoNetwork egoNetwork;

	private InvitationListViewModel viewModel;
	private InvitationListAdapter adapter;
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
				.get(InvitationListViewModel.class);
		viewModel.onCreate();
		viewModel.getPendingInvitations()
				.observe(this, this::onContextInvitationsChanged);

		adapter = new InvitationListAdapter(this, this,
				InvitationItem.class);
		list = findViewById(R.id.list);
		list.setEmptyText(R.string.no_pending_invitations);
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
	public void onRejectGroup(InvitationItem item) {
		OnClickListener removeListener = (dialog, which) ->
				removePendingGroupInvitation(
						(GroupInvitation) item.getInvitation());
		AlertDialog.Builder builder = new AlertDialog.Builder(
				InvitationListActivity.this, R.style.HeliosDialogTheme);
		builder.setTitle(
				getString(R.string.dialog_title_remove_pending_group_invite));
		builder.setMessage(
				getString(
						R.string.dialog_message_remove_pending_group_invite));
		builder.setNegativeButton(R.string.delete_group_invite,
				removeListener);
		builder.setPositiveButton(R.string.cancel, null);
		// re-enable remove button when dialog is dismissed/canceled
		builder.setOnDismissListener(dialog -> adapter.notifyDataSetChanged());
		builder.show();
	}

	@Override
	public void onJoinGroup(InvitationItem item) {
		viewModel.joinPendingGroup((GroupInvitation) item.getInvitation());
		Toast.makeText(InvitationListActivity.this,
				R.string.join_context_toast,
				LENGTH_LONG)
				.show();
		egoNetwork.setCurrent(
				egoNetwork.getOrCreateContext(item.getContextName() + "%" +
						item.getInvitation().getContextId()));


		Intent intent = new Intent(this, NavDrawerActivity.class);
		intent.setFlags(
				FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public void onRejectContext(
			InvitationItem item) {
		// show warning dialog
		OnClickListener removeListener = (dialog, which) ->
				removePendingContextInvitation(
						(ContextInvitation) item.getInvitation());
		AlertDialog.Builder builder = new AlertDialog.Builder(
				InvitationListActivity.this, R.style.HeliosDialogTheme);
		builder.setTitle(
				getString(R.string.dialog_title_remove_pending_context_invite));
		builder.setMessage(
				getString(
						R.string.dialog_message_remove_pending_context_invite));
		builder.setNegativeButton(R.string.delete_context_invite,
				removeListener);
		builder.setPositiveButton(R.string.cancel, null);
		// re-enable remove button when dialog is dismissed/canceled
		builder.setOnDismissListener(dialog -> adapter.notifyDataSetChanged());
		builder.show();
	}

	@Override
	public void onJoinContext(
			InvitationItem item) {
		viewModel.joinPendingContext((ContextInvitation) item.getInvitation());
		Toast.makeText(InvitationListActivity.this,
				R.string.join_context_toast,
				LENGTH_LONG)
				.show();
		Intent intent = new Intent(this, NavDrawerActivity.class);
		intent.setFlags(
				FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	private void removePendingContextInvitation(
			ContextInvitation contextInvite) {
		viewModel.rejectPendingContextInvitation(contextInvite);
	}

	private void removePendingGroupInvitation(
			GroupInvitation groupInvitation) {
		viewModel.rejectPendingGroupInvitation(groupInvitation);
	}

	private void onContextInvitationsChanged(
			Collection<InvitationItem> items) {
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
