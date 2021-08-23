package eu.h2020.helios_social.happ.helios.talk.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.happ.helios.talk.context.ContextController;
import eu.h2020.helios_social.happ.helios.talk.context.Themes;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static eu.h2020.helios_social.modules.groupcommunications.context.ContextConstants.MAX_CONTEXT_NAME_LENGTH;

public abstract class HeliosContextFragment extends BaseFragment {

    @Inject
    protected volatile ContextualEgoNetwork egoNetwork;
    @Inject
    protected ContactManager contactManager;
    @Inject
    protected GroupManager groupManager;
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
        Objects.requireNonNull(actionBar).setIcon(R.drawable.ic_context_white);
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        String[] currentContext =
                egoNetwork.getCurrentContext().getData().toString()
                        .split("%");
        if (currentContext[0].equals("All")) actionBar.setTitle("\t" + "No Context");
        else {
            // put private name in toolbar title
            try {
                actionBar.setTitle("\t" + contextController.getContextPrivateName(currentContext[1]));
            } catch (DbException e) {
                e.printStackTrace();
            }

        }
        styleBasedOnContext(currentContext[1]);
    }

    protected void styleBasedOnContext(String contextId) {
        try {
            if (contextId.equals("All")) {
                getActivity().setTheme(R.style.HeliosTheme);
                actionBar.setBackgroundDrawable(
                        new ColorDrawable(
                                getActivity()
                                        .getColor(R.color.helios_primary)));
            } else {
                Themes themes = new Themes(getActivity());
                Integer color = contextController.getContextColor(contextId);
                getActivity().setTheme(themes.getTheme(color));
                actionBar.setBackgroundDrawable(
                        new ColorDrawable(color));
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.context_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
/*        MenuItem friendRequests = menu.findItem(R.id.pending_contacts);
        int incomingConnectionRequests = 0;
        try {
            incomingConnectionRequests = contactManager.pendingIncomingConnectionRequests();
        } catch (DbException e) {
            e.printStackTrace();
        }
        int inviteCounter = 0;
        try {
            inviteCounter += contextController.getPendingIncomingContextInvitations();
        } catch (DbException e) {
            e.printStackTrace();
        }
        try {
            inviteCounter += groupManager.pendingIncomingGroupInvitations();
        } catch (DbException e) {
            e.printStackTrace();
        }
        friendRequests.setIcon(buildCounterDrawable(incomingConnectionRequests,
                                                    R.drawable.ic_person_white));
        MenuItem invites = menu.findItem(R.id.pending_contexts);
        invites.setIcon(buildCounterDrawable(inviteCounter, R.drawable.ic_notifications));*/
    }

    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
        String currentContext =
                egoNetwork.getCurrentContext().getData().toString()
                        .split("%")[1];
        if (currentContext.equals("All")) {
            //menu.getItem(2).setEnabled(true);
            //menu.getItem(0).setVisible(false);
            menu.findItem(R.id.action_context_delete).setVisible(false);
            menu.findItem(R.id.action_context_rename).setVisible(false);
            //menu.getItem(3).setEnabled(true);
            //menu.getItem(4).setEnabled(false);

        } else {
            //menu.getItem(0).setVisible(true);
            menu.findItem(R.id.action_context_delete).setVisible(true);
            menu.findItem(R.id.action_context_rename).setVisible(true);
            //menu.getItem(2).setEnabled(true);
            //menu.getItem(3).setEnabled(true);
            //menu.getItem(4).setEnabled(true);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
//            case R.id.action_create_context:
//                Intent i1 =
//                        new Intent(getContext(),
//                                   CreateContextActivity.class);
//                startActivity(i1);
//                return true;
//            case R.id.action_add_contacts_to_context:
//                Intent inviteContactsToContextActivity =
//                        new Intent(getContext(),
//                                   InviteContactsToContextActivity.class);
//                inviteContactsToContextActivity.putExtra(CONTEXT_ID,
//                                                         egoNetwork.getCurrentContext().getData().toString()
//                                                                 .split("%")[1]);
//                startActivity(inviteContactsToContextActivity);
//                return true;
            case R.id.action_context_rename:
                String currentContextId =
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1];
                showRenameContextDialog();
                return true;
            case R.id.action_context_delete:
                String currentContext =
                        egoNetwork.getCurrentContext().getData().toString()
                                .split("%")[1];
                if (currentContext.equals("All")) {
                    Toast.makeText(getActivity(),"You can not leave 'No Context'", Toast.LENGTH_SHORT).show();
                }
                else {
                    showDeleteContextDialog();
                }
                return true;
//            case R.id.pending_contacts:
//                Intent pendingContactListActivity = new Intent(getActivity(),
//                                                               PendingContactListActivity.class);
//                startActivity(pendingContactListActivity);
//                return true;
//            case R.id.pending_contexts:
//                Intent inviteListActivity = new Intent(getActivity(),
//                                                       InvitationListActivity.class);
//                startActivity(inviteListActivity);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private boolean validateName(String name) {
        int length = StringUtils.toUtf8(name).length;
        if (length > MAX_CONTEXT_NAME_LENGTH) {
            return false;
        }
        return length > 1;
    }

    private void showRenameContextDialog() {
        final EditText edittext = new EditText(getContext());
            String contextId = egoNetwork.getCurrentContext().getData().toString()
                .split("%")[1];
        DialogInterface.OnClickListener okListener =
                (dialog, which) -> {
                    renameContext(contextId, edittext.getText().toString());
                    actionBar.setTitle("\t" + edittext.getText().toString());
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),
                R.style.HeliosDialogTheme);
        builder.setTitle(getString(R.string.dialog_title_rename_context));
        builder.setView(edittext);
        builder.setNegativeButton(R.string.cancel,
                null);
        builder.setPositiveButton(R.string.dialog_button_rename_context, okListener);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(validateName(edittext.getText().toString())));
        // Now set the textchange listener for edittext
        edittext.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                // Check if edittext is empty
                // Disable ok button or Something into edit text. Enable the button.
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(validateName(s.toString()));

            }
        });
            dialog.show();

    }
    // rename context in DB
    private void renameContext(String contextId, String newName){
        contextController.setContextPrivateName(contextId,newName, new ResultExceptionHandler<Void, DbException>() {
            @Override
            public void onException(DbException exception) {

            }
            @Override
            public void onResult(Void result) {
            }
        });

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
        contextController.deleteContext(contextId, new ResultExceptionHandler<Void, DbException>() {
            @Override
            public void onException(DbException exception) {
                handleDbException(exception);
                Toast.makeText(
                        getActivity(),
                        "something went wrong",
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onResult(Void v) {
                egoNetwork.setCurrent(egoNetwork.getOrCreateContext("All%All"));
                Intent intent = new Intent(getActivity(), NavDrawerActivity.class);
                Bundle extras = new Bundle();
                extras.putString("message", "context removed successfully");
                intent.putExtras(extras);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

/*    private Drawable buildCounterDrawable(int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this.getActivity());
        View view = inflater.inflate(R.layout.counter_menu_item_layout, null);
        view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);

            if (count > 9) textView.setText("9+");
            else textView.setText("" + count);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);

    }*/
}
