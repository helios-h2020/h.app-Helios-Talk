package eu.h2020.helios_social.happ.helios.talk.favourites;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;


public class FavItemViewHolder extends RecyclerView.ViewHolder {

	protected final ConstraintLayout layout;
	protected final CircleImageView avatar;
	protected final TextView message;
	protected final TextView name;
	@Nullable
	private final TextView date;
	private final ConstraintSet textConstraints = new ConstraintSet();
	private final ConstraintSet imageConstraints = new ConstraintSet();
	private final ConstraintSet imageTextConstraints = new ConstraintSet();

	public FavItemViewHolder(View v) {
		super(v);

		layout = v.findViewById(R.id.layout);
		avatar = v.findViewById(R.id.avatar);
		name = v.findViewById(R.id.authorName);
		date = v.findViewById(R.id.dateView);
		message = v.findViewById(R.id.text);

		textConstraints.clone(v.getContext(),
				R.layout.list_item_fav_content);
		imageConstraints.clone(v.getContext(),
				R.layout.list_item_fav_image);
		imageTextConstraints.clone(v.getContext(),
				R.layout.list_item_fav_image_text);
	}

	protected void bind(Context ctx, FavItem item) {
		avatar.setImageResource(R.drawable.ic_person);
		name.setText(item.getAuthor());
		long timestamp = item.getTime();
		date.setText(UiUtils.formatDate(date.getContext(), timestamp));
		message.setText(item.getText());

		bindTextItem();

	}

	private void bindTextItem() {
		textConstraints.applyTo(layout);
	}

}
