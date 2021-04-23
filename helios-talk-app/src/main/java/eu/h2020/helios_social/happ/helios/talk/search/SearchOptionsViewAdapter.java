package eu.h2020.helios_social.happ.helios.talk.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import eu.h2020.helios_social.happ.helios.talk.R;

public class SearchOptionsViewAdapter extends BaseAdapter {

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private List<String> options;

    public SearchOptionsViewAdapter(Context context, List<String> optionsList) {
        mContext = context;
        this.options = optionsList;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public String getItem(int position) {
        return options.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup container) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_search_option_item, container, false);
        }

        ((TextView) view.findViewById(R.id.option))
                .setText(getItem(position));
        return view;
    }

}
