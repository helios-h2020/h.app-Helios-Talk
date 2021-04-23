package eu.h2020.helios_social.happ.helios.talk.account.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.account.SetupController;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class OnBoardingFragmentStepFive extends BaseFragment implements
        View.OnClickListener {

    private final static String TAG = OnBoardingFragmentStepFive.class.getName();

    @Inject
    SetupController setupController;
    private TextView nextButton;

    public static OnBoardingFragmentStepFive newInstance() {
        return new OnBoardingFragmentStepFive();
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
        View v = inflater.inflate(R.layout.fragment_onboarding_step_5,
                container, false);
        nextButton = v.findViewById(R.id.next);
        nextButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        setupController.showAuthorNameFragment();
    }

    @Override
    public String getUniqueTag() {
        return TAG;
    }
}