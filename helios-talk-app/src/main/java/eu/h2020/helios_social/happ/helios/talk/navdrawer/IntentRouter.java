package eu.h2020.helios_social.happ.helios.talk.navdrawer;

import android.content.Context;
import android.content.Intent;

import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.EXTRA_TEXT;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static eu.h2020.helios_social.modules.groupcommunications_utils.contact.HeliosLinkConstants.LINK_REGEX;

class IntentRouter {

	static void handleExternalIntent(Context ctx, Intent i) {
		String action = i.getAction();
		// add remote contact with clicked helios:// link
		if (ACTION_VIEW.equals(action) && "helios".equals(i.getScheme())) {
			//redirect(ctx, i, AddContactActivity.class);
		}
		// add remote contact with shared helios:// link
		else if (ACTION_SEND.equals(action) &&
				"text/plain".equals(i.getType()) &&
				i.getStringExtra(EXTRA_TEXT) != null &&
				LINK_REGEX.matcher(i.getStringExtra(EXTRA_TEXT)).find()) {
			//redirect(ctx, i, AddContactActivity.class);
		}
	}

	private static void redirect(Context ctx, Intent i,
			Class<? extends HeliosTalkActivity> activityClass) {
		i.setClass(ctx, activityClass);
		i.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
		ctx.startActivity(i);
	}

}
