package eu.h2020.helios_social.happ.helios.talk.share;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.ShareContentType;

public class ShareContentItem implements Cloneable {

    private ShareContentType contentType;
    private String text;
    private List<Attachment> attachments;

    public ShareContentItem(ShareContentType contentType, String text) {
        this.contentType = contentType;
        this.text = text;
    }

    public ShareContentItem(ShareContentType contentType, List<Attachment> attachments, String text) {
        this.contentType = contentType;
        this.attachments = attachments;
        this.text = text;
    }


    public ShareContentType getContentType() {
        return contentType;
    }

    public String getText() {
        return text;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    @Override
    public String toString() {
        return "ShareContentItem{" +
                "contentType=" + contentType +
                ", text='" + text + '\'' +
                ", attachments=" + attachments +
                '}';
    }

    @Override
    public ShareContentItem clone() {
        ShareContentItem shareContentItem = null;
        try {
            shareContentItem = (ShareContentItem) super.clone();
        } catch (CloneNotSupportedException e) {
            shareContentItem = new ShareContentItem(this.contentType, this.attachments, this.text);
        }
        if (attachments != null) {
            shareContentItem.attachments = cloneList(this.attachments);
        }
        return shareContentItem;
    }

    private List<Attachment> cloneList(List<Attachment> attachments) {
        List<Attachment> clone = new ArrayList<Attachment>(attachments.size());
        for (Attachment item : attachments) clone.add(item.clone());
        return clone;
    }
}
