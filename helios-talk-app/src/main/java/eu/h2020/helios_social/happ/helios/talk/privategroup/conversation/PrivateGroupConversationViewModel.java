package eu.h2020.helios_social.happ.helios.talk.privategroup.conversation;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import eu.h2020.helios_social.core.context.Context;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentRetriever;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.NoSuchGroupException;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.LiveEvent;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.MutableLiveEvent;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupMessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupMessage;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingManager;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.GroupMessageFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;
import eu.h2020.helios_social.modules.groupcommunications.api.utils.Pair;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

public class PrivateGroupConversationViewModel extends AndroidViewModel {
    private static Logger LOG =
            getLogger(PrivateGroupConversationViewModel.class.getName());

    @DatabaseExecutor
    private final Executor dbExecutor;
    private final MessagingManager messagingManager;
    private final ConversationManager conversationManager;
    private final GroupManager groupManager;
    private final ContextManager contextManager;
    private final GroupMessageFactory groupMessageFactory;
    private final AttachmentRetriever attachmentRetriever;

    @Nullable
    private String groupId = null;
    private final MutableLiveData<Context> context = new MutableLiveData<>();
    private String groupName = null;
    private final MutableLiveData<PrivateGroup> privateGroup =
            new MutableLiveData<>();
    private final MutableLiveData<Boolean> groupDisolved =
            new MutableLiveData<>();
    private final MutableLiveEvent<GroupMessageHeader> addedHeader =
            new MutableLiveEvent<>();

    @Inject
    public PrivateGroupConversationViewModel(
            @NonNull Application application,
            @DatabaseExecutor Executor dbExecutor,
            MessagingManager messagingManager,
            ConversationManager conversationManager,
            GroupManager groupManager,
            ContextManager contextManager,
            GroupMessageFactory groupMessageFactory,
            AttachmentRetriever attachmentRetriever) {
        super(application);
        this.attachmentRetriever = attachmentRetriever;
        this.dbExecutor = dbExecutor;
        this.messagingManager = messagingManager;
        this.conversationManager = conversationManager;
        this.groupManager = groupManager;
        this.contextManager = contextManager;
        this.groupMessageFactory = groupMessageFactory;
        groupDisolved.setValue(false);
    }

    /**
     * Setting the {@link ContactId} automatically triggers loading of other
     * data.
     */
    void setGroupId(String groupId) {
        if (this.groupId == null) {
            this.groupId = groupId;
            loadGroup(groupId);
            loadContext(groupId);
        } else if (!groupId.equals(this.groupId)) {
            throw new IllegalStateException();
        }
    }


    private void loadContext(String groupId) {
        dbExecutor.execute(() -> {
            try {
                long start = now();
                Group group = conversationManager.getContactGroup(groupId);
                Context currentContext = contextManager.getContext(group.getContextId());
                context.postValue(currentContext);
                logDuration(LOG, "Loading context", start);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            } catch (FormatException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    void setGroupName(String groupName) {
        if (this.groupName == null) {
            this.groupName = groupName;
        }
    }

    private void loadGroup(String groupId) {
        dbExecutor.execute(() -> {
            try {
                long start = now();
                PrivateGroup g = (PrivateGroup)
                        groupManager.getGroup(groupId, GroupType.PrivateGroup);
                privateGroup.postValue(g);
                logDuration(LOG, "Loading private group", start);
            } catch (NoSuchGroupException e) {
                groupDisolved.postValue(true);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            } catch (FormatException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    void markMessageRead(String groupId, String messageId) {
        dbExecutor.execute(() -> {
            try {
                long start = now();
                conversationManager.setReadFlag(groupId, messageId, true);
                logDuration(LOG, "Marking read", start);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    @UiThread
    void sendMessage(String text, List<AttachmentItem> attachmentItems, long timestamp, Message.Type messageType) {
        requireNonNull(groupId);
        if (messageType == Message.Type.TEXT)
            createMessage(groupId, text, timestamp);
        else {
            List<Attachment> attachments = new ArrayList();
            for (AttachmentItem item : attachmentItems) {
                attachments.add(
                        new Attachment(
                                item.getUri().toString(),
                                item.getStorageURL(),
                                item.getMimeType(),
                                item.getUri().getPath().replaceAll(".*/", ""))
                );
            }
            createMessageWithAttachments(groupId, text, attachments, messageType, timestamp);
        }
    }

    private void createMessage(String groupId, String text,
                               long timestamp) {
        try {
            Pair<String, String> fakeIdentity =
                    groupManager.getFakeIdentity(groupId);
            GroupMessage pgm;
            pgm = groupMessageFactory.createGroupMessage(
                    groupId, requireNonNull(text), timestamp,
                    fakeIdentity.getFirst(), fakeIdentity.getSecond());
            GroupMessageHeader h = (GroupMessageHeader) messagingManager
                    .sendGroupMessage(
                            privateGroup.getValue(),
                            pgm
                    );
            addedHeader.postEvent(h);
        } catch (DbException | FormatException e) {
            throw new AssertionError(e);
        }
    }

    private void createMessageWithAttachments(String groupId, String text, List<Attachment> attachments,
                                              Message.Type messageType, long timestamp) {
        try {
            Pair<String, String> fakeIdentity =
                    groupManager.getFakeIdentity(groupId);
            GroupMessage pgm;
            pgm = groupMessageFactory.createAttachmentMessage(
                    groupId, attachments, messageType, text, timestamp,
                    fakeIdentity.getFirst(), fakeIdentity.getSecond());
            GroupMessageHeader h = (GroupMessageHeader) messagingManager
                    .sendGroupMessage(
                            privateGroup.getValue(),
                            pgm
                    );
            addedHeader.postEvent(h);
        } catch (DbException | FormatException e) {
            throw new AssertionError(e);
        }
    }

    public AttachmentRetriever getAttachmentRetriever() {
        return attachmentRetriever;
    }

    LiveData<PrivateGroup> getPrivateGroup() {
        return privateGroup;
    }

    LiveData<Boolean> isGroupDisolved() {
        return groupDisolved;
    }

    LiveEvent<GroupMessageHeader> getAddedGroupMessage() {
        return addedHeader;
    }

    LiveData<Context> getContext() {
        return context;
    }
}
