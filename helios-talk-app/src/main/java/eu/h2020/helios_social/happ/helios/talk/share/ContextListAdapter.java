package eu.h2020.helios_social.happ.helios.talk.share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.R;

public class ContextListAdapter extends BaseAdapter {

    private Context mContext;
    private List<ContextItem> contextItems;
    private LayoutInflater inflater;

    public ContextListAdapter(@NonNull Context context, @NonNull List<ContextItem> contextItems) {

        this.contextItems = contextItems;
        this.mContext = context;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return contextItems.size();
    }

    @Override
    public ContextItem getItem(int position) {
        return contextItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addAll(Collection<? extends ContextItem> items) {
        contextItems.addAll(items);
    }

    public void removeAll(Collection<? extends ContextItem> items) {
        contextItems.removeAll(items);
    }

    public void clear() {
        contextItems.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_selection_item, null);
        }

        EmojiTextView selection = (EmojiTextView) convertView.findViewById(R.id.selection);
        ContextItem currentContext = contextItems.get(position);
        selection.setText(currentContext.getName());

        return convertView;
    }
}
