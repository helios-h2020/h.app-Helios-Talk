package eu.h2020.helios_social.happ.helios.talk.share;

import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;

public interface ShareContentController {

    void shareText(ContactId contactId, String groupId, String contextId,
                   String text, UiResultExceptionHandler<Void, Exception> handler);

    void shareText(Group group, String text, UiResultExceptionHandler<Void, Exception> handler);

    void shareImages(ContactId contactId, String groupId, String contextId,
                     List<Attachment> attachments, String text, UiResultExceptionHandler<Void, Exception> handler);

    void shareImages(Group group, List<Attachment> attachments, String text,
                     UiResultExceptionHandler<Void, Exception> handler);

}
