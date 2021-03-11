package eu.h2020.helios_social.happ.helios.talk.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import eu.h2020.helios_social.modules.groupcommunications_utils.crypto.DecryptionResult;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static eu.h2020.helios_social.modules.groupcommunications_utils.crypto.DecryptionResult.KEY_STRENGTHENER_ERROR;
import static eu.h2020.helios_social.modules.groupcommunications_utils.crypto.DecryptionResult.SUCCESS;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class PasswordFragment extends BaseFragment implements TextWatcher {

	final static String TAG = PasswordFragment.class.getName();

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private StartupViewModel viewModel;
	private Button signInButton;
	private ProgressBar progress;
	private TextInputLayout input;
	private TextInputEditText password;

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_password, container,
				false);

		viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory)
				.get(StartupViewModel.class);

		viewModel.getPasswordValidated().observeEvent(this, result -> {
			if (result != SUCCESS) onPasswordInvalid(result);
		});

		signInButton = v.findViewById(R.id.btn_sign_in);
		signInButton.setOnClickListener(view -> onSignInButtonClicked());
		progress = v.findViewById(R.id.progress_wheel);
		input = v.findViewById(R.id.password_layout);
		password = v.findViewById(R.id.edit_password);
		password.setOnEditorActionListener((view, actionId, event) -> {
			if (actionId == IME_ACTION_DONE || UiUtils
					.enterPressed(actionId, event)) {
				onSignInButtonClicked();
				return true;
			}
			return false;
		});
		password.addTextChangedListener(this);
		v.findViewById(R.id.btn_forgotten)
				.setOnClickListener(view -> onForgottenPasswordClick());

		return v;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before,
			int count) {
		if (count > 0) UiUtils.setError(input, null, false);
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	private void onSignInButtonClicked() {
		UiUtils.hideSoftKeyboard(password);
		signInButton.setVisibility(INVISIBLE);
		progress.setVisibility(VISIBLE);
		viewModel.validatePassword(password.getText().toString());
	}

	private void onPasswordInvalid(DecryptionResult result) {
		signInButton.setVisibility(VISIBLE);
		progress.setVisibility(INVISIBLE);
		if (result == KEY_STRENGTHENER_ERROR) {
			LoginUtils.createKeyStrengthenerErrorDialog(requireContext()).show();
		} else {
			UiUtils.setError(input, getString(R.string.try_again), true);
			password.setText(null);
			// show the keyboard again
			UiUtils.showSoftKeyboard(password);
		}
	}

	private void onForgottenPasswordClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),
				R.style.HeliosDialogTheme);
		builder.setTitle(R.string.dialog_title_lost_password);
		builder.setMessage(R.string.dialog_message_lost_password);
		builder.setPositiveButton(R.string.cancel, null);
		builder.setNegativeButton(R.string.delete,
				(dialog, which) -> viewModel.deleteAccount());
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

}
