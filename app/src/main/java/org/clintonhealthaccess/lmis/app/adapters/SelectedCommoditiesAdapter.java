package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.List;

public class SelectedCommoditiesAdapter extends ArrayAdapter<Commodity> {
    private List<Commodity> objects;

    public SelectedCommoditiesAdapter(Context context, int resource, List<Commodity> objects) {
        super(context, resource, objects);
        this.objects = objects;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.selected_commodity_list_item, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        Commodity commodity = getItem(position);
        textViewCommodityName.setText(commodity.getName());

        return rowView;
    }



}
