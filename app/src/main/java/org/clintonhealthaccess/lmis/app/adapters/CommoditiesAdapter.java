package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;

import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.widget.AdapterView.OnItemClickListener;

public class CommoditiesAdapter extends ArrayAdapter<CommodityViewModel> {
    private CommodityDisplayStrategy commodityDisplayStrategy;

    public CommoditiesAdapter(Context context, int resource, List<CommodityViewModel> commodities, CommodityDisplayStrategy commodityDisplayStrategy) {
        super(context, resource, commodities);
        this.commodityDisplayStrategy = commodityDisplayStrategy;
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

        commodityDisplayStrategy.apply(commodityViewModel, checkboxCommoditySelected, textViewCommodityOutOfStock);

        return rowView;
    }

    public void adaptGridViewCommodities(GridView gridViewCommodities, final CommodityDisplayStrategy commodityDisplayStrategy) {
        gridViewCommodities.setAdapter(this);
        gridViewCommodities.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CommodityViewModel commodityViewModel = getItem(i);
                if (commodityDisplayStrategy.allowClick(commodityViewModel)) {
                    commodityViewModel.toggleSelected();
                    EventBus.getDefault().post(new CommodityToggledEvent(commodityViewModel));
                    notifyDataSetChanged();
                }
            }
        });
    }
}
