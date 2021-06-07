package eu.h2020.helios_social.happ.helios.talk.share;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.controller.DbControllerImpl;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.attachment.AttachmentManager;
import eu.h2020.helios_social.modules.groupcommunications.api.attachment.InvalidAttachmentException;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupMessage;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privateconversation.PrivateMessageFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.GroupMessageFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.utils.Pair;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;

public class ShareContentControllerImpl extends DbControllerImpl implements ShareContentController {
    private static final Logger LOG = Logger.getLogger(ShareContentControllerImpl.class.getName());

    private final MessagingManager messagingManager;
    private final PrivateMessageFactory privateMessageFactory;
    private final GroupMessageFactory groupMessageFactory;
    private final GroupManager groupManager;
    private final AttachmentManager attachmentManager;

    @Inject
    public ShareContentControllerImpl(@DatabaseExecutor Executor dbExecutor,
                                      LifecycleManager lifecycleManager,
                                      MessagingManager messagingManager,
                                      PrivateMessageFactory privateMessageFactory,
                                      GroupMessageFactory groupMessageFactory,
                                      GroupManager groupManager,
                                      AttachmentManager attachmentManager) {
        super(dbExecutor, lifecycleManager);
        this.messagingManager = messagingManager;
        this.groupMessageFactory = groupMessageFactory;
        this.privateMessageFactory = privateMessageFactory;
        this.groupManager = groupManager;
        this.attachmentManager = attachmentManager;
    }

    @Override
    public void shareText(ContactId contactId, String groupId, String contextId,
                          String text, UiResultExceptionHandler<Void, Exception> handler) {
        runOnDbThread(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                Message message = privateMessageFactory.createTextMessage(
                        groupId,
                        timestamp,
                        requireNonNull(text));

                messagingManager.sendPrivateMessage(
                        contactId,
                        contextId,
                        message
                );

                handler.onResult(null);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    public void shareText(Group group, String text, UiResultExceptionHandler<Void, Exception> handler) {
        runOnDbThread(() -> {
            try {
                Pair<String, String> fakeIdentity =
                        groupManager.getFakeIdentity(group.getId());
                long timestamp = System.currentTimeMillis();
                GroupMessage message = groupMessageFactory.createGroupMessage(
                        group.getId(),
                        requireNonNull(text),
                        timestamp,
                        fakeIdentity.getFirst(),
                        fakeIdentity.getSecond());

                messagingManager.sendGroupMessage(group, message);

                handler.onResult(null);
            } catch (DbException | FormatException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    public void shareImages(ContactId contactId, String groupId, String contextId,
                            List<Attachment> attachments, String text, UiResultExceptionHandler<Void, Exception> handler) {
        runOnDbThread(() -> {
            try {
                if (attachmentManager.validateAttachments(attachments)) {
                    long timestamp = System.currentTimeMillis();

                    Message message = privateMessageFactory.createImageAttachmentMessage(
                            groupId,
                            timestamp,
                            text,
                            attachments);

                    messagingManager.sendPrivateMessage(
                            contactId,
                            contextId,
                            message
                    );
                    handler.onResult(null);
                } else {
                    String extra_message = "";
                    if (text != null && validateURL(text)) {
                        long timestamp = System.currentTimeMillis();
                        Message message = privateMessageFactory.createTextMessage(
                                groupId,
                                timestamp,
                                requireNonNull(text)
                        );

                        messagingManager.sendPrivateMessage(
                                contactId,
                                contextId,
                                message
                        );
                        extra_message = "\nOnly the text has been shared to chat!";
                    }
                    if (attachments.size() > 1)
                        handler.onException(new InvalidAttachmentException("Invalid Attachments" + extra_message));
                    else
                        handler.onException(new InvalidAttachmentException("Invalid Attachment" + extra_message));

                }
            } catch (DbException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    @Override
    public void shareImages(Group group, List<Attachment> attachments, String
            text, UiResultExceptionHandler<Void, Exception> handler) {
        runOnDbThread(() -> {
            try {
                if (attachmentManager.validateAttachments(attachments)) {
                    Pair<String, String> fakeIdentity =
                            groupManager.getFakeIdentity(group.getId());
                    long timestamp = System.currentTimeMillis();

                    GroupMessage message = groupMessageFactory.createImageAttachmentMessage(
                            group.getId(),
                            attachments,
                            text,
                            timestamp,
                            fakeIdentity.getFirst(),
                            fakeIdentity.getSecond());

                    messagingManager.sendGroupMessage(group, message);
                    handler.onResult(null);
                } else {
                    String extra_message = "";
                    if (text != null && validateURL(text)) {
                        long timestamp = System.currentTimeMillis();
                        Pair<String, String> fakeIdentity =
                                groupManager.getFakeIdentity(group.getId());
                        GroupMessage message = groupMessageFactory.createGroupMessage(
                                group.getId(),
                                requireNonNull(text),
                                timestamp,
                                fakeIdentity.getFirst(),
                                fakeIdentity.getSecond());

                        messagingManager.sendGroupMessage(
                                group,
                                message
                        );
                        extra_message = "\nOnly the text has been shared to chat!";
                    }
                    if (attachments.size() > 1)
                        handler.onException(new InvalidAttachmentException("Invalid Attachments" + extra_message));
                    else
                        handler.onException(new InvalidAttachmentException("Invalid Attachment" + extra_message));
                }
            } catch (DbException | FormatException e) {
                logException(LOG, WARNING, e);
                handler.onException(e);
            }
        });
    }

    boolean validateURL(String text) {
        String urlRegex = "((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }
}
