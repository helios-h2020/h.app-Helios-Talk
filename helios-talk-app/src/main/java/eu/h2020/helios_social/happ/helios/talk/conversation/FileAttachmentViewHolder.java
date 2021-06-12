package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.vanniktech.emoji.EmojiTextView;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;

public class FileAttachmentViewHolder extends ConversationItemViewHolder {

    private Button openFileButton;
    private EmojiTextView promptTextView;
    private TextView fileNameTextView;
    private final ImageView favourite;

    FileAttachmentViewHolder(View v, ConversationListener listener, boolean isIncoming) {
        super(v, listener, isIncoming);
        openFileButton = v.findViewById(R.id.openButton);
        promptTextView = v.findViewById(R.id.promptText);
        fileNameTextView = v.findViewById(R.id.file_name);
        favourite = v.findViewById(R.id.favourite);
    }

    @Override
    @CallSuper
    void bind(Context ctx, ConversationItem item, boolean selected) {
        FileAttachmentConversationItem fileAttachmentConversationItem = (FileAttachmentConversationItem) item;
        super.bind(ctx, fileAttachmentConversationItem, selected);

        System.out.println("FILEATTACHMENT: " + fileAttachmentConversationItem);

        if (fileAttachmentConversationItem.getAttachmentList().size() > 0) {
            AttachmentItem attachmentItem = fileAttachmentConversationItem.getAttachmentList().get(0);
            fileNameTextView.setText(attachmentItem.getAttachmentName());
            promptTextView.setText(fileAttachmentConversationItem.getPromptText());

            openFileButton.setOnClickListener(v -> {
                if (listener != null) listener.onFileClicked(v, attachmentItem);
            });
        }

        if (favourite != null) {
            if (item.isFavourite())
                favourite.setImageResource(R.drawable.ic_star_enable);
            else
                favourite.setImageResource(R.drawable.ic_star_disable);

            favourite.setOnClickListener(v -> {
                listener.onFavouriteClicked(v, item);
            });
        }

    }
}