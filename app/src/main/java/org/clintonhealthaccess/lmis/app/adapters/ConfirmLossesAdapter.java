package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.LossItem;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ConfirmLossesAdapter extends ArrayAdapter<LossItem> {

    private int resource;

    public ConfirmLossesAdapter(Context context, int resource, List<LossItem> lossItems) {
        super(context, resource, lossItems);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, parent, false);

            viewHolder.textViewCommodityName = (TextView) convertView.findViewById(R.id.lossesCommodityName);
            viewHolder.textViewTotalLosses = (TextView) convertView.findViewById(R.id.totalLosses);
            viewHolder.textViewNewStockOnHand = (TextView) convertView.findViewById(R.id.newStockOnHand);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textViewCommodityName.setText(getItem(position).getCommodity().getName());
        viewHolder.textViewNewStockOnHand.setText(String.valueOf(getItem(position).getNewStockOnHand()));
        viewHolder.textViewTotalLosses.setText(String.valueOf(getItem(position).getTotalLosses()));

        return convertView;
    }

    static class ViewHolder {
        TextView textViewCommodityName;
        TextView textViewTotalLosses;
        TextView textViewNewStockOnHand;
    }
}
