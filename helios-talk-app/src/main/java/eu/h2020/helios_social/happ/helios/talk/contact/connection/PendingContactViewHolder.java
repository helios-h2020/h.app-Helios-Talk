package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactType;

@NotNullByDefault
class PendingContactViewHolder extends ViewHolder {

    private final PendingContactListener listener;
    private final CircleImageView avatar;
    private final TextView name;
    private final TextView time;
    private final TextView message;
    private final Button confirmButton;
    private final Button deleteButton;
    private final ImageButton deleteOutgoingButton;
    private final TextView outgoingRequest;

    PendingContactViewHolder(View v, PendingContactListener listener) {
        super(v);
        avatar = v.findViewById(R.id.avatar);
        name = v.findViewById(R.id.name);
        time = v.findViewById(R.id.time);
        message = v.findViewById(R.id.message);
        confirmButton = v.findViewById(R.id.confirmButton);
        deleteButton = v.findViewById(R.id.deleteButton);
        deleteOutgoingButton = v.findViewById(R.id.deleteOutgoingButton);
        outgoingRequest = v.findViewById(R.id.outgoingPrompt);
        this.listener = listener;
    }

    public void bind(PendingContactItem item) {
        PendingContact p = item.getPendingContact();
        if (p.getProfilePicture() == null) avatar.setImageResource(R.drawable.ic_person);
        else avatar.setImageBitmap(BitmapFactory.decodeByteArray(
                p.getProfilePicture(),
                0,
                p.getProfilePicture().length)
        );
        name.setText(p.getAlias());
        time.setText(UiUtils.formatDate(time.getContext(), p.getTimestamp()));
        message.setText(p.getMessage());
        deleteButton.setOnClickListener(v -> listener.onPendingContactItemDelete(item));
        // delete outgoing pending invitation
        deleteOutgoingButton.setOnClickListener(v -> listener.onPendingContactItemDelete(item));

        confirmButton.setOnClickListener(v -> listener.onPendingContactItemConfirm(item));

        // set buttons visibilities
        if (item.getPendingContact().getPendingContactType().equals(
                PendingContactType.OUTGOING)) {
            deleteButton.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);
            outgoingRequest.setVisibility(View.VISIBLE);
            deleteOutgoingButton.setVisibility(View.VISIBLE);
        } else {
            outgoingRequest.setVisibility(View.GONE);
            deleteOutgoingButton.setVisibility(View.GONE);

        }

    }

}
