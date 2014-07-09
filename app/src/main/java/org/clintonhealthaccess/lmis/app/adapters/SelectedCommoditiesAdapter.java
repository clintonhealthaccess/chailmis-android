package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.List;

import de.greenrobot.event.EventBus;

public class SelectedCommoditiesAdapter extends ArrayAdapter<Commodity> {

    public SelectedCommoditiesAdapter(Context context, int resource, List<Commodity> commodities) {
        super(context, resource, commodities);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.selected_commodity_list_item, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        ImageButton imageButtonCancel = (ImageButton)rowView.findViewById(R.id.imageButtonCancel);
        final Commodity commodity = getItem(position);
        textViewCommodityName.setText(commodity.getName());

        activateCancelButton(imageButtonCancel, commodity);

        return rowView;
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final Commodity commodity) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CommodityToggledEvent(commodity));
            }
        });
    }
}
