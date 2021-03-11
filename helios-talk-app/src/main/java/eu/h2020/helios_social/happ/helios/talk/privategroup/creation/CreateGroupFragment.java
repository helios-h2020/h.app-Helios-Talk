package eu.h2020.helios_social.happ.helios.talk.privategroup.creation;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class CreateGroupFragment extends BaseFragment {

	public final static String TAG = CreateGroupFragment.class.getName();
	private static final int MAX_GROUP_NAME_LENGTH = 75;

	private CreateGroupListener listener;
	private EditText nameEntry;
	private Button createGroupButton;
	private TextInputLayout nameLayout;
	private ProgressBar progress;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (CreateGroupListener) context;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_create_group, container,
				false);
		nameEntry = v.findViewById(R.id.name);
		nameEntry.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int lengthBefore, int lengthAfter) {
				enableOrDisableCreateButton();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		nameEntry.setOnEditorActionListener((v1, actionId, e) -> {
			if (actionId == IME_ACTION_DONE || UiUtils.enterPressed(actionId, e)) {
				createGroup();
				return true;
			}
			return false;
		});

		nameLayout = v.findViewById(R.id.nameLayout);

		createGroupButton = v.findViewById(R.id.button);
		createGroupButton.setOnClickListener(v1 -> createGroup());

		progress = v.findViewById(R.id.progressBar);

		return v;
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	private void enableOrDisableCreateButton() {
		if (createGroupButton == null) return; // Not created yet
		createGroupButton.setEnabled(validateName());
	}

	private boolean validateName() {
		String name = nameEntry.getText().toString();
		int length = StringUtils.toUtf8(name).length;
		if (length > MAX_GROUP_NAME_LENGTH) {
			nameLayout.setError(getString(R.string.name_too_long));
			return false;
		}
		nameLayout.setError(null);
		return length > 0;
	}

	private void createGroup() {
		if (!validateName()) return;
		UiUtils.hideSoftKeyboard(nameEntry);
		createGroupButton.setVisibility(GONE);
		progress.setVisibility(VISIBLE);
		listener.onGroupNameChosen(nameEntry.getText().toString());
	}
}
