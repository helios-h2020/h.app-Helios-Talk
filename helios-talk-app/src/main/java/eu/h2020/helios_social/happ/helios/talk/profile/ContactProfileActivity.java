package eu.h2020.helios_social.happ.helios.talk.profile;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.ProfileReceivedEvent;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.Profile;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.ProfileManager;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.sharing.SharingProfileManager;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_ID;
import static java.util.logging.Logger.getLogger;

public class ContactProfileActivity extends HeliosTalkActivity implements EventListener {

    private static final Logger LOG =
            getLogger(ContactProfileActivity.class.getName());

    @Inject
    volatile ProfileManager profileManager;
    @Inject
    volatile SharingProfileManager sharingProfileManager;
    @Inject
    volatile ContextualEgoNetwork egoNetwork;
    @Inject
    EventBus eventBus;

    private ContactId contactId;

    private ImageView avatar;
    private TextView nickname;
    private TextView fullname;
    private Spinner gender;
    private Spinner country;
    private TextView work;
    private TextView uni;
    private TagEditText interests;
    private TextView quote;


    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        Intent i = getIntent();

        String id = i.getStringExtra(CONTACT_ID);
        if (id == null) throw new IllegalStateException();
        contactId = new ContactId(id);
        setContentView(R.layout.activity_contact_profile);

        avatar = findViewById(R.id.avatarView);
        nickname = findViewById(R.id.user_nickaname);
        fullname = findViewById(R.id.user_fullname);
        gender = findViewById(R.id.gender);
        country = findViewById(R.id.country);
        work = findViewById(R.id.work);
        uni = findViewById(R.id.university);
        interests = findViewById(R.id.interests);
        quote = findViewById(R.id.quote);
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (signedIn()) {
            LOG.info("Sending Profile Request");
            sharingProfileManager.sendProfileRequest(contactId,
                    egoNetwork.getCurrentContext().getData().toString().split("%")[1]);
        }
    }

    public void displayProfile(Profile p) {
        avatar.setImageResource(R.drawable.ic_person_big);
        nickname.setText(p.getAlias().replace("[", "").replace("]", "")
                .replaceAll("\"", ""));
        setTitle(p.getAlias().replace("[", "").replace("]", "")
                .replaceAll("\"", "") + "'s Profile");
        fullname.setText(p.getFullname());
        gender.setSelection(p.getGender());
        country.setSelection(p.getCountry());
        uni.setText(p.getUniversity());
        work.setText(p.getWork());
        interests.setText(p.getInterests());
        if (interests.getText().length() > 0) {
            interests.format();
        }
        quote.setText(p.getQuote());
        if (p.getProfilePic() != null) {
            avatar.setImageBitmap(BitmapFactory
                    .decodeByteArray(p.getProfilePic(), 0,
                            p.getProfilePic().length));
        }
    }

    @Override
    public void eventOccurred(Event e) {
        if (e instanceof ProfileReceivedEvent) {
            if (contactId.getId().equals(((ProfileReceivedEvent) e).getContactId().getId())) {
                displayProfile(((ProfileReceivedEvent) e).getProfile());
            }
        }
    }
}

