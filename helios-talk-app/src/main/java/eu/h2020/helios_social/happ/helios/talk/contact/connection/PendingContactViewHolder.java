package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.happ.helios.talk.view.TextAvatarView;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactType;

@NotNullByDefault
class PendingContactViewHolder extends ViewHolder {

	private final PendingContactListener listener;
	private final TextAvatarView avatar;
	private final TextView name;
	private final TextView time;
	private final TextView message;
	private final Button confirmButton;
	private final Button deleteButton;
	private final TextView outgoingRequet;


	PendingContactViewHolder(View v, PendingContactListener listener) {
		super(v);
		avatar = v.findViewById(R.id.avatar);
		name = v.findViewById(R.id.name);
		time = v.findViewById(R.id.time);
		message = v.findViewById(R.id.message);
		confirmButton = v.findViewById(R.id.confirmButton);
		deleteButton = v.findViewById(R.id.deleteButton);
		outgoingRequet = v.findViewById(R.id.outgoingPrompt);
		this.listener = listener;
	}

	public void bind(PendingContactItem item) {
		PendingContact p = item.getPendingContact();
		avatar.setText(p.getAlias());
		avatar.setBackgroundBytes(p.getId().getId().getBytes());
		name.setText(p.getAlias());
		time.setText(UiUtils.formatDate(time.getContext(), p.getTimestamp()));
		message.setText(p.getMessage());
		deleteButton.setOnClickListener(v -> {
			listener.onPendingContactItemDelete(item);
		});

		confirmButton.setOnClickListener(v -> {
			listener.onPendingContactItemConfirm(item);
		});

		if (item.getPendingContact().getPendingContactType().equals(
				PendingContactType.OUTGOING)) {
			deleteButton.setVisibility(View.GONE);
			confirmButton.setVisibility(View.GONE);
			outgoingRequet.setVisibility(View.VISIBLE);
		} else {
			outgoingRequet.setVisibility(View.GONE);
		}

	}

}
