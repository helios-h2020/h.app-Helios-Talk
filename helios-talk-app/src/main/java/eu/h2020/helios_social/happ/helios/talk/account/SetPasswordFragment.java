package eu.h2020.helios_social.happ.helios.talk.account;

import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.login.StrengthMeter;

import javax.annotation.Nullable;

import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static java.util.Objects.requireNonNull;
import static eu.h2020.helios_social.happ.helios.talk.api.crypto.PasswordStrengthEstimator.QUITE_WEAK;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SetPasswordFragment extends SetupFragment {

	private final static String TAG = SetPasswordFragment.class.getName();
	private static final int MAX_AUTHOR_NAME_LENGTH = 50;

	private TextInputLayout passwordEntryWrapper;
	private TextInputLayout passwordConfirmationWrapper;
	private TextInputEditText passwordEntry;
	private TextInputEditText passwordConfirmation;
	private StrengthMeter strengthMeter;
	private Button nextButton;
	private ProgressBar progressBar;
	private TextInputLayout authorNameWrapper;
	private TextInputEditText authorNameInput;

	public static SetPasswordFragment newInstance() {
		return new SetPasswordFragment();
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		requireActivity()
				.setTitle(getString(R.string.setup_password_intro));
		View v =
				inflater.inflate(R.layout.fragment_setup_author_name, container,
						false);

		strengthMeter = v.findViewById(R.id.strength_meter);
		passwordEntryWrapper = v.findViewById(R.id.password_entry_wrapper);
		passwordEntry = v.findViewById(R.id.password_entry);
		passwordConfirmationWrapper =
				v.findViewById(R.id.password_confirm_wrapper);
		passwordConfirmation = v.findViewById(R.id.password_confirm);
		nextButton = v.findViewById(R.id.signup);
		progressBar = v.findViewById(R.id.progress);
		authorNameWrapper = v.findViewById(R.id.nickname_entry_wrapper);
		authorNameInput = v.findViewById(R.id.nickname_entry);
		authorNameInput.addTextChangedListener(this);

		passwordEntry.addTextChangedListener(this);
		passwordConfirmation.addTextChangedListener(this);
		nextButton.setOnClickListener(this);

		if (!setupController.needToShowDozeFragment()) {
			nextButton.setText(R.string.create_account_button);
			passwordConfirmation.setImeOptions(IME_ACTION_DONE);
		}

		return v;
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	@Override
	protected String getHelpText() {
		return getString(R.string.setup_password_explanation);
	}

	@Override
	public void onTextChanged(CharSequence authorName, int i, int i1, int i2) {
		String password1 = passwordEntry.getText().toString();
		String password2 = passwordConfirmation.getText().toString();
		boolean passwordsMatch = password1.equals(password2);

		strengthMeter
				.setVisibility(password1.length() > 0 ? VISIBLE : INVISIBLE);
		float strength = setupController.estimatePasswordStrength(password1);
		strengthMeter.setStrength(strength);
		boolean strongEnough = strength >= QUITE_WEAK;

		UiUtils.setError(passwordEntryWrapper, getString(R.string.password_too_weak),
				password1.length() > 0 && !strongEnough);
		UiUtils.setError(passwordConfirmationWrapper,
				getString(R.string.passwords_do_not_match),
				password2.length() > 0 && !passwordsMatch);

		boolean nameError = authorNameInput.getText().toString().length() >
				MAX_AUTHOR_NAME_LENGTH;
		UiUtils.setError(authorNameWrapper, getString(R.string.name_too_long),
				nameError);

		boolean enabled = passwordsMatch && strongEnough && !nameError;
		nextButton.setEnabled(enabled);
		passwordConfirmation.setOnEditorActionListener(enabled ? this : null);
		authorNameInput.setOnEditorActionListener(enabled ? this : null);
	}

	@Override
	public void onClick(View view) {
		setupController
				.setAuthorName(authorNameInput.getText().toString().trim());
		IBinder token = passwordEntry.getWindowToken();
		Object o = getContext().getSystemService(INPUT_METHOD_SERVICE);
		((InputMethodManager) o).hideSoftInputFromWindow(token, 0);
		setupController.setPassword(passwordEntry.getText().toString());
		if (setupController.needToShowDozeFragment()) {
			setupController.showDozeFragment();
		} else {
			nextButton.setVisibility(INVISIBLE);
			progressBar.setVisibility(VISIBLE);
			setupController.createAccount();
		}
	}

}
