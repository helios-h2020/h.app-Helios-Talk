package eu.h2020.helios_social.happ.helios.talk.util;

import android.annotation.SuppressLint;
import android.content.Context;

import eu.h2020.helios_social.happ.helios.talk.R;

import androidx.annotation.ColorRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static android.os.Build.VERSION.SDK_INT;
import static androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE;


public class HeliosTalkNotificationBuilder extends NotificationCompat.Builder {

	public HeliosTalkNotificationBuilder(Context context, String channelId) {
		super(context, channelId);
		// Auto-cancel does not fire the delete intent, see
		// https://issuetracker.google.com/issues/36961721
		setAutoCancel(true);

		setLights(ContextCompat.getColor(context, R.color.green_light),
				750, 500);
		if (SDK_INT >= 21) setVisibility(VISIBILITY_PRIVATE);
	}

	@SuppressLint("RestrictedApi")
	public HeliosTalkNotificationBuilder setColorRes(@ColorRes int res) {
		setColor(ContextCompat.getColor(mContext, res));
		return this;
	}

	public HeliosTalkNotificationBuilder setNotificationCategory(String category) {
		if (SDK_INT >= 21) setCategory(category);
		return this;
	}

}
