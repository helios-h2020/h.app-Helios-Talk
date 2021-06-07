package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.CallSuper;

import eu.h2020.helios_social.happ.helios.talk.R;

public class SharedContactViewHolder extends ConversationItemViewHolder {

    private Button sharedContactName;

    SharedContactViewHolder(View v, ConversationListener listener, boolean isIncoming) {
        super(v, listener, isIncoming);
        sharedContactName = v.findViewById(R.id.contact_name);
    }

    @Override
    @CallSuper
    void bind(Context ctx, ConversationItem item, boolean selected) {
        SharedContactConversationItem notice = (SharedContactConversationItem) item;
        super.bind(ctx, notice, selected);

        text.setText(notice.getPromptText());

        if (notice.getPeerInfo() != null) {
            sharedContactName.setText(notice.getPeerInfo().getAlias());
            if (isIncoming())
                sharedContactName.setOnClickListener(v -> {
                    if (listener != null) listener.onSharedContactClicked(v, item);
                });
        }
    }
}
