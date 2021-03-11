package eu.h2020.helios_social.happ.helios.talk.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;


@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ErrorFragment extends BaseFragment {

	private static final String TAG =
			eu.h2020.helios_social.happ.helios.talk.fragment.ErrorFragment.class
					.getName();

	private static final String ERROR_MSG = "errorMessage";

	public static eu.h2020.helios_social.happ.helios.talk.fragment.ErrorFragment newInstance(
			String message) {
		eu.h2020.helios_social.happ.helios.talk.fragment.ErrorFragment
				f =
				new eu.h2020.helios_social.happ.helios.talk.fragment.ErrorFragment();
		Bundle args = new Bundle();
		args.putString(ERROR_MSG, message);
		f.setArguments(args);
		return f;
	}

	private String errorMessage;

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args == null) throw new AssertionError();
		errorMessage = args.getString(ERROR_MSG);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.fragment_error, container, false);
		TextView msg = v.findViewById(R.id.errorMessage);
		msg.setText(errorMessage);
		return v;
	}

}
