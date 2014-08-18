/*
 * Copyright (c) 2014, Clinton Health Access Initiative
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

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
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;

import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.widget.AdapterView.OnItemClickListener;

public class CommoditiesAdapter extends ArrayAdapter<BaseCommodityViewModel> {
    private CommodityDisplayStrategy commodityDisplayStrategy;

    public CommoditiesAdapter(Context context, int resource, List<BaseCommodityViewModel> commodities, CommodityDisplayStrategy commodityDisplayStrategy) {
        super(context, resource, commodities);
        this.commodityDisplayStrategy = commodityDisplayStrategy;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.commodity_list_item, parent, false);
        CheckBox checkboxCommoditySelected = (CheckBox) rowView.findViewById(R.id.checkboxCommoditySelected);
        BaseCommodityViewModel commodityViewModel = getItem(position);
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
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BaseCommodityViewModel commodityViewModel = getItem(position);
                if (commodityDisplayStrategy.allowClick(commodityViewModel)) {
                    commodityViewModel.toggleSelected();
                    EventBus.getDefault().post(new CommodityToggledEvent(commodityViewModel));
                    notifyDataSetChanged();
                }
            }
        });
    }
}
