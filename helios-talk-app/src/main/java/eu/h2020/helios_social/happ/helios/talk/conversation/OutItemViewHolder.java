package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
class OutItemViewHolder {

	private final ImageView status;

	OutItemViewHolder(View v) {
		status = v.findViewById(R.id.status);
	}

	void bind(ConversationItem item) {
		int res;
		if (item.isSeen()) {
			res = R.drawable.message_delivered;
		} else if (item.isSent()) {
			res = R.drawable.message_sent;
		} else {
			res = R.drawable.message_stored;
		}
		status.setImageResource(res);
	}

}
