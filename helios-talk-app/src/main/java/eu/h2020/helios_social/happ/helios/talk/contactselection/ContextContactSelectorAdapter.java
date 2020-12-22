package eu.h2020.helios_social.happ.helios.talk.contactselection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;

@NotNullByDefault
class ContextContactSelectorAdapter extends
		BaseContactSelectorAdapter<SelectableContactItem, SelectableContactHolder> {

	ContextContactSelectorAdapter(Context context,
			OnContactClickListener<SelectableContactItem> listener) {
		super(context, SelectableContactItem.class, listener);
	}

	@Override
	public SelectableContactHolder onCreateViewHolder(ViewGroup viewGroup,
			int i) {
		View v = LayoutInflater.from(ctx).inflate(
				R.layout.list_item_selectable_context_contact, viewGroup,
				false);
		return new SelectableContactHolder(v);
	}

}
