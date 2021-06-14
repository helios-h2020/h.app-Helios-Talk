package eu.h2020.helios_social.happ.helios.talk.view;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.FileProvider;
import androidx.customview.view.AbsSavedState;
import androidx.lifecycle.LifecycleOwner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog.Builder;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.content.Intent.CATEGORY_OPENABLE;
import static android.content.Intent.EXTRA_ALLOW_MULTIPLE;
import static android.content.Intent.EXTRA_MIME_TYPES;
import static android.os.Build.VERSION.SDK_INT;
import static android.widget.Toast.LENGTH_LONG;
import static androidx.customview.view.AbsSavedState.EMPTY_STATE;
import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingConstants.MAX_IMAGE_ATTACHMENTS_PER_MESSAGE;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.android.util.AndroidUtils;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationViewModel;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
public class TextAttachmentController extends TextSendController implements ImagePreview.ImagePreviewListener, FileAttachmentPreview.FileAttachmentPreviewListener {

    private final Context ctx;
    private final ImagePreview imagePreview;
    private final FileAttachmentPreview fileAttachmentPreview;
    private final AttachmentListener attachmentListener;
    private final CompositeSendButton sendButton;
    private final AppCompatImageButton fileAttachmentButton;
    private final AppCompatImageButton capturePhotoButton;

    private final List<Uri> uris = new ArrayList<>();
    private final List<AttachmentItem> attachments = new ArrayList();
    private final CharSequence textHint;
    private boolean loadingUris = false;
    private Message.Type messageType = Message.Type.TEXT;
    private Uri capturedPhotoUri;

    public TextAttachmentController(Context ctx, TextInputView v,
                                    ImagePreview imagePreview,
                                    FileAttachmentPreview fileAttachmentPreview,
                                    AttachmentListener attachmentListener) {
        super(v, attachmentListener, false);

        this.ctx = ctx;
        this.attachmentListener = attachmentListener;
        this.imagePreview = imagePreview;
        this.imagePreview.setImagePreviewListener(this);

        this.fileAttachmentPreview = fileAttachmentPreview;
        this.fileAttachmentPreview.setFileAttachmentPreviewListener(this);

        sendButton = (CompositeSendButton) compositeSendButton;
        fileAttachmentButton = v.findViewById(R.id.attachFileButton);
        fileAttachmentButton.setOnClickListener(l -> onFileAttachmentButtonClicked());
        capturePhotoButton = v.findViewById(R.id.captureFromCameraButton);
        capturePhotoButton.setOnClickListener(l -> onCapturePhotoClicked());
        sendButton.setOnImageClickListener(view -> onImageButtonClicked());

        textHint = textInput.getHint();
    }

