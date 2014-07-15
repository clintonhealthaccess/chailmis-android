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

    public static final CheckBoxVisibilityStrategy DO_HIDE = new CheckBoxVisibilityStrategy() {
        @Override
        public void apply(CommodityViewModel commodityViewModel, CheckBox checkboxCommoditySelected, TextView textViewCommodityOutOfStock) {
            if (commodityViewModel.stockIsFinished()) {
                checkboxCommoditySelected.setVisibility(View.INVISIBLE);
                textViewCommodityOutOfStock.setVisibility(View.VISIBLE);
            }
        }
    };

    public static final CheckBoxVisibilityStrategy DO_NOTHING = new CheckBoxVisibilityStrategy() {
        @Override
        public void apply(CommodityViewModel commodityViewModel, CheckBox checkboxCommoditySelected, TextView textViewCommodityOutOfStock) {
            // do nothing;
        }
    };
    private CheckBoxVisibilityStrategy checkBoxVisibilityStrategy;

    public CommoditiesAdapter(Context context, int resource, List<CommodityViewModel> commodities) {
        super(context, resource, commodities);
        this.checkBoxVisibilityStrategy = DO_HIDE;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    public CommoditiesAdapter(Context context, int resource, List<CommodityViewModel> commodities, CheckBoxVisibilityStrategy checkBoxVisibilityStrategy) {
        super(context, resource, commodities);
        this.checkBoxVisibilityStrategy = checkBoxVisibilityStrategy;
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

        checkBoxVisibilityStrategy.apply(commodityViewModel, checkboxCommoditySelected, textViewCommodityOutOfStock);

        return rowView;
    }

    abstract public interface CheckBoxVisibilityStrategy {
        void apply(CommodityViewModel commodityViewModel, CheckBox checkboxCommoditySelected, TextView textViewCommodityOutOfStock);
    }
}
