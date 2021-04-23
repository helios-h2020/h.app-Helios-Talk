package eu.h2020.helios_social.happ.helios.talk.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.util.HeliosTalkAdapter;

public class ResultListAdapter extends HeliosTalkAdapter<ResultItem, ResultItemViewHolder> {

    private ResultsActionListener listener;

    public ResultListAdapter(Context ctx, ResultsActionListener listener) {
        super(ctx, ResultItem.class);
        this.listener = listener;
    }

    @Override
    public int compare(ResultItem item1, ResultItem item2) {
        if (item1.getDistance() > item2.getDistance()) return -1;
        else if (item2.getDistance() < item2.getDistance()) return 1;
        else return 0;
    }

    @Override
    public boolean areContentsTheSame(ResultItem item1, ResultItem item2) {
        return item1.getId().equals(item2.getId());
    }

    @Override
    public boolean areItemsTheSame(ResultItem item1, ResultItem item2) {
        return item1.getId().equals(item2.getId());
    }


    @NonNull
    @Override
    public ResultItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.list_item_forum_result, parent, false);
        return new ResultItemViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultItemViewHolder resultItemViewHolder, int position) {
        resultItemViewHolder.bind(ctx, items.get(position));
    }
}
