package eu.h2020.helios_social.happ.helios.talk.account;


import android.content.Context;
import android.util.AttributeSet;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.R;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

@UiThread
@NotNullByDefault
class DozeView extends PowerView {

	@Nullable
	private Runnable onButtonClickListener;

	public DozeView(Context context) {
		this(context, null);
	}

	public DozeView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DozeView(Context context, @Nullable AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setText(R.string.setup_doze_intro);
		setButtonText(R.string.setup_doze_button);
	}

	@Override
	public boolean needsToBeShown() {
		return needsToBeShown(getContext());
	}

	public static boolean needsToBeShown(Context context) {
		return UiUtils.needsDozeWhitelisting(context);
	}

	@Override
	protected int getHelpText() {
		return R.string.setup_doze_explanation;
	}

	@Override
	protected void onButtonClick() {
		if (onButtonClickListener == null) throw new IllegalStateException();
		onButtonClickListener.run();
	}

	public void setOnButtonClickListener(Runnable runnable) {
		onButtonClickListener = runnable;
	}

}
