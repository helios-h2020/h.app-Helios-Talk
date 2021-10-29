package eu.h2020.helios_social.happ.helios.talk.group;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import eu.h2020.helios_social.happ.helios.talk.R;

public class GroupTypeSelectionAdapter extends ArrayAdapter {
    private String[] groupTypeName;
    private int[] info;
    private int[] hint;

    private Integer[] imageid;
    private Activity context;

    public GroupTypeSelectionAdapter(Activity context, String[] groupTypeName, int[] info, int[] hint, Integer[] imageid) {
        super(context, R.layout.list_group_type_select, groupTypeName);
        this.context = context;
        this.groupTypeName = groupTypeName;
        this.info = info;
        this.hint = hint;
        this.imageid = imageid;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row=convertView;
        LayoutInflater inflater = context.getLayoutInflater();
        if(convertView==null)
            row = inflater.inflate(R.layout.list_group_type_select, null, true);
        TextView textViewGroupTypeName = row.findViewById(R.id.name);
        TextView textViewInfo = row.findViewById(R.id.info_text);
        TextView textViewHint = row.findViewById(R.id.hint_text);
        ImageView imageFlag = row.findViewById(R.id.icon);

        String hintText = context.getString(hint[position]);
        if (position==0){
            ImageView hintIV = row.findViewById(R.id.icon_hint);
            textViewHint.setVisibility(View.GONE);
            hintIV.setVisibility(View.GONE);
        }

        textViewGroupTypeName.setText(groupTypeName[position]);
        textViewInfo.setText(info[position]);
        textViewHint.setText(hint[position]);
        imageFlag.setImageResource(imageid[position]);
        return  row;
    }
}
