package eu.h2020.helios_social.happ.helios.talk.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
//import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;

import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.COMPACTING;
import static eu.h2020.helios_social.happ.helios.talk.login.StartupViewModel.State.MIGRATING;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class OpenDatabaseFragment extends BaseFragment {

    final static String TAG = OpenDatabaseFragment.class.getName();

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	/*@Inject
	ContextualEgoNetwork egoNetwork;*/

    private TextView textView;
    private ImageView imageView;

    @Override
    public void injectFragment(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_open_database, container,
                false);
        textView = v.findViewById(R.id.textView);
        imageView = v.findViewById(R.id.imageView);

        StartupViewModel viewModel = ViewModelProviders.of(requireActivity(),
                viewModelFactory).get(StartupViewModel.class);

        viewModel.getState().observe(getViewLifecycleOwner(), this::onStateChanged);

        return v;
    }

    private void onStateChanged(State state) {
        if (state == MIGRATING) showMigration();
        else if (state == COMPACTING) showCompaction();
    }

    private void showMigration() {
        textView.setText(R.string.startup_migrate_database);
        imageView.setImageResource(R.drawable.ic_helios_social_network);
    }

    private void showCompaction() {
        textView.setText(R.string.startup_compact_database);
        imageView.setImageResource(R.drawable.ic_helios_social_network);
    }

    @Override
    public String getUniqueTag() {
        return TAG;
    }

}
