package eu.h2020.helios_social.happ.helios.talk.view;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.customview.view.AbsSavedState;
import androidx.lifecycle.LifecycleOwner;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
public class TextAttachmentController extends TextSendController implements ImagePreview.ImagePreviewListener {

    private final Context ctx;
    private final ImagePreview imagePreview;
    private final AttachmentListener attachmentListener;
    private final CompositeSendButton sendButton;

    private final List<Uri> imageUris = new ArrayList<>();
    private final List<AttachmentItem> attachments = new ArrayList();
    private final CharSequence textHint;
    private boolean loadingUris = false;

    public TextAttachmentController(Context ctx, TextInputView v,
                                    ImagePreview imagePreview,
                                    AttachmentListener attachmentListener) {
        super(v, attachmentListener, false);

        this.ctx = ctx;
        this.attachmentListener = attachmentListener;
        this.imagePreview = imagePreview;
        this.imagePreview.setImagePreviewListener(this);

        sendButton = (CompositeSendButton) compositeSendButton;
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
        } else if (imageUris.isEmpty()) {
            sendButton.showProgress(false);
            sendButton.showImageButton(textIsEmpty, sendEnabled);
        } else {
            sendButton.showProgress(false);
            sendButton.showImageButton(false, sendEnabled);
        }
    }

    @Override
    public void onSendEvent() {
        if (canSend()) {
            if (loadingUris) throw new AssertionError();
            listener.onSendClick(textInput.getText(), attachments);
            reset();
        }
    }

    @Override
    protected boolean canSendEmptyText() {
        return !imageUris.isEmpty();
    }

    public void setImagesSupported() {
        sendButton.setImagesSupported();
    }

    private void onImageButtonClicked() {
        Intent intent = getAttachFileIntent();
        if (attachmentListener.getLifecycle().getCurrentState() != DESTROYED) {
            attachmentListener.onAttachImage(intent);
        }
    }

    private Intent getAttachFileIntent() {
        Intent intent = new Intent(SDK_INT >= 19 ?
                ACTION_OPEN_DOCUMENT : ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(CATEGORY_OPENABLE);
        if (SDK_INT >= 19)
            intent.putExtra(EXTRA_MIME_TYPES, AndroidUtils.getSupportedImageContentTypes());
        if (SDK_INT >= 18) intent.putExtra(EXTRA_ALLOW_MULTIPLE, true);
        return intent;
    }

    public void onImageReceived(@Nullable Intent resultData) {
        if (resultData == null) return;
        if (loadingUris || !imageUris.isEmpty()) throw new AssertionError();
        List<Uri> newUris = new ArrayList<>();
        if (resultData.getData() != null) {
            newUris.add(resultData.getData());
            onNewUris(false, newUris);
        } else if (SDK_INT >= 18 && resultData.getClipData() != null) {
            ClipData clipData = resultData.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                newUris.add(clipData.getItemAt(i).getUri());
            }
            onNewUris(false, newUris);
        }
    }

    private void onNewUris(boolean restart, List<Uri> newUris) {
        if (newUris.isEmpty()) return;
        if (loadingUris) throw new AssertionError();
        loadingUris = true;
        if (newUris.size() > MAX_IMAGE_ATTACHMENTS_PER_MESSAGE) {
            newUris = newUris.subList(0, MAX_IMAGE_ATTACHMENTS_PER_MESSAGE);
            attachmentListener.onTooManyAttachments();
        }
        imageUris.addAll(newUris);
        updateViewState();
        textInput.setHint(R.string.image_caption_hint);
        List<ImagePreviewItem> items = ImagePreviewItem.fromUris(imageUris);
        imagePreview.showPreview(items);

        for (Uri imageUri : imageUris) {
            AttachmentItem imageAttachmentItem = new AttachmentItem(imageUri, ctx.getContentResolver().getType(imageUri));
            attachments.add(imageAttachmentItem);
            imagePreview.loadPreviewImage(imageAttachmentItem);

        }
        loadingUris = false;
    }

    private void reset() {
        // restore hint
        textInput.setHint(textHint);
        // hide image layout
        imagePreview.setVisibility(View.GONE);
        // reset image URIs
        imageUris.clear();
        //reset attachments
        attachments.clear();
        // definitely not loading anymore
        loadingUris = false;
        // show the image button again, so images can get attached
        updateViewState();
    }

    @Override
    public Parcelable onSaveInstanceState(@Nullable Parcelable superState) {
        SavedState state =
                new SavedState(superState == null ? EMPTY_STATE : superState);
        state.imageUris = imageUris;
        return state;
    }

    @Override
    @Nullable
    public Parcelable onRestoreInstanceState(Parcelable inState) {
        SavedState state = (SavedState) inState;
        if (!imageUris.isEmpty()) throw new AssertionError();
        if (state.imageUris != null) onNewUris(true, state.imageUris);
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
        private List<Uri> imageUris;

        private SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            //noinspection unchecked
            imageUris = in.readArrayList(Uri.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(imageUris);
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

        void onTooManyAttachments();
    }
}
