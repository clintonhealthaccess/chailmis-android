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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
import org.clintonhealthaccess.lmis.app.services.AlertsService;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class AlertsAdapter extends ArrayAdapter<LowStockAlert> {
    public AlertsAdapter(Context context, int resource, List<LowStockAlert> alerts) {
        super(context, resource, alerts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.alert_list_item, parent, false);

        TextView textViewComodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        TextView textViewQuantity = (TextView) rowView.findViewById(R.id.textViewQuantity);
        TextView textViewAlertDate = (TextView) rowView.findViewById(R.id.textViewAlertDate);

        LowStockAlert alert = getItem(position);
        int stockOnHand = alert.getCommodity().getStockOnHand();

        textViewAlertDate.setText(AlertsService.ALERT_DATE_FORMAT.format(alert.getDateCreated()));
        textViewComodityName.setText(alert.getCommodity().getName());
        textViewQuantity.setText(String.valueOf(stockOnHand));

        if (alert.getCommodity().isOutOfStock()) {
            textViewComodityName.setTextColor(getContext().getResources().getColor(R.color.alerts_red));
            textViewAlertDate.setTextColor(getContext().getResources().getColor(R.color.alerts_red));
            textViewQuantity.setTextColor(getContext().getResources().getColor(R.color.alerts_red));
        }


        return rowView;
    }

}
