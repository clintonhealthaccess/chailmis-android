package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;

import java.util.List;

import roboguice.RoboGuice;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ConfirmDispenseAdapter extends ArrayAdapter<DispensingItem> {
    private final Dispensing dispensing;

    public ConfirmDispenseAdapter(Context context, int resource, List<DispensingItem> items, Dispensing dispensing) {
        super(context, resource, items);
        RoboGuice.getInjector(context).injectMembers(this);
        this.dispensing = dispensing;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.confirm_commodity_list_item, parent, false);

        TextView textViewAdjustedQuantity = (TextView) rowView.findViewById(R.id.textViewAdjustedQuantity);
        TextView textViewSOH = (TextView) rowView.findViewById(R.id.textViewSOH);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);

        DispensingItem item = getItem(position);
        if (dispensing.isDispenseToFacility()) {
            textViewCommodityName.setText(item.getCommodity().getName());
            textViewAdjustedQuantity.setText(item.getQuantity().toString());
            textViewSOH.setText(String.valueOf(item.getCommodity().getStockItem().getQuantity() - item.getQuantity()));
        } else {
            textViewCommodityName.setText(item.getCommodity().getName());
            textViewAdjustedQuantity.setVisibility(View.INVISIBLE);
            textViewSOH.setText(String.valueOf(item.getQuantity()));
        }

        return rowView;
    }
}
