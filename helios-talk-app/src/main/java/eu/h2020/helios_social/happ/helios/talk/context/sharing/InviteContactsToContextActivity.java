package eu.h2020.helios_social.happ.helios.talk.context.sharing;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils;

import static eu.h2020.helios_social.modules.groupcommunications.context.ContextConstants.MAX_CONTEXT_NAME_LENGTH;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class InviteContactsToContextActivity
		extends InviteContactActivity {

	private static final Logger LOG =
			Logger.getLogger(InviteContactsToContextActivity.class.getName());

	private Collection<ContactId> contactsToInvite;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle bundle) {
		super.onCreate(bundle);

		if (bundle == null) {
			LOG.info("Current Context: " + contextId);
			showInitialFragment(InvitingContactsFragment.newInstance(
					contextId, contextName
			));
		}
	}

	@Override
	void invite(Collection<ContactId> contacts) {
		String name="";
		// if there is not a public name, force the user to set one.
		try {
			name = controller.getContextName(contextId);
		} catch (DbException e) {
			e.printStackTrace();
		}
		if (name.isEmpty()){
			try {
				contactsToInvite = contacts;
				showRenameContextDialog();
			} catch (DbException e) {
				e.printStackTrace();
			}
		}
		else
			proceedToInvite(contacts);
	}
	private void proceedToInvite(Collection<ContactId> contacts){
		controller.invite(contextId,
				contacts,
				new UiExceptionHandler<DbException>(this) {
					@Override
					public void onExceptionUi(DbException exception) {
						handleDbException(exception);
					}
				});
		onBackPressed();
	}

	// check if name is valid
	private boolean validateName(String name) {
		int length = StringUtils.toUtf8(name).length;
		if (length > MAX_CONTEXT_NAME_LENGTH) {
			return false;
		}
		return length > 1;
	}

	// show the dialog to set a public name
	private void showRenameContextDialog() throws DbException {
		final EditText edittext = new EditText(this);

		edittext.setText(controller.getContextPrivateName(contextId));

		DialogInterface.OnClickListener okListener =
				(dialog, which) -> {
					try {
						setPublicName(contextId, edittext.getText().toString());
						proceedToInvite(contactsToInvite);
					} catch (DbException e) {
						e.printStackTrace();
					}
				};
		AlertDialog.Builder builder = new AlertDialog.Builder(this,
				R.style.HeliosDialogTheme);
		builder.setTitle(getString(R.string.dialog_title_set_context_public_name));
		builder.setView(edittext);
		builder.setNegativeButton(R.string.cancel,
				null);
		builder.setPositiveButton(R.string.ok, okListener);
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

	// set the public name in DB
	private void setPublicName(String contextId, String newName) throws DbException {
		controller.setContextName(contextId,newName, new ResultExceptionHandler<Void, DbException>() {
			@Override
			public void onException(DbException exception) {

			}

			@Override
			public void onResult(Void result) {
			}
		});

	}


}
