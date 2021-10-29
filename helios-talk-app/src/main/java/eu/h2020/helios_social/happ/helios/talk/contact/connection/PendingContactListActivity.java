package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContact;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import io.github.kobakei.materialfabspeeddial.FabSpeedDial;


@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class PendingContactListActivity extends HeliosTalkActivity
        implements PendingContactListener, FabSpeedDial.OnMenuItemClickListener {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    AndroidNotificationManager notificationManager;

    private PendingContactListViewModel viewModel;
    private PendingContactListAdapter adapter;
    private HeliosTalkRecyclerView list;

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_pending_condact_list);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(PendingContactListViewModel.class);
        viewModel.onCreate();
        viewModel.getPendingContacts()
                .observe(this, this::onPendingContactsChanged);

        adapter = new PendingContactListAdapter(this, this,
                PendingContactItem.class);
        list = findViewById(R.id.list);
        list.setEmptyTitle(R.string.no_pending_connections);
        list.setEmptyText("");
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        list.showProgressBar();


        FabSpeedDial speedDial = findViewById(R.id.speedDial);
        speedDial.inflateMenu(R.menu.contact_list_actions);
        speedDial.addOnMenuItemClickListener(this);

        // replace speedDial with a button, there is no need of menu when we have only one option.
/*        FloatingActionButton imageButton = findViewById(R.id.addImageBtn);
        Intent intent = new Intent(PendingContactListActivity.this, AddContactActivity.class);
        intent.putExtra("isOutGoing",true);

        imageButton.setOnClickListener(v -> startActivity(intent));*/

    }

    @Override
    public void onMenuItemClick(FloatingActionButton fab, @Nullable TextView v,
                                int itemId) {
        Intent intent = new Intent(PendingContactListActivity.this, AddContactActivity.class);
        switch (itemId) {
            case R.id.action_add_contact_remotely:
                intent.putExtra("isOutGoing",true);
                startActivity(intent);
                return;
            case R.id.action_my_helios_link:
                intent.putExtra("isOutGoing",false);
                startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        notificationManager.clearConnectionRequestsNotification();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPendingContactItemDelete(PendingContactItem item) {
        // show warning dialog
        OnClickListener removeListener = (dialog, which) ->
                removePendingContact(item.getPendingContact());
        AlertDialog.Builder builder = new AlertDialog.Builder(
                PendingContactListActivity.this, R.style.HeliosDialogTheme);
        builder.setTitle(
                getString(R.string.dialog_title_remove_pending_contact));
        builder.setMessage(
                getString(R.string.dialog_message_remove_pending_contact));
        builder.setNegativeButton(R.string.delete, removeListener);
        builder.setPositiveButton(R.string.cancel, null);
        // re-enable remove button when dialog is dismissed/canceled
        builder.setOnDismissListener(dialog -> adapter.notifyDataSetChanged());
        builder.show();
    }

    @Override
    public void onPendingContactItemConfirm(PendingContactItem item) {
        viewModel.addPendingContact(item.getPendingContact());
    }

    private void removePendingContact(PendingContact pendingContact) {
        viewModel.removePendingContact(pendingContact);
    }

    private void onPendingContactsChanged(
            Collection<PendingContactItem> items) {
        if (items.isEmpty()) {
            if (adapter.isEmpty()) {
                list.showData();  // hides progress bar, shows empty text
            } else {
                // all previous contacts have been removed, so we can finish
                supportFinishAfterTransition();
            }
        } else {
            adapter.setItems(items);
        }
    }

}
