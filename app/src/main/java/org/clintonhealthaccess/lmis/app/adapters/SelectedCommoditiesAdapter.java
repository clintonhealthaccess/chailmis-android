package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.watchers.QuantityTextWatcher;

import java.util.List;

import de.greenrobot.event.EventBus;

public class SelectedCommoditiesAdapter extends ArrayAdapter<CommodityViewModel> {

    private int resource;

    public SelectedCommoditiesAdapter(Context context, int resource, List<CommodityViewModel> commodities) {
        super(context, resource, commodities);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        ImageButton imageButtonCancel = (ImageButton) rowView.findViewById(R.id.imageButtonCancel);
        final EditText editTextQuantity = (EditText) rowView.findViewById(R.id.editTextQuantity);

        final CommodityViewModel commodityViewModel = getItem(position);
        textViewCommodityName.setText(commodityViewModel.getName());

        TextWatcher watcher = new QuantityTextWatcher(editTextQuantity,commodityViewModel);
        editTextQuantity.addTextChangedListener(watcher);
        int quantity = commodityViewModel.getQuantityEntered();
        if(quantity > 0) editTextQuantity.setText(Integer.toString(quantity));
        activateCancelButton(imageButtonCancel, commodityViewModel);

        return rowView;
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final CommodityViewModel commodityViewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CommodityToggledEvent(commodityViewModel));
            }
        });
    }

}
