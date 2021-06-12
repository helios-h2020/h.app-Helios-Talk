package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.vanniktech.emoji.EmojiTextView;

import java.io.IOException;

import javax.annotation.Nullable;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.conversation.glide.GlideApp;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static android.view.View.INVISIBLE;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.NONE;
import static com.bumptech.glide.load.resource.bitmap.DownsampleStrategy.FIT_CENTER;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.ATTACHMENT_URI;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.CONTACT_NAME;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.TEXT_MESSAGE;
import static eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity.TIMESTAMP;
import static java.util.Objects.requireNonNull;

public class ImageActivity extends HeliosTalkActivity {
    @DrawableRes
    private static final int ERROR_RES = R.drawable.ic_image_broken;

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

        GlideApp.with(imageView)
                .load(uri)
                .diskCacheStrategy(NONE)
                .error(ERROR_RES)
                .downsample(FIT_CENTER)
                .transition(withCrossFade())
                .into(imageView);
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
