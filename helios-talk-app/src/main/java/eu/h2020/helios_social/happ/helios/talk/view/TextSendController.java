package eu.h2020.helios_social.happ.helios.talk.view;

import android.net.Uri;
import android.os.Parcelable;
import android.view.View;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.util.Collections;
import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
public class TextSendController implements
        EmojiTextInputView.TextInputListener {

    protected final EmojiTextInputView textInput;
    protected final View compositeSendButton;
    protected final SendListener listener;

    protected boolean ready = true, textIsEmpty = true;

    private final boolean allowEmptyText;

    public TextSendController(TextInputView v, SendListener listener,
                              boolean allowEmptyText) {
        this.compositeSendButton = v.findViewById(R.id.compositeSendButton);
        this.compositeSendButton.setOnClickListener(view -> onSendEvent());
        this.listener = listener;
        this.textInput = v.getEmojiTextInputView();
        this.allowEmptyText = allowEmptyText;
    }

    @Override
    public void onTextIsEmptyChanged(boolean isEmpty) {
        textIsEmpty = isEmpty;
        updateViewState();
    }

    @Override
    public void onSendEvent() {
        if (canSend()) {
            listener.onSendClick(textInput.getText(), Collections.emptyList(), Message.Type.TEXT);
        }
    }

    public void setReady(boolean ready) {
        this.ready = ready;
        updateViewState();
    }

    public void setEnabled(boolean enabled) {
        textInput.setEnabled(enabled);
        compositeSendButton.setEnabled(enabled);
    }

    protected void updateViewState() {
        textInput.setEnabled(ready);
        compositeSendButton
                .setEnabled(ready && (!textIsEmpty || canSendEmptyText()));
    }

    protected final boolean canSend() {
        if (textInput.isTooLong()) {
            Snackbar.make(compositeSendButton, R.string.text_too_long,
                          BaseTransientBottomBar.LENGTH_SHORT).show();
            return false;
        }
        return ready && (canSendEmptyText() || !textIsEmpty);
    }

    protected boolean canSendEmptyText() {
        return allowEmptyText;
    }

    @Nullable
    public Parcelable onSaveInstanceState(@Nullable Parcelable superState) {
        return superState;
    }

    @Nullable
    public Parcelable onRestoreInstanceState(Parcelable state) {
        return state;
    }

    @UiThread
    public interface SendListener {
        void onSendClick(@Nullable String text, List<AttachmentItem> attachments, Message.Type messageType);
    }

}
