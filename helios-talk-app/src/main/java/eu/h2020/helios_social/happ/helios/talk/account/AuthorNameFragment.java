package eu.h2020.helios_social.happ.helios.talk.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AuthorNameFragment extends BaseFragment implements
		View.OnClickListener {

	private final static String TAG = AuthorNameFragment.class.getName();

	@Inject
	SetupController setupController;
	private Button nextButton;

	public static AuthorNameFragment newInstance() {
		return new AuthorNameFragment();
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		requireActivity().setTitle(getString(R.string.setup_title));
		View v = inflater.inflate(R.layout.fragment_startup,
				container, false);
		nextButton = v.findViewById(R.id.signup);
		nextButton.setOnClickListener(this);

		return v;
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	@Override
	public void onClick(View view) {
		setupController.showPasswordFragment();
	}
}
