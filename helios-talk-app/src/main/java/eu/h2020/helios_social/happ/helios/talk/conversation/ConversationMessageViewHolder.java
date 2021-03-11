package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import static androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT;
import static androidx.core.content.ContextCompat.getColor;

@UiThread
@NotNullByDefault
class ConversationMessageViewHolder extends ConversationItemViewHolder {

    private final ImageAdapter adapter;
    private final ViewGroup statusLayout;
    private final ImageView favourite;
    private final int timeColor, timeColorBubble;
    private final ConstraintSet textConstraints = new ConstraintSet();
    private final ConstraintSet imageTextConstraints = new ConstraintSet();

    ConversationMessageViewHolder(View v, ConversationListener listener,
                                  boolean isIncoming, RecyclerView.RecycledViewPool imageViewPool,
                                  ImageItemDecoration imageItemDecoration) {
        super(v, listener, isIncoming);
        statusLayout = v.findViewById(R.id.statusLayout);
        favourite = v.findViewById(R.id.favourite);

        // image list
        RecyclerView list = v.findViewById(R.id.imageList);
        list.setRecycledViewPool(imageViewPool);
        adapter = new ImageAdapter(v.getContext(), listener);
        list.setAdapter(adapter);
        list.addItemDecoration(imageItemDecoration);

        // remember original status text color
        timeColor = time.getCurrentTextColor();
        timeColorBubble = getColor(v.getContext(), R.color.white);

        // clone constraint sets from layout files
        textConstraints.clone(v.getContext(),
                R.layout.list_item_conversation_msg_in_content);
        imageTextConstraints.clone(v.getContext(),
                R.layout.list_item_conversation_msg_image_text);

        // in/out are different layouts, so we need to do this only once
        textConstraints
                .setHorizontalBias(R.id.statusLayout, isIncoming() ? 1 : 0);
        imageTextConstraints
                .setHorizontalBias(R.id.statusLayout, isIncoming() ? 1 : 0);
    }

    @Override
    void bind(Context ctx, ConversationItem conversationItem,
              boolean selected) {
        super.bind(ctx, conversationItem, selected);
        ConversationMessageItem item =
                (ConversationMessageItem) conversationItem;
        if (item.getAttachmentList().isEmpty()) {
            bindTextItem();
        } else {
            bindImageItem(item);
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

    private void bindTextItem() {
        resetStatusLayoutForText();
        textConstraints.applyTo(layout);
        adapter.clear();
    }

    private void bindImageItem(ConversationMessageItem item) {
        ConstraintSet constraintSet;
        resetStatusLayoutForText();
        constraintSet = imageTextConstraints;

        // bubble adapts to size of image list
        constraintSet.constrainWidth(R.id.imageList, WRAP_CONTENT);
        constraintSet.constrainHeight(R.id.imageList, WRAP_CONTENT);

        constraintSet.applyTo(layout);
        adapter.setConversationItem(item);
    }

    private void resetStatusLayoutForText() {
        statusLayout.setBackgroundResource(0);
        // also reset padding (the background drawable defines some)
        statusLayout.setPadding(0, 0, 0, 0);
        time.setTextColor(timeColor);
    }

}
