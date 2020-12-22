package eu.h2020.helios_social.happ.helios.talk;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import eu.h2020.helios_social.happ.android.AndroidNotificationManager;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class NotificationCleanupService extends IntentService {

	private static final String TAG =
			NotificationCleanupService.class.getName();

	@Inject
	AndroidNotificationManager notificationManager;

	public NotificationCleanupService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		AndroidComponent applicationComponent =
				((HeliosTalkApplication) getApplication())
						.getApplicationComponent();
		applicationComponent.inject(this);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {

	}

	/*@Override
	protected void onHandleIntent(@Nullable Intent i) {
		if (i == null || i.getData() == null) return;
		Uri uri = i.getData();
		if (uri.equals(CONTACT_URI)) {
		//	notificationManager.clearAllContactNotifications();
		} else if (uri.equals(GROUP_URI)) {
		//	notificationManager.clearAllGroupMessageNotifications();
		} else if (uri.equals(FORUM_URI)) {
		//	notificationManager.clearAllForumPostNotifications();
		} else if (uri.equals(CONTACT_ADDED_URI)) {
		//	notificationManager.clearAllContactAddedNotifications();
		}
	}*/
}
