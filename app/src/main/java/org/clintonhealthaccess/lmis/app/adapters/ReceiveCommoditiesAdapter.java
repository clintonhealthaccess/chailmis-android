package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;

import java.util.List;

import de.greenrobot.event.EventBus;

public class ReceiveCommoditiesAdapter extends ArrayAdapter<ReceiveCommodityViewModel> {

    private int resource;

    public ReceiveCommoditiesAdapter(Context context, int resource, List<ReceiveCommodityViewModel> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, parent, false);

            holder.textViewCommodityName = (TextView) convertView.findViewById(R.id.textViewCommodityName);
            holder.imageButtonCancel = (ImageButton) convertView.findViewById(R.id.imageButtonCancel);
            holder.editTextOrderedQuantity = (EditText) convertView.findViewById(R.id.editTextOrderedQuantity);
            holder.editTextReceivedQuantity = (EditText) convertView.findViewById(R.id.editTextReceivedQuantity);
            holder.textViewDifferenceQuantity = (TextView) convertView.findViewById(R.id.textViewDifferenceQuantity);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String name = getItem(position).getCommodity().getName();
        holder.textViewCommodityName.setText(name);
        holder.editTextReceivedQuantity.setText(String.valueOf(getItem(position).getQuantityReceived()));
        holder.editTextOrderedQuantity.setText(String.valueOf(getItem(position).getQuantityOrdered()));
        holder.textViewDifferenceQuantity.setText(String.valueOf(getItem(position).getDifference()));
        activateCancelButton(holder.imageButtonCancel, getItem(position));

        return convertView;
    }

    static class ViewHolder {
        TextView textViewCommodityName;
        ImageButton imageButtonCancel;
        EditText editTextOrderedQuantity;
        EditText editTextReceivedQuantity;
        TextView textViewDifferenceQuantity;
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final ReceiveCommodityViewModel viewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CommodityToggledEvent(viewModel));
            }
        });
    }
}
