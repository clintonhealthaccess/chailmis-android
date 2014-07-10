package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.StockService;

import java.util.List;

import roboguice.RoboGuice;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class CommoditiesAdapter extends ArrayAdapter<Commodity> {

    @Inject
    private StockService stockService;

    public CommoditiesAdapter(Context context, int resource, List<Commodity> commodities) {
        super(context, resource, commodities);
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.commodity_list_item, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        CheckBox checkboxCommoditySelected = (CheckBox) rowView.findViewById(R.id.checkboxCommoditySelected);
        Commodity commodity = getItem(position);
        textViewCommodityName.setText(commodity.getName());
        checkboxCommoditySelected.setChecked(commodity.getSelected());

        if (stockService.getStockLevelFor(commodity) == 0) rowView.setEnabled(false);

        return rowView;
    }
}
