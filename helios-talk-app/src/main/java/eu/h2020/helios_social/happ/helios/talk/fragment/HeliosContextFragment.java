package eu.h2020.helios_social.happ.helios.talk.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.context.invites.InvitationListActivity;
import eu.h2020.helios_social.happ.helios.talk.context.sharing.InviteContactsToContextActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.PendingContactListActivity;
import eu.h2020.helios_social.happ.helios.talk.context.ContextController;
import eu.h2020.helios_social.happ.helios.talk.context.CreateContextActivity;
import eu.h2020.helios_social.happ.helios.talk.context.Themes;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.widget.Toast.LENGTH_LONG;
import static eu.h2020.helios_social.happ.helios.talk.contactselection.ContextContactSelectorActivity.CONTEXT_ID;

public abstract class HeliosContextFragment extends BaseFragment {

    @Inject
    protected ContextualEgoNetwork egoNetwork;
    @Inject
    protected ContextController contextController;

    protected ActionBar actionBar;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        actionBar =
                ((AppCompatActivity) requireActivity())
                        .getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_context_white);
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        String[] currentContext =
                egoNetwork.getCurrentContext().getData().toString()
                        .split("%");
        actionBar.setTitle(
                "\t" + currentContext[0]);
        styleBasedOnContext(currentContext[1]);
    }

    private void styleBasedOnContext(String contextId) {
        try {
            if (contextId.equals("All")) {
                int defaultColor = getActivity().getResources()
                        .getColor(R.color.helios_default_context_color);
                getActivity().setTheme(R.style.HeliosTheme);
                actionBar.setBackgroundDrawable(
                        new ColorDrawable(
                                getActivity()
                                        .getColor(R.color.helios_primary)));
            } else {
                Themes themes = new Themes(getActivity());
                Integer color = contextController.getContextColor(contextId);
                getActivity()
                        .setTheme(themes.getTheme(color));
                actionBar.setBackgroundDrawable(
                        new ColorDrawable(color));
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.context_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        String currentContext =
                egoNetwork.getCurrentContext().getData().toString()
                        .split("%")[1];
        if (currentContext.equals("All")) {
            menu.getItem(2).setEnabled(false);
            menu.getItem(3).setEnabled(true);
            menu.getItem(4).setEnabled(false);

        } else {
            menu.getItem(2).setEnabled(true);
            menu.getItem(3).setEnabled(true);
            menu.getItem(4).setEnabled(true);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_create_context:
                Intent i1 =
                        new Intent(getContext(),
                                CreateContextActivity.class);
                startActivity(i1);
                return true;
            case R.id.action_add_contacts_to_context:
                Intent inviteContactsToContextActivity =
                        new Intent(getContext(),
                                InviteContactsToContextActivity.class);
                inviteContactsToContextActivity.putExtra(CONTEXT_ID,
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1]);
                startActivity(inviteContactsToContextActivity);
                return true;
            case R.id.action_context_delete:
                showDeleteContextDialog();
                return true;
            case R.id.pending_contacts:
                Intent pendingContactListActivity = new Intent(getActivity(),
                        PendingContactListActivity.class);
                startActivity(pendingContactListActivity);
                return true;
            case R.id.pending_contexts:
                Intent inviteListActivity = new Intent(getActivity(),
                        InvitationListActivity.class);
                startActivity(inviteListActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteContextDialog() {
        String contextId = egoNetwork.getCurrentContext().getData().toString()
                .split("%")[1];
        DialogInterface.OnClickListener okListener =
                (dialog, which) -> deleteContext(contextId);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),
                R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.dialog_title_leave_context));
        builder.setMessage(getString(R.string.dialog_message_leave_context));
        builder.setNegativeButton(R.string.dialog_button_remove_context,
                okListener);
        builder.setPositiveButton(R.string.cancel, null);
        builder.show();
    }

    private void deleteContext(String contextId) {
		/*contextController.deleteContext(contextId,
				new UiResultExceptionHandler<String, DbException>(
						this) {
					@Override
					public void onResultUi(String name) {
					}

					@Override
					public void onExceptionUi(DbException exception) {
						handleDbException(exception);
					}
				});
		egoNetwork.removeContext(
				egoNetwork.getOrCreateContext(contextId));
		Toast.makeText(getContext(),
				R.string.context_removed_toast,
				LENGTH_LONG)
				.show();
		egoNetwork.setCurrent(egoNetwork.getOrCreateContext("All@All"));
		Intent intent = new Intent(getActivity(), NavDrawerActivity.class);
		intent.setFlags(
				FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);*/

    }
}
