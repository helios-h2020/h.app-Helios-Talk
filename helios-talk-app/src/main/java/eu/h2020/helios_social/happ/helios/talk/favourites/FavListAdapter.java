package eu.h2020.helios_social.happ.helios.talk.favourites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;

public class FavListAdapter
		extends HeliosTalkAdapter<FavItem, FavItemViewHolder> {

	public FavListAdapter(Context ctx) {
		super(ctx, FavItem.class);
	}

	@Override
	public FavItemViewHolder onCreateViewHolder(ViewGroup parent,
			int viewType) {
		View v = LayoutInflater.from(ctx).inflate(
				R.layout.list_item_fav, parent, false);
		return new FavItemViewHolder(v);
	}

	@Override
	public void onBindViewHolder(FavItemViewHolder ui, int position) {
		ui.bind(ctx, items.get(position));
	}

	@Override
	public int compare(FavItem a, FavItem b) {
		if (a == b) return 0;
		// The fav message with the latest message comes first
		long aTime = a.getTime(), bTime = b.getTime();
		if (aTime > bTime) return -1;
		else return 1;
	}

	@Override
	public boolean areContentsTheSame(FavItem item1, FavItem item2) {
		return item1.getMessageId().equals(item2.getMessageId());
	}

	@Override
	public boolean areItemsTheSame(FavItem a, FavItem b) {
		return a.getMessageId().equals(b.getMessageId());
	}
}
