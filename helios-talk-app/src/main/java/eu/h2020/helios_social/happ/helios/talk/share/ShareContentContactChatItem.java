package eu.h2020.helios_social.happ.helios.talk.share;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;

public class ShareContentContactChatItem extends ShareContentChatItem {

    private Contact contact;

    public ShareContentContactChatItem(Contact contact, Group group) {
        super(group.getId(), group.getContextId(), contact.getAlias(), group.getGroupType());
        this.contact = contact;
    }

    public Contact getContact() {
        return contact;
    }

    public byte[] getProfilePicture() {
        return contact.getProfilePicture();
    }

    @Override
    public int getIconResourceId() {
        return R.drawable.ic_person_white;
    }
}
