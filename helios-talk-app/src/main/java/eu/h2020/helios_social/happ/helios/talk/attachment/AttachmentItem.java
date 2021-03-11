package eu.h2020.helios_social.happ.helios.talk.attachment;

import android.net.Uri;

import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.Immutable;

import androidx.annotation.Nullable;

import static java.util.Objects.requireNonNull;

@Immutable
@NotNullByDefault
public class AttachmentItem {

    private final Uri uri;
    private final String mimeType;
    private String storageURL;
    private long instanceId;
    private static final AtomicLong NEXT_INSTANCE_ID = new AtomicLong(0);

    public AttachmentItem(Uri uri, String mimeType) {
        this.uri = uri;
        this.mimeType = mimeType;
        instanceId = NEXT_INSTANCE_ID.getAndIncrement();
    }

    public AttachmentItem(Uri uri, String mimeType, String storageURL) {
        this.uri = uri;
        this.mimeType = mimeType;
        this.storageURL = storageURL;
        instanceId = NEXT_INSTANCE_ID.getAndIncrement();
    }

    public String getMimeType() {
        return mimeType;
    }

    public Uri getUri() {
        return uri;
    }

    public String getTransitionName() {
        return String.valueOf(instanceId);
    }

    public void setStorageURL(String url) {
        this.storageURL = url;
    }

    public String getStorageURL(){
        return storageURL;
    }
    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof AttachmentItem &&
                instanceId == ((AttachmentItem) o).instanceId;
    }

}
