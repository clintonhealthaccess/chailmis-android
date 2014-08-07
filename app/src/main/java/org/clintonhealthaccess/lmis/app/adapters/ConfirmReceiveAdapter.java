package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.ReceiveItem;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ConfirmReceiveAdapter extends ArrayAdapter<ReceiveItem> {

    private int resource;

    public ConfirmReceiveAdapter(Context context, int resource, List<ReceiveItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, parent, false);

        holder.textViewCommodityName = (TextView) convertView.findViewById(R.id.textViewCommodityName);
        holder.textViewQuantityAllocated = (TextView) convertView.findViewById(R.id.textViewQuantityAllocated);
        holder.textViewQuantityReceived = (TextView) convertView.findViewById(R.id.textViewQuantityReceived);
        holder.textViewQuantityDifference = (TextView) convertView.findViewById(R.id.textViewQuantityDifference);

        holder.textViewCommodityName.setText(getItem(position).getCommodity().getName());
        holder.textViewQuantityAllocated.setText(String.valueOf(getItem(position).getQuantityAllocated()));
        holder.textViewQuantityReceived.setText(String.valueOf(getItem(position).getQuantityReceived()));
        holder.textViewQuantityDifference.setText(String.valueOf(getItem(position).getDifference()));

        return convertView;
    }

    static class ViewHolder {
        TextView textViewCommodityName;
        TextView textViewQuantityAllocated;
        TextView textViewQuantityReceived;
        TextView textViewQuantityDifference;
    }
}
