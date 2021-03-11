package eu.h2020.helios_social.happ.helios.talk.forum.membership;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;

public class ForumMemberListAdapter extends
        HeliosTalkAdapter<ForumMemberListItem, ForumMemberListItemViewHolder> {

    private OnForumMemberItemSelectedListener<ForumMemberListItem> listener;

    ForumMemberListAdapter(Context context, OnForumMemberItemSelectedListener listener) {
        super(context, ForumMemberListItem.class);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ForumMemberListItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup,
                                                            int i) {
        View v = LayoutInflater.from(ctx).inflate(
                R.layout.list_item_member, viewGroup, false);
        return new ForumMemberListItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumMemberListItemViewHolder ui,
                                 int position) {
        ui.bind(items.get(position), listener);
    }

    @Override
    public int compare(ForumMemberListItem m1, ForumMemberListItem m2) {
        String n1 = m1.getAlias();
        String n2 = m2.getAlias();
        return n1.compareTo(n2);
    }

    @Override
    public boolean areContentsTheSame(ForumMemberListItem item1, ForumMemberListItem item2) {
        return false;
    }

    @Override
    public boolean areItemsTheSame(ForumMemberListItem m1, ForumMemberListItem m2) {
        return m1.getPeerId().getId().equals(m2.getPeerId().getId());
    }

    public interface OnForumMemberItemSelectedListener<I> {
        void onItemSelected(int selected, I item);
    }
}

