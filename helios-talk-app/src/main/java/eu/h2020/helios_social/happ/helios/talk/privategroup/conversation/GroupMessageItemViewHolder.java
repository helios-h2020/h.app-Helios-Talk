package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.view.AuthorView;

import static eu.h2020.helios_social.happ.helios.talk.api.util.StringUtils.trim;


@UiThread
@NotNullByDefault
public class GroupMessageItemViewHolder extends ViewHolder {

	protected final GroupListener listener;
	private final View root;
	protected final ConstraintLayout layout;
	protected final AuthorView author;
	protected final TextView text;
	protected final ImageView favourite;
	@Nullable
	private String itemKey = null;

	GroupMessageItemViewHolder(View v, GroupListener listener) {
		super(v);
		this.listener = listener;
		root = v;
		layout = v.findViewById(R.id.layout);
		text = v.findViewById(R.id.text);
		author = v.findViewById(R.id.author);
		favourite = v.findViewById(R.id.favourite);
	}

	@CallSuper
	void bind(Context ctx, GroupMessageItem item, boolean selected) {
		itemKey = item.getKey();
		root.setActivated(selected);

		if (item.getText() != null) {
			text.setText(trim(item.getText()));
		}
		author.setAuthor(item.getPeerInfo());

		long timestamp = item.getTime();
		author.setDate(timestamp);

		if (favourite != null) {
			if (item.isFavourite())
				favourite.setImageResource(R.drawable.ic_star_enable);
			else
				favourite.setImageResource(R.drawable.ic_star_disable);

			favourite.setOnClickListener(v -> {
				listener.onFavouriteClicked(v, item);
			});
		}

	}

	@Nullable
	String getItemKey() {
		return itemKey;
	}

}
