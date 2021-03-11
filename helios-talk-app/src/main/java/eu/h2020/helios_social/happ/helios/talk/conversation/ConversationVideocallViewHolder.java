package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;


import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.videocall.VideoCallActivity;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils.isNullOrEmpty;


@UiThread
@NotNullByDefault
public class ConversationVideocallViewHolder
		extends ConversationItemViewHolder {

	private final Button joinButton;

	ConversationVideocallViewHolder(View v, ConversationListener listener,
			boolean isIncoming) {
		super(v, listener, isIncoming);
		this.joinButton = v.findViewById(R.id.joinButton);
	}

	@Override
	@CallSuper
	void bind(Context ctx, ConversationItem item, boolean selected) {
		VideoCallConversationItem notice = (VideoCallConversationItem) item;
		super.bind(ctx, notice, selected);

		String room_id = notice.getRoomId();
		text.setText(((VideoCallConversationItem) item).getPromptText());

		if (!isNullOrEmpty(room_id)) {
			joinButton.setOnClickListener(v -> startVideoCall(ctx, room_id));
		}
	}

	private void startVideoCall(Context ctx, String room_id) {
		Intent videoCallIntent =
				new Intent(ctx, VideoCallActivity.class);
		videoCallIntent.putExtra("room_name", room_id);
		ctx.startActivity(videoCallIntent);
	}
}
