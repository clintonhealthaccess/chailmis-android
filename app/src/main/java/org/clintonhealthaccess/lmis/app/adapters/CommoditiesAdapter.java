package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.List;

public class CommoditiesAdapter extends ArrayAdapter<Commodity> {
    public CommoditiesAdapter(Context context, int resource, List<Commodity> objects) {

        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.commodity_list_item, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        CheckBox checkboxCommoditySelected = (CheckBox) rowView.findViewById(R.id.checkboxCommoditySelected);
        Commodity commodity = getItem(position);
        textViewCommodityName.setText(commodity.getName());
        checkboxCommoditySelected.setChecked(commodity.getSelected());

        return rowView;
    }
}
