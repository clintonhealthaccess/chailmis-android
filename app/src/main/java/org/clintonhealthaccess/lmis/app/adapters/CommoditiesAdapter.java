package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewModels.CommodityViewModel;

import java.util.List;

import roboguice.RoboGuice;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class CommoditiesAdapter extends ArrayAdapter<CommodityViewModel> {


    public CommoditiesAdapter(Context context, int resource, List<CommodityViewModel> commodities) {
        super(context, resource, commodities);
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.commodity_list_item, parent, false);
        CheckBox checkboxCommoditySelected = (CheckBox) rowView.findViewById(R.id.checkboxCommoditySelected);
        CommodityViewModel commodityViewModel = getItem(position);
        checkboxCommoditySelected.setChecked(commodityViewModel.isSelected());
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        textViewCommodityName.setText(commodityViewModel.getName());

        TextView textViewCommodityOutOfStock = (TextView) rowView.findViewById(R.id.textViewCommodityOutOfStock);

        if (commodityViewModel.stockIsFinished()) {
            checkboxCommoditySelected.setVisibility(View.INVISIBLE);
            textViewCommodityOutOfStock.setVisibility(View.VISIBLE);
        }

        return rowView;
    }
}
