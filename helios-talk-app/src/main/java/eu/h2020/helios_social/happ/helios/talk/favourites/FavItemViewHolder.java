package eu.h2020.helios_social.happ.helios.talk.favourites;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationListener;
import eu.h2020.helios_social.happ.helios.talk.conversation.ImageAdapter;
import eu.h2020.helios_social.happ.helios.talk.conversation.ImageItemDecoration;
import eu.h2020.helios_social.happ.helios.talk.group.GroupMessageItem;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.happ.helios.talk.view.AuthorView;


public class FavItemViewHolder extends RecyclerView.ViewHolder {

    private final ImageAdapter adapter;
    protected final ConstraintLayout layout;
    protected final AuthorView author;
    protected final TextView text;
    protected final ImageView favourite;
    @Nullable
    private String itemKey = null;
    private RecyclerView list;
    private final ConstraintSet layoutConstraints = new ConstraintSet();
    private final LinearLayout attachmentsPreview;

    public FavItemViewHolder(View v, RecyclerView.RecycledViewPool imageViewPool,
                             ImageItemDecoration imageItemDecoration) {
        super(v);
        layout = v.findViewById(R.id.layout);
        text = v.findViewById(R.id.text);
        author = v.findViewById(R.id.author);
        favourite = v.findViewById(R.id.favourite);
        attachmentsPreview = v.findViewById(R.id.attachmentsPreview);

        // image list
        list = v.findViewById(R.id.imageList);
        list.setRecycledViewPool(imageViewPool);
        layoutConstraints.clone(v.getContext(),
                R.layout.list_item_group_conversation_msg);
        adapter = new ImageAdapter(v.getContext(), null);
        list.setAdapter(adapter);
        list.addItemDecoration(imageItemDecoration);
    }

    @CallSuper
    void bind(Context ctx, FavItem item) {
        itemKey = item.getKey();
        author.setAuthor(item.getAuthor());
        long timestamp = item.getTime();
        author.setDate(timestamp);

        if (item.getAttachmentList().size() > 0) {
            if (item.getText() == null) {
                text.setVisibility(View.GONE);
            } else {
                text.setText(item.getText());
                text.setVisibility(View.VISIBLE);
            }

            adapter.setConversationItem(item);
            layoutConstraints.applyTo(layout);
            attachmentsPreview.setVisibility(View.VISIBLE);
        } else {
            text.setText(item.getText());
            attachmentsPreview.setVisibility(View.GONE);
            adapter.clear();
            layoutConstraints.applyTo(layout);
        }

    }

    @Nullable
    String getItemKey() {
        return itemKey;
    }

}
