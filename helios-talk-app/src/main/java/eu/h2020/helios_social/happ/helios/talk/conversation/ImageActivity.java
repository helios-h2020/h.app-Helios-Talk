package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.vanniktech.emoji.EmojiTextView;

import javax.annotation.Nullable;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.ATTACHMENT_URI;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_NAME;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.TEXT_MESSAGE;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.TIMESTAMP;
import static java.util.Objects.requireNonNull;

public class ImageActivity extends HeliosTalkActivity {

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        Intent intent = getIntent();
        String contactName = intent.getStringExtra(CONTACT_NAME);
        Uri uri = Uri.parse(intent.getStringExtra(ATTACHMENT_URI));
        long timestamp = intent.getLongExtra(TIMESTAMP, 0);
        String text_message = intent.getStringExtra(TEXT_MESSAGE);

        setContentView(R.layout.activity_image);

        Toolbar toolbar = requireNonNull(setUpCustomToolbar(true));
        EmojiTextView contactNameView = toolbar.findViewById(R.id.contactName);
        TextView messageTextView = toolbar.findViewById(R.id.message);
        TextView date = toolbar.findViewById(R.id.time);
        ImageView imageView = findViewById(R.id.imagefull);

        contactNameView.setText(contactName);
        messageTextView.setText(text_message);
        date.setText(UiUtils.formatDate(this, timestamp));
        imageView.setImageURI(uri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
