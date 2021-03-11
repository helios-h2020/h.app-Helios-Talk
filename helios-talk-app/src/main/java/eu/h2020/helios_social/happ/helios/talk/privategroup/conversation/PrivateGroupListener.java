package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import androidx.annotation.UiThread;

import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationListener;
import eu.h2020.helios_social.happ.helios.talk.group.GroupListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
public interface PrivateGroupListener extends ConversationListener {

    void onGroupDissolved();
}
