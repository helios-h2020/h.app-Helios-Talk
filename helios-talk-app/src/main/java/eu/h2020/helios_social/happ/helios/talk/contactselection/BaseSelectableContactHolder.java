package eu.h2020.helios_social.happ.helios.talk.contactselection;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import javax.annotation.Nullable;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.contact.BaseContactListAdapter;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactItemViewHolder;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

@UiThread
@NotNullByDefault
public class BaseSelectableContactHolder<I extends SelectableContactItem>
		extends ContactItemViewHolder<I> {

	private final CheckBox checkBox;
	protected final TextView info;

	public BaseSelectableContactHolder(View v) {
		super(v);
		checkBox = v.findViewById(R.id.checkBox);
		info = v.findViewById(R.id.infoView);
	}

	@Override
	protected void bind(I item, @Nullable
			BaseContactListAdapter.OnContactClickListener<I> listener) {
		super.bind(item, listener);

		if (item.isSelected()) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}

		if (item.isDisabled()) {
			layout.setEnabled(false);
			grayOutItem(true);
		} else {
			layout.setEnabled(true);
			grayOutItem(false);
		}
	}

	protected void grayOutItem(boolean gray) {
		float alpha = gray ? UiUtils.GREY_OUT : 1f;
		avatar.setAlpha(alpha);
		name.setAlpha(alpha);
		checkBox.setAlpha(alpha);
		info.setAlpha(alpha);
	}

}
