/*
 * Copyright (c) 2014, Thoughtworks Inc
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

package org.clintonhealthaccess.lmis.app.adapters.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityConsumptionReportRH2Item;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacilityConsumptionReportRH2Adapter extends ArrayAdapter<FacilityConsumptionReportRH2Item>{
    private int resource;
    private boolean isGrey = true;

    public FacilityConsumptionReportRH2Adapter(Context context, int resource, List<FacilityConsumptionReportRH2Item> items) {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(resource, parent, false);
        Map<Integer, String> integerStringMap = new HashMap<>();
        FacilityConsumptionReportRH2Item item = getItem(position);
        integerStringMap.put(R.id.textViewCommodityName, item.getCommodityName());
        integerStringMap.put(R.id.textViewOpeningBalance, String.valueOf(item.getOpeningStock()));
        integerStringMap.put(R.id.textViewQuantityReceived, String.valueOf(item.getCommoditiesReceived()));
        integerStringMap.put(R.id.textViewQuantityAdjusted, String.valueOf(item.getCommoditiesAdjusted()));
        integerStringMap.put(R.id.textViewQuantityDispensedToFacilities, String.valueOf(item.getCommoditiesDispensedToFacilities()));
        integerStringMap.put(R.id.textViewTotalDispensed, String.valueOf(item.totalDispensed()));
        integerStringMap.put(R.id.textViewQuantityLost, String.valueOf(item.getCommoditiesLost()));
        integerStringMap.put(R.id.textViewClosingStock, String.valueOf(item.getClosingStock()));

        for (Integer key : integerStringMap.keySet()) {
            TextView textView = (TextView) view.findViewById(key);
            textView.setText(integerStringMap.get(key));
        }
        if(isGrey){
            view.setBackgroundColor(getContext().getResources().getColor(R.color.m_grey));
        }
        isGrey = !isGrey;
        return view;
    }
}