    @Override
    protected void updateViewState() {
        textInput.setEnabled(ready && !loadingUris);
        boolean sendEnabled = ready && !loadingUris &&
                (!textIsEmpty || canSendEmptyText());
        if (loadingUris) {
            sendButton.showProgress(true);
        } else if (uris.isEmpty()) {
            sendButton.showProgress(false);
            sendButton.showImageButton(textIsEmpty, sendEnabled);
            if (textIsEmpty) {
                fileAttachmentButton.setVisibility(View.VISIBLE);
                capturePhotoButton.setVisibility(View.VISIBLE);
            } else {
                fileAttachmentButton.setVisibility(View.GONE);
                capturePhotoButton.setVisibility(View.GONE);
            }
        } else {
            sendButton.showProgress(false);
            sendButton.showImageButton(false, sendEnabled);
            fileAttachmentButton.setVisibility(View.GONE);
            capturePhotoButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSendEvent() {
        if (canSend()) {
            if (loadingUris) throw new AssertionError();
            listener.onSendClick(textInput.getText(), attachments, messageType);
            reset();
        }
    }

    @Override
    protected boolean canSendEmptyText() {
        return !uris.isEmpty();
    }

    public void setImagesSupported() {
        sendButton.setImagesSupported();
    }

    private void onImageButtonClicked() {
        Intent intent = getAttachImagesIntent();
        if (attachmentListener.getLifecycle().getCurrentState() != DESTROYED) {
            attachmentListener.onAttachImage(intent);
        }
    }

    private void onFileAttachmentButtonClicked() {
        Intent intent = getAttachFileIntent();
        if (attachmentListener.getLifecycle().getCurrentState() != DESTROYED) {
            attachmentListener.onAttachFile(intent);
        }
    }

    private void onCapturePhotoClicked() {
        Intent intent = getAttachCapturedPhotoIntent();
        if (attachmentListener.getLifecycle().getCurrentState() != DESTROYED) {
            attachmentListener.onAttachCapturedPhoto(intent);
        }
    }

    private Intent getAttachImagesIntent() {
        Intent intent = new Intent(SDK_INT >= 19 ?
                                           ACTION_OPEN_DOCUMENT : ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(CATEGORY_OPENABLE);
        if (SDK_INT >= 19)
            intent.putExtra(EXTRA_MIME_TYPES, AndroidUtils.getSupportedImageContentTypes());
        if (SDK_INT >= 18) intent.putExtra(EXTRA_ALLOW_MULTIPLE, true);
        return intent;
    }

    private Intent getAttachCapturedPhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(ctx.getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                File photoFile = createImageFile();
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    capturedPhotoUri = FileProvider
                            .getUriForFile(
                                    ctx,
                                    ctx.getApplicationContext().getPackageName() + ".provider",
                                    photoFile
                            );
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoUri);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return takePictureIntent;
    }

    private Intent getAttachFileIntent() {
        Intent intent = new Intent(SDK_INT >= 19 ?
                                           ACTION_OPEN_DOCUMENT : ACTION_GET_CONTENT);
        String[] mimeTypes = {"text/*", "video/*", "application/pdf"};
        intent.setType("*/*");
        intent.addCategory(CATEGORY_OPENABLE);
        if (SDK_INT >= 19)
            intent.putExtra(EXTRA_MIME_TYPES, mimeTypes);
        if (SDK_INT >= 18) intent.putExtra(EXTRA_ALLOW_MULTIPLE, false);
        return intent;
    }

    /**
     * @return A temp file. The captured image will be saved on it.
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

    public void onAttachmentsReceived(@Nullable Intent resultData, Message.Type messageType) {
        this.messageType = messageType;
        if (loadingUris || !uris.isEmpty()) throw new AssertionError();
        List<Uri> newUris = new ArrayList<>();
        if (capturedPhotoUri != null) {
            newUris.add(capturedPhotoUri);
            onNewUris(false, newUris, messageType);
        } else if (resultData.getData() != null) {
            newUris.add(resultData.getData());
            onNewUris(false, newUris, messageType);
        } else if (SDK_INT >= 18 && resultData.getClipData() != null) {
            ClipData clipData = resultData.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                newUris.add(clipData.getItemAt(i).getUri());
            }
            onNewUris(false, newUris, messageType);
        }
    }

    private void onNewUris(boolean restart, List<Uri> newUris, Message.Type messageType) {
        if (newUris.isEmpty()) return;
        if (loadingUris) throw new AssertionError();
        loadingUris = true;
        if (messageType == Message.Type.IMAGES) {
            if (newUris.size() > MAX_IMAGE_ATTACHMENTS_PER_MESSAGE) {
                newUris = newUris.subList(0, MAX_IMAGE_ATTACHMENTS_PER_MESSAGE);
                attachmentListener.onTooManyImageAttachments();
            }
            uris.addAll(newUris);
            updateViewState();
            textInput.setHint(R.string.image_caption_hint);

            List<ImagePreviewItem> items = ImagePreviewItem.fromUris(uris);
            imagePreview.showPreview(items);

            for (Uri imageUri : uris) {
                AttachmentItem imageAttachmentItem = new AttachmentItem(imageUri, ctx.getContentResolver().getType(imageUri));
                attachments.add(imageAttachmentItem);
                imagePreview.loadPreviewImage(imageAttachmentItem);
            }
        } else if (messageType == Message.Type.FILE_ATTACHMENT) {
            uris.addAll(newUris);
            updateViewState();
            textInput.setHint(R.string.image_caption_hint);

            Uri fileUri = uris.get(0);
            AttachmentItem attachmentItem = new AttachmentItem(fileUri, ctx.getContentResolver().getType(fileUri));
            attachments.add(attachmentItem);
            fileAttachmentPreview.showPreview(new FileAttachmentPreviewItem(fileUri));
        }
        loadingUris = false;
    }

    private void reset() {
        // restore hint
        textInput.setHint(textHint);
        // hide image layout
        imagePreview.setVisibility(View.GONE);
        // hide file attachment layout
        fileAttachmentPreview.setVisibility(View.GONE);
        //reset current captured photo uri to null
        capturedPhotoUri = null;
        // reset attachment URIs
        uris.clear();
        //reset attachments
        attachments.clear();
        //reset message type as text
        messageType = Message.Type.TEXT;
        // definitely not loading anymore
        loadingUris = false;
        // show the image button again, so images can get attached
        updateViewState();
    }

    @Override
    public Parcelable onSaveInstanceState(@Nullable Parcelable superState) {
        SavedState state =
                new SavedState(superState == null ? EMPTY_STATE : superState);
        state.uris = uris;
        state.messageType = messageType;
        return state;
    }

    @Override
    @Nullable
    public Parcelable onRestoreInstanceState(Parcelable inState) {
        SavedState state = (SavedState) inState;
        if (!uris.isEmpty()) throw new AssertionError();
        if (state.uris != null) onNewUris(true, state.uris, state.messageType);
        return state.getSuperState();
    }

    @UiThread
    private void onError(@Nullable String errorMsg) {
        if (errorMsg == null) {
            errorMsg = imagePreview.getContext()
                    .getString(R.string.image_attach_error);
        }
        Toast.makeText(textInput.getContext(), errorMsg, LENGTH_LONG).show();
        onCancel();
    }

    @Override
    public void onCancel() {
        textInput.clearText();
        //attachmentManager.cancel();
        reset();
    }


    private static class SavedState extends AbsSavedState {

        @Nullable
        private List<Uri> uris;
        private Message.Type messageType;

        private SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            //noinspection unchecked
            uris = in.readArrayList(Uri.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(uris);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @UiThread
    public interface AttachmentListener extends SendListener, LifecycleOwner {

        void onAttachImage(Intent intent);

        void onAttachCapturedPhoto(Intent intent);

        void onAttachFile(Intent intent);

        void onTooManyImageAttachments();
    }
}
