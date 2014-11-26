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

package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.reports.ConsumptionValue;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityCommodityConsumptionRH1ReportItem;

import java.util.List;

public class FacilityCommodityConsumptionReportRH1Adapter extends ArrayAdapter<FacilityCommodityConsumptionRH1ReportItem> {
    private final int resource;
    boolean isGrey = true;
    public static final LinearLayout.LayoutParams PARAMS = getLayoutParams();

    private static LinearLayout.LayoutParams getLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.setP(3, 3, 3, 3);
        params.gravity = Gravity.CENTER;
        return params;
    }

    public FacilityCommodityConsumptionReportRH1Adapter(Context context, int resource,
                                                        List<FacilityCommodityConsumptionRH1ReportItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(resource, parent, false);
        FacilityCommodityConsumptionRH1ReportItem item = getItem(position);

        TextView textViewCommodityName = (TextView) linearLayout.findViewById(R.id.textViewCommodityName);
        textViewCommodityName.setText(item.getCommodity().getName());
        textViewCommodityName.setPadding(3, 3, 3, 3);
        textViewCommodityName.setTextSize(getContext().getResources().getDimension(R.dimen.day_item_text_size));

        for (ConsumptionValue value : item.getValues()) {
            TextView textView = new TextView(getContext());
            textView.setLayoutParams(PARAMS);
            textView.setTextColor(getContext().getResources().getColor(R.color.black));
            textView.setText(String.valueOf(value.getConsumption()));
            textView.setPadding(3, 3, 3, 3);
            textView.setTextSize(getContext().getResources().getDimension(R.dimen.day_item_text_size));
            linearLayout.addView(textView);
        }

        if(isGrey){
            linearLayout.setBackgroundColor(getContext().getResources().getColor(R.color.m_grey));
        }
        isGrey = !isGrey;

        return linearLayout;
    }
}
