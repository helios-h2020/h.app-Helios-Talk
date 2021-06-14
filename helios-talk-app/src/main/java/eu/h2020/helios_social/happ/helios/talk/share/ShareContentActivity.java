package eu.h2020.helios_social.happ.helios.talk.share;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.context.ContextController;
import eu.h2020.helios_social.happ.helios.talk.context.Themes;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.view.HeliosTalkRecyclerView;
import eu.h2020.helios_social.modules.groupcommunications.api.attachment.InvalidAttachmentException;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.context.DBContext;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.Forum;
import eu.h2020.helios_social.modules.groupcommunications.api.group.Group;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupType;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Attachment;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.GroupCount;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.ShareContentType;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.PrivateGroup;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;
import static java.util.logging.Level.WARNING;

public class ShareContentActivity extends HeliosTalkActivity implements ShareContentListener, AdapterView.OnItemSelectedListener {
    private static Logger LOG = Logger.getLogger(ShareContentActivity.class.getName());

    private ShareContentChatListAdapter adapter;
    private ContextListAdapter contextListAdapter;
    private HeliosTalkRecyclerView list;
    private ShareContentItem shareContentItem;
    private Spinner contextSpinner;
    private List<ContextItem> contextItems = new ArrayList<>();
    private String currentContext;
    private Integer contextColor;


