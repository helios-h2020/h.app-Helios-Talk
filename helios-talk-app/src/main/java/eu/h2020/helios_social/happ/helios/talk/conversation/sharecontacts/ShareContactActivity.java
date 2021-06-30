package eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.contactselection.ContactSelectorActivity;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.conversation.SharedContactConversationItem;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.CreateForumController;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.ForumInviteActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.ForumInviteFragment;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.Peer;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerId;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_ID;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_NAME;

public class ShareContactActivity extends ContactSelectorActivity {
    private static final Logger LOG =
            Logger.getLogger(ForumInviteActivity.class.getName());

    @Inject
    ShareContactController controller;
    private PeerInfo peerInfo;

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);

        Intent i = getIntent();
        String contactId = i.getStringExtra(CONTACT_ID);
        if (contactId == null)
            throw new IllegalStateException("No CONTACT_ID in intent");
        String contact_name = i.getStringExtra(CONTACT_NAME);
        if (contactId == null)
            throw new IllegalStateException("No CONTACT_NAME in intent");
        peerInfo = new PeerInfo.Builder()
                .peerId(new PeerId(contactId))
                .alias(contact_name)
                .build();

        if (bundle == null) {
            showInitialFragment(ShareContactFragment.newInstance());
        }
    }

    @Override
    public void contactsSelected(Collection<ContactId> contacts) {
        super.contactsSelected(contacts);
        LOG.info("sharing contact");
        shareContact();
        super.onBackPressed();
    }

    public void shareContact() {
        if (peerInfo == null)
            throw new IllegalStateException("PeerInfo was not initialized");
        controller.shareContact(peerInfo, contacts,
                                new UiResultExceptionHandler<Void, DbException>(this) {
                                    @Override
                                    public void onResultUi(Void result) {
                                        setResult(RESULT_OK);
                                        supportFinishAfterTransition();
                                    }

                                    @Override
                                    public void onExceptionUi(DbException exception) {
                                        setResult(RESULT_CANCELED);
                                        handleDbException(exception);
                                    }
                                });
    }
}
