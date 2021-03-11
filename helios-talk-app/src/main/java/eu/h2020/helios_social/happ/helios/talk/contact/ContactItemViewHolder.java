package eu.h2020.helios_social.happ.helios.talk.contact;

import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import javax.annotation.Nullable;

import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@UiThread
@NotNullByDefault
public class ContactItemViewHolder<I extends ContactItem>
        extends RecyclerView.ViewHolder {

    protected final ViewGroup layout;
    protected final ImageView avatar;
    protected final TextView name;
    @Nullable
    protected final ImageView bulb;

    public ContactItemViewHolder(View v) {
        super(v);

        layout = (ViewGroup) v;
        avatar = v.findViewById(R.id.avatarView);
        name = v.findViewById(R.id.nameView);
        // this can be null as not all layouts that use this ViewHolder have it
        bulb = v.findViewById(R.id.bulbView);
    }

    protected void bind(I item, @Nullable
            BaseContactListAdapter.OnContactClickListener<I> listener) {
        if (item.getContact().getProfilePicture() == null)
            avatar.setImageResource(R.drawable.ic_person);
        else
            avatar.setImageBitmap(BitmapFactory.decodeByteArray(
                    item.getContact().getProfilePicture(),
                    0,
                    item.getContact().getProfilePicture().length)
            );
        name.setText(item.getContact().getAlias());

        if (bulb != null) {
            // online/offline
            if (item.isConnected()) {
                bulb.setImageResource(R.drawable.contact_connected);
            } else {
                bulb.setImageResource(R.drawable.contact_disconnected);
            }
        }

        layout.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(avatar, item);
        });
    }

}