    @Inject
    volatile ContactManager contactManager;
    @Inject
    volatile ConversationManager conversationManager;
    @Inject
    volatile GroupManager groupManager;
    @Inject
    ShareContentController shareContentController;
    @Inject
    ContextManager contextManager;
    @Inject
    ContextController contextController;

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_share_content);

        setTitle("Send To");

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            } else if (type.startsWith("video/")) {
                handleSendVideo(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        }

        currentContext = "All";
        adapter = new ShareContentChatListAdapter(this, this);
        list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        list.setEmptyImage(R.drawable.ic_no_chats_illustration);
        list.setEmptyTitle(R.string.no_conversations);
        list.setEmptyText(R.string.no_conversations_in_context_details);
        contextSpinner = findViewById(R.id.selectedContext);
        contextSpinner.setOnItemSelectedListener(this);
        contextListAdapter = new ContextListAdapter(this, contextItems);
        contextSpinner.setAdapter(contextListAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadContexts();
        loadChats(currentContext);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.clear();
        list.showProgressBar();
        list.stopPeriodicUpdate();
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            shareContentItem = new ShareContentItem(ShareContentType.TEXT, sharedText);
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (imageUri != null) {
            Attachment attachment = new Attachment(imageUri.toString(), null, getContentResolver().getType(imageUri), imageUri.getPath().replaceAll(".*/", ""));
            List<Attachment> attachments = new ArrayList<>();
            attachments.add(attachment);
            shareContentItem = new ShareContentItem(ShareContentType.IMAGES, attachments, sharedText);
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (imageUris != null) {
            List<Attachment> attachments = new ArrayList<>();
            for (Uri imageUri : imageUris) {
                attachments.add(new Attachment(
                        imageUri.toString(),
                        null,
                        getContentResolver().getType(imageUri),
                        imageUri.getPath().replaceAll(".*/", ""))
                );
            }
            shareContentItem = new ShareContentItem(ShareContentType.IMAGES, attachments, sharedText);
        }
    }

    void handleSendVideo(Intent intent) {
        Uri videoUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (videoUri != null) {
            List<Attachment> attachments = new ArrayList<>();
            attachments.add(new Attachment(
                    videoUri.toString(),
                    null,
                    getContentResolver().getType(videoUri),
                    videoUri.getPath().replaceAll(".*/", ""))
            );
            shareContentItem = new ShareContentItem(ShareContentType.VIDEO, attachments, sharedText);
        }
    }

    private void loadContexts() {
        contextItems.clear();
        runOnDbThread(() -> {
            try {
                Collection<DBContext> contexts = contextManager.getContexts();
                for (DBContext context : contexts) {
                    contextItems.add(new ContextItem(context.getId(), context.getName(), context.getColor()));
                }
                contextListAdapter.notifyDataSetChanged();
                int index = contextItems.indexOf(new ContextItem("All", "All", 0));
                contextSpinner.setSelection(index, true);
                contextColor = contextItems.get(index).getColor();
            } catch (DbException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadChats(String currentContext) {
        int revision = adapter.getRevision();
        runOnDbThread(() -> {
            try {
                long start = now();
                List<ShareContentChatItem> chats = new ArrayList<>();

                for (Contact c : contactManager.getContacts(currentContext)) {
                    Group group = conversationManager.getContactGroup(c.getId(), currentContext);
                    GroupCount groupCount = conversationManager.getGroupCount(group.getId());
                    ShareContentContactChatItem item =
                            new ShareContentContactChatItem(c, group);
                    item.setLstTimestamp(groupCount.getLatestMsgTime());
                    chats.add(item);
                }

                Collection<Group> groups = groupManager.getGroups(currentContext);
                for (Group group : groups) {
                    GroupCount groupCount = conversationManager.getGroupCount(group.getId());
                    ShareContentGroupChatItem item;
                    if (group.getGroupType().equals(GroupType.PrivateGroup)) {
                        item = new ShareContentGroupChatItem((PrivateGroup) group);
                    } else {
                        item = new ShareContentGroupChatItem((Forum) group);
                    }
                    item.setLstTimestamp(groupCount.getLatestMsgTime());
                    chats.add(item);
                }
                logDuration(LOG, "Chats Full load", start);
                displayChats(revision, chats);
            } catch (DbException | FormatException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    private void displayChats(int revision, List<ShareContentChatItem> chats) {
        runOnUiThreadUnlessDestroyed(() -> {
            if (revision == adapter.getRevision()) {
                adapter.incrementRevision();
                if (chats.isEmpty()) {
                    list.showData();
                } else {
                    adapter.replaceAll(chats);
                }
            } else {
                loadChats(currentContext);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getSupportActionBar() != null)
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.helios_default_context_color)));
    }

    @Override
    public void onShareContent(ShareContentChatItem item) {
        //deep copy of share content item is necessary to retain attachment list intact
        ShareContentItem shareContentItem = this.shareContentItem.clone();
        if (shareContentItem == null) {
            Toast.makeText(ShareContentActivity.this,
                           "Share Content Failed!",
                           Toast.LENGTH_LONG).show();
        }
        if (item instanceof ShareContentContactChatItem) {
            ShareContentContactChatItem chatItem = (ShareContentContactChatItem) item;
            if (shareContentItem.getContentType() == ShareContentType.TEXT
                    && shareContentItem != null
                    && shareContentItem.getText() != null) {

                shareContentController.shareText(
                        chatItem.getContact().getId(),
                        chatItem.getGroupId(),
                        chatItem.getContextId(),
                        shareContentItem.getText(),
                        new UiResultExceptionHandler<Void, Exception>(this) {

                            @Override
                            public void onResultUi(Void v) {
                                Toast.makeText(ShareContentActivity.this,
                                               "Text Shared Successfully",
                                               Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onExceptionUi(Exception exception) {
                                if (exception instanceof DbException)
                                    handleDbException((DbException) exception);
                                else
                                    LOG.warning(exception.getMessage());
                            }
                        });

            } else if (shareContentItem.getContentType() == ShareContentType.IMAGES
                    && shareContentItem != null
                    && shareContentItem.getAttachments().size() > 0) {

                shareContentController.shareAttachments(
                        chatItem.getContact().getId(),
                        chatItem.getGroupId(),
                        chatItem.getContextId(),
                        shareContentItem.getAttachments(),
                        shareContentItem.getText(),
                        Message.Type.IMAGES,
                        new UiResultExceptionHandler<Void, Exception>(this) {

                            @Override
                            public void onResultUi(Void v) {
                                Toast.makeText(ShareContentActivity.this,
                                               "Images Shared Successfully",
                                               Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onExceptionUi(Exception exception) {
                                if (exception instanceof DbException)
                                    handleDbException((DbException) exception);
                                else if (exception instanceof InvalidAttachmentException)
                                    Toast.makeText(
                                            ShareContentActivity.this,
                                            exception.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                else

                                    LOG.warning(exception.getMessage());
                            }
                        });
            } else if (shareContentItem.getContentType() == ShareContentType.VIDEO
                    && shareContentItem != null
                    && shareContentItem.getAttachments().size() == 1) {
                shareContentController.shareAttachments(
                        chatItem.getContact().getId(),
                        chatItem.getGroupId(),
                        chatItem.getContextId(),
                        shareContentItem.getAttachments(),
                        shareContentItem.getText(),
                        Message.Type.FILE_ATTACHMENT,
                        new UiResultExceptionHandler<Void, Exception>(this) {

                            @Override
                            public void onResultUi(Void v) {
                                Toast.makeText(ShareContentActivity.this,
                                               "Video Shared Successfully",
                                               Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onExceptionUi(Exception exception) {
                                if (exception instanceof DbException)
                                    handleDbException((DbException) exception);
                                else if (exception instanceof InvalidAttachmentException)
                                    Toast.makeText(
                                            ShareContentActivity.this,
                                            exception.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                else

                                    LOG.warning(exception.getMessage());
                            }
                        });
            }
        } else {
            ShareContentGroupChatItem chatItem = (ShareContentGroupChatItem) item;
            if (shareContentItem.getContentType() == ShareContentType.TEXT
                    && shareContentItem != null
                    && shareContentItem.getText() != null) {

                shareContentController.shareText(
                        chatItem.getGroup(),
                        shareContentItem.getText(),
                        new UiResultExceptionHandler<Void, Exception>(this) {

                            @Override
                            public void onResultUi(Void v) {
                                Toast.makeText(ShareContentActivity.this,
                                               "Text Shared Successfully",
                                               Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onExceptionUi(Exception exception) {
                                if (exception instanceof DbException)
                                    handleDbException((DbException) exception);
                                else
                                    LOG.warning(exception.getMessage());
                            }
                        });

            } else if (shareContentItem.getContentType() == ShareContentType.IMAGES
                    && shareContentItem != null
                    && shareContentItem.getAttachments().size() > 0) {

                shareContentController.shareAttachments(
                        chatItem.getGroup(),
                        shareContentItem.getAttachments(),
                        shareContentItem.getText(),
                        Message.Type.IMAGES,
                        new UiResultExceptionHandler<Void, Exception>(this) {

                            @Override
                            public void onResultUi(Void v) {
                                Toast.makeText(ShareContentActivity.this,
                                               "Images Shared Successfully",
                                               Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onExceptionUi(Exception exception) {
                                if (exception instanceof DbException)
                                    handleDbException((DbException) exception);
                                else if (exception instanceof InvalidAttachmentException)
                                    Toast.makeText(
                                            ShareContentActivity.this,
                                            exception.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                else
                                    LOG.warning(exception.getMessage());
                            }
                        });
            } else if (shareContentItem.getContentType() == ShareContentType.VIDEO
                    && shareContentItem != null
                    && shareContentItem.getAttachments().size() == 1) {

                shareContentController.shareAttachments(
                        chatItem.getGroup(),
                        shareContentItem.getAttachments(),
                        shareContentItem.getText(),
                        Message.Type.FILE_ATTACHMENT,
                        new UiResultExceptionHandler<Void, Exception>(this) {

                            @Override
                            public void onResultUi(Void v) {
                                Toast.makeText(ShareContentActivity.this,
                                               "Video Shared Successfully",
                                               Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onExceptionUi(Exception exception) {
                                if (exception instanceof DbException)
                                    handleDbException((DbException) exception);
                                else if (exception instanceof InvalidAttachmentException)
                                    Toast.makeText(
                                            ShareContentActivity.this,
                                            exception.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                else
                                    LOG.warning(exception.getMessage());
                            }
                        });
            }
        }
    }

    boolean validateURL(String text) {
        String urlRegex = "((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentContext = contextItems.get(position).getId();
        if (currentContext.equals("All")) {
            setTheme(R.style.HeliosTheme);
        } else {
            Themes themes = new Themes(this);
            Integer color = contextItems.get(position).getColor();
            setTheme(themes.getTheme(color));
        }
        loadChats(currentContext);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
