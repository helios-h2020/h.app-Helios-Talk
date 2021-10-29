package eu.h2020.helios_social.happ.helios.talk.group;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationListener;
import eu.h2020.helios_social.happ.helios.talk.view.AuthorView;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import static eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils.trim;

public abstract class GroupConversationItemViewHolder extends ViewHolder implements View.OnCreateContextMenuListener {

    protected final ConversationListener listener;
    private final View root;
    protected final ConstraintLayout layout;
    protected final TextView text;
    protected final AuthorView author;
    protected final ImageView favourite;
    //protected final ImageButton addContactButton;
    @Nullable
    private String itemKey = null;

    GroupConversationItemViewHolder(View v, ConversationListener listener) {
        super(v);
        this.listener = listener;
        root = v;
        layout = v.findViewById(R.id.layout);
        text = v.findViewById(R.id.text);
        author = v.findViewById(R.id.author);
        favourite = v.findViewById(R.id.favourite);
        //addContactButton = v.findViewById(R.id.add_contact_btn);
    }

    @CallSuper
    void bind(Context ctx, GroupMessageItem item, boolean selected) {
        itemKey = item.getKey();
        root.setActivated(selected);
        author.setAuthor(item.getPeerInfo());
        long timestamp = item.getTime();
        author.setDate(timestamp);
        if (item.getPeerInfo().getAlias() != null) {
            author.setOnCreateContextMenuListener(this);
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

        if (item.getText() != null) {
            text.setText(trim(item.getText()));
        }

    }

    @Nullable
    String getItemKey() {
        return itemKey;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, R.id.action_send_connection_request,
                 Menu.NONE, R.string.send_connection_request);
    }
}
