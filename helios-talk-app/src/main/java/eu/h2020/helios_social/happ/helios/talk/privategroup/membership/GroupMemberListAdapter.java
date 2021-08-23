package eu.h2020.helios_social.happ.helios.talk.privategroup.membership;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import eu.h2020.helios_social.happ.helios.talk.R;

import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;

public class GroupMemberListAdapter extends
        HeliosTalkAdapter<GroupMemberListItem, GroupMemberListItemViewHolder> {

    private OnGroupMemberItemSelectedListener<GroupMemberListItem> listener;

    GroupMemberListAdapter(Context context, OnGroupMemberItemSelectedListener listener) {
        super(context, GroupMemberListItem.class);
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupMemberListItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup,
                                                            int i) {
        View v = LayoutInflater.from(ctx).inflate(
                R.layout.list_item_private_group_member, viewGroup, false);
        return new GroupMemberListItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberListItemViewHolder ui,
                                 int position) {
        ui.bind(items.get(position), listener);
    }

    @Override
    public int compare(GroupMemberListItem m1, GroupMemberListItem m2) {
        String n1 = m1.getAlias();
        String n2 = m2.getAlias();
        return n1.compareTo(n2);
    }

    @Override
    public boolean areContentsTheSame(GroupMemberListItem item1, GroupMemberListItem item2) {
        return false;
    }

    @Override
    public boolean areItemsTheSame(GroupMemberListItem m1, GroupMemberListItem m2) {
        return m1.getPeerId().getId().equals(m2.getPeerId().getId());
    }

    public interface OnGroupMemberItemSelectedListener<I> {
        void onItemSelected(int selected, I item);
    }
}

