package eu.h2020.helios_social.happ.helios.talk.contact;

import android.content.Context;
import android.view.View;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

import static androidx.recyclerview.widget.SortedList.INVALID_POSITION;

public abstract class BaseContactListAdapter<I extends ContactItem, VH extends ContactItemViewHolder<I>>
		extends HeliosTalkAdapter<I, VH> {

	@Nullable
	protected final OnContactClickListener<I> listener;

	public BaseContactListAdapter(Context ctx, Class<I> c,
			@Nullable OnContactClickListener<I> listener) {
		super(ctx, c);
		this.listener = listener;
	}

	@Override
	public void onBindViewHolder(@NonNull VH ui, int position) {
		I item = items.get(position);
		ui.bind(item, listener);
	}

	@Override
	public int compare(I c1, I c2) {
		return c1.getContact().getAlias()
				.compareTo(c2.getContact().getAlias());
	}

	@Override
	public boolean areItemsTheSame(I c1, I c2) {
		return c1.getContact().equals(c2.getContact());
	}

	@Override
	public boolean areContentsTheSame(ContactItem c1, ContactItem c2) {
		return true;
	}

	int findItemPosition(ContactId c) {
		for (int i = 0; i < getItemCount(); i++) {
			I item = getItemAt(i);
			if (item != null && item.getContact().getId().equals(c))
				return i;
		}
		return INVALID_POSITION; // Not found
	}

	public interface OnContactClickListener<I> {
		void onItemClick(View view, I item);
	}

}
