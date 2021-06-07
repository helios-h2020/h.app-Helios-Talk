package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import java.util.logging.Logger;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment.BaseFragmentListener;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.EXTRA_TEXT;
import static android.widget.Toast.LENGTH_LONG;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class AddContactActivity extends HeliosTalkActivity implements
        BaseFragmentListener {
    private static Logger LOG = Logger.getLogger(AddContactActivity.class.getName());

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private AddContactViewModel viewModel;

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_fragment_container);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(AddContactViewModel.class);
        viewModel.onCreate();
        viewModel.getRemoteHeliosLinkEntered().observeEvent(this, entered -> {
            if (entered) {
                NicknameFragment f = new NicknameFragment();
                showNextFragment(f);
            }
        });

        Intent i = getIntent();
        if (state == null) {
            // do not react to the intent again when recreating the activity
            onNewIntent(i);
        }

        if (state == null) {
            showInitialFragment(new LinkExchangeFragment());
        }
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        String action = i.getAction();
        if (ACTION_SEND.equals(action) || ACTION_VIEW.equals(action)) {
            String text = i.getStringExtra(EXTRA_TEXT);
            String uri = "helios://" + i.getDataString().replace("https://helios-social.com/", "");
            if (text != null) handleIncomingLink(text);
            else if (uri != null) handleIncomingLink(uri);
        }
    }

    private void handleIncomingLink(String link) {
        if (link.equals(viewModel.getHeliosLink().getValue())) {
            Toast.makeText(this, R.string.intent_own_link, LENGTH_LONG)
                    .show();
        } else if (viewModel.isValidHeliosLink(link)) {
            viewModel.setRemoteHeliosLink(link);
        } else {
            Toast.makeText(this, R.string.invalid_link, LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
