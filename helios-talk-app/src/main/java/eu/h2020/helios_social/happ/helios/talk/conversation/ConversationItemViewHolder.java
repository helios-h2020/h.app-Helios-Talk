package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.Context;
import android.view.View;
import android.widget.TextView;


import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils.trim;


@UiThread
@NotNullByDefault
public abstract class ConversationItemViewHolder extends ViewHolder {

	protected final ConversationListener listener;
	private final View root;
	protected final ConstraintLayout layout;
	@Nullable
	private final OutItemViewHolder outViewHolder;
	protected final TextView text;
	protected final TextView time;
	@Nullable
	private String itemKey = null;

	ConversationItemViewHolder(View v, ConversationListener listener,
			boolean isIncoming) {
		super(v);
		this.listener = listener;
		this.outViewHolder = isIncoming ? null : new OutItemViewHolder(v);
		root = v;
		layout = v.findViewById(R.id.layout);
		text = v.findViewById(R.id.text);
		time = v.findViewById(R.id.time);
	}

	@CallSuper
	void bind(Context ctx, ConversationItem item, boolean selected) {
		itemKey = item.getKey();
		root.setActivated(selected);

		if (item.getText() != null) {
			text.setText(trim(item.getText()));
		}

		long timestamp = item.getTime();
		time.setText(UiUtils.formatDate(time.getContext(), timestamp));

		if (outViewHolder != null) outViewHolder.bind(item);
	}

	boolean isIncoming() {
		return outViewHolder == null;
	}

	@Nullable
	String getItemKey() {
		return itemKey;
	}

}
