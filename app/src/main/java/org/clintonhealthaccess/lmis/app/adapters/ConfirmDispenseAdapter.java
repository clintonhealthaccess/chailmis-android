package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;

import java.util.List;

import roboguice.RoboGuice;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ConfirmDispenseAdapter extends ArrayAdapter<DispensingItem> {
    public ConfirmDispenseAdapter(Context context, int resource, List<DispensingItem> items) {
        super(context, resource, items);
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.confirm_commodity_list_item, parent, false);

        TextView textViewAdjustedQuantity = (TextView) rowView.findViewById(R.id.textViewAdjustedQuantity);
        TextView textViewSOH = (TextView) rowView.findViewById(R.id.textViewSOH);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);

        DispensingItem item = getItem(position);

        textViewCommodityName.setText(item.getCommodity().getName());
        textViewAdjustedQuantity.setText(item.getQuantity().toString());
        textViewSOH.setText(String.valueOf(item.getCommodity().getStockItem().quantity() - item.getQuantity()));

        return rowView;
    }
}
