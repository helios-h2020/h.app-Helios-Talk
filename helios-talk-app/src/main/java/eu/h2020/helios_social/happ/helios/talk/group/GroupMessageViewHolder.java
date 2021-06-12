package eu.h2020.helios_social.happ.helios.talk.group;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationListener;
import eu.h2020.helios_social.happ.helios.talk.conversation.ImageAdapter;
import eu.h2020.helios_social.happ.helios.talk.conversation.ImageItemDecoration;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.view.AuthorView;


@UiThread
@NotNullByDefault
public class GroupMessageViewHolder extends GroupConversationItemViewHolder implements View.OnCreateContextMenuListener {

    private final ImageAdapter adapter;
    private final View root;
    @Nullable
    protected String itemKey = null;
    private RecyclerView list;
    private final ConstraintSet layoutConstraints = new ConstraintSet();
    private final LinearLayout attachmentsPreview;

    GroupMessageViewHolder(View v, ConversationListener listener, RecyclerView.RecycledViewPool imageViewPool,
                           ImageItemDecoration imageItemDecoration) {
        super(v, listener);
        root = v;
        attachmentsPreview = v.findViewById(R.id.attachmentsPreview);

        // image list
        list = v.findViewById(R.id.imageList);
        list.setRecycledViewPool(imageViewPool);
        layoutConstraints.clone(v.getContext(),
                                R.layout.list_item_group_conversation_msg);
        adapter = new ImageAdapter(v.getContext(), listener);
        list.setAdapter(adapter);
        list.addItemDecoration(imageItemDecoration);
    }

    @CallSuper
    void bind(Context ctx, GroupMessageItem item, boolean selected) {
        super.bind(ctx, item, selected);
        root.setActivated(selected);

        if (item.getAttachmentList().size() > 0) {
            text.setText(item.getText());
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
}
