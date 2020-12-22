package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import android.view.View;

import androidx.annotation.UiThread;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
public interface GroupListener {

	void onFavouriteClicked(View view, GroupMessageItem messageItem);

	void onGroupDissolved();
}
