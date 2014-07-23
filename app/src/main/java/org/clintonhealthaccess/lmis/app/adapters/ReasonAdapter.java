package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.OrderReason;

import java.util.List;

public class ReasonAdapter extends ArrayAdapter<OrderReason> {
    public ReasonAdapter(Context context, int resource, List<OrderReason> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView textView = (TextView) inflater.inflate(R.layout.spinner_item, parent, false);
        textView.setText(getItem(position).getReason());
        return textView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);

        if (v == null) {
            v = new TextView(getContext());
        }
        v.setText(getItem(position).getReason());
        return v;
    }
}
