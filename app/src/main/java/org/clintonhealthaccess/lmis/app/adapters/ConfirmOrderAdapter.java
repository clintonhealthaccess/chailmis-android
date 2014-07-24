package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.OrderItem;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ConfirmOrderAdapter extends ArrayAdapter<OrderItem> {
    public ConfirmOrderAdapter(Context context, int resource, List<OrderItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.confirm_order_list_item, parent, false);
        TextView textViewSerialNumber = (TextView) rowView.findViewById(R.id.textViewSerialNumber);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        TextView textViewQuantityOrdered = (TextView) rowView.findViewById(R.id.textViewQuantityOrdered);
        OrderItem item = getItem(position);
        textViewSerialNumber.setText(item.getSRVNumber());
        textViewCommodityName.setText(item.getCommodtyName());
        textViewQuantityOrdered.setText(String.valueOf(item.getQuantity()));
        return rowView;
    }
}
