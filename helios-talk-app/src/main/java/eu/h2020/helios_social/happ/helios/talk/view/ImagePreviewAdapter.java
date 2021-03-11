package eu.h2020.helios_social.happ.helios.talk.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static java.util.Objects.requireNonNull;

@NotNullByDefault
class ImagePreviewAdapter extends Adapter<ImagePreviewViewHolder> {

	private final List<ImagePreviewItem> items;
	@LayoutRes
	private final int layout;

	ImagePreviewAdapter(Collection<ImagePreviewItem> items) {
		this.items = new ArrayList<>(items);
		this.layout = items.size() == 1 ?
				R.layout.list_item_image_preview_single :
				R.layout.list_item_image_preview;
	}

	@Override
	public ImagePreviewViewHolder onCreateViewHolder(ViewGroup viewGroup,
			int type) {
		View v = LayoutInflater.from(viewGroup.getContext())
				.inflate(layout, viewGroup, false);
		return new ImagePreviewViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ImagePreviewViewHolder viewHolder,
			int position) {
		viewHolder.bind(items.get(position));
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	int loadItemPreview(AttachmentItem attachmentItem) {
		ImagePreviewItem newItem = new ImagePreviewItem(attachmentItem.getUri());
		int pos = items.indexOf(newItem);
		if (pos == NO_POSITION) throw new AssertionError();
		ImagePreviewItem item = items.get(pos);
		if (item.getItem() == null) {
			item.setItem(requireNonNull(attachmentItem));
			notifyItemChanged(pos, item);
			return pos;
		}
		return NO_POSITION;
	}

}
