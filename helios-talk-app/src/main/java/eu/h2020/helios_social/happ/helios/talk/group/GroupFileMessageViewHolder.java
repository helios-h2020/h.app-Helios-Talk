package eu.h2020.helios_social.happ.helios.talk.group;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationListener;

public class GroupFileMessageViewHolder extends GroupConversationItemViewHolder {

    private Button openFileButton;
    private TextView fileNameTextView;
    private TextView promptText;
    private final View root;

    GroupFileMessageViewHolder(View v, ConversationListener listener) {
        super(v, listener);
        root = v;
        openFileButton = v.findViewById(R.id.openButton);
        fileNameTextView = v.findViewById(R.id.file_name);
        promptText = v.findViewById(R.id.promptText);
    }

    @Override
    @CallSuper
    void bind(Context ctx, GroupMessageItem item, boolean selected) {
        super.bind(ctx, item, selected);
        root.setActivated(selected);

        if (item.getAttachmentList().size() > 0) {
            promptText.setText(R.string.file_attachment_shared);
            AttachmentItem attachmentItem = item.getAttachmentList().get(0);
            fileNameTextView.setText(attachmentItem.getAttachmentName());

            openFileButton.setOnClickListener(v -> {
                if (listener != null) listener.onFileClicked(v, attachmentItem);
            });
        }

    }
}
