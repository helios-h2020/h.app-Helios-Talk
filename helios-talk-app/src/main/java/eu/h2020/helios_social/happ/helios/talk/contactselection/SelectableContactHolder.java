package eu.h2020.helios_social.happ.helios.talk.contactselection;

import android.view.View;

import javax.annotation.Nullable;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contact.BaseContactListAdapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@UiThread
@NotNullByDefault
class SelectableContactHolder
		extends BaseSelectableContactHolder<SelectableContactItem> {

	SelectableContactHolder(View v) {
		super(v);
	}

	@Override
	protected void bind(SelectableContactItem item, @Nullable
			BaseContactListAdapter.OnContactClickListener<SelectableContactItem> listener) {
		super.bind(item, listener);

		if (item.isDisabled()) {
			info.setVisibility(VISIBLE);
		} else {
			info.setVisibility(GONE);
		}
	}

}
