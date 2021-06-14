package eu.h2020.helios_social.happ.helios.talk.share;

import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;

public interface ShareContentController {

    void shareText(ContactId contactId, String groupId, String contextId,
                   String text, UiResultExceptionHandler<Void, Exception> handler);

    void shareText(Group group, String text, UiResultExceptionHandler<Void, Exception> handler);

    void shareAttachments(ContactId contactId, String groupId, String contextId,
                          List<Attachment> attachments, String text, Message.Type messageType, UiResultExceptionHandler<Void, Exception> handler);

    void shareAttachments(Group group, List<Attachment> attachments, String text, Message.Type messageType,
                          UiResultExceptionHandler<Void, Exception> handler);

}
