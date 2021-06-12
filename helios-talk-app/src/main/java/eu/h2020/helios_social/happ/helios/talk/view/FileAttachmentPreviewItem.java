package eu.h2020.helios_social.happ.helios.talk.view;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;

public class FileAttachmentPreviewItem {

    private final Uri uri;
    @Nullable
    private AttachmentItem item;

    FileAttachmentPreviewItem(Uri uri) {
        this.uri = uri;
        this.item = null;
    }

    public void setItem(AttachmentItem item) {
        this.item = item;
    }

    @Nullable
    public AttachmentItem getItem() {
        return item;
    }

    public String getFileName() {
        return uri.getLastPathSegment().replaceAll(".*/","");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof FileAttachmentPreviewItem &&
                uri.equals(((FileAttachmentPreviewItem) o).uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
