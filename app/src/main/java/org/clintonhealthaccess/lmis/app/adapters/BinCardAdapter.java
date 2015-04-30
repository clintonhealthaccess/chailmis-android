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
import org.clintonhealthaccess.lmis.app.models.reports.BinCardItem;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;

import java.util.List;

public class BinCardAdapter extends ArrayAdapter<BinCardItem> {

    private final int resource;

    public BinCardAdapter(Context context, int resource, List<BinCardItem> binCardItems) {
        super(context, resource, binCardItems);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = layoutInflater.inflate(resource, parent, false);

        BinCardItem binCardItem = getItem(position);

        TextView textViewDate = (TextView) rowView.findViewById(R.id.textViewDate);
        textViewDate.setText(DateUtil.formatDate(binCardItem.getDate()));

        TextView textViewReceivedFromIssuedTo = (TextView) rowView.findViewById(R.id.textViewReceivedFromIssuedTo);
        textViewReceivedFromIssuedTo.setText(String.valueOf(binCardItem.getReceivedFromIssuedTo()));

        TextView textViewQuantityReceived = (TextView) rowView.findViewById(R.id.textViewQuantityReceived);
        if (binCardItem.getQuantityReceived() > 0) {
            textViewQuantityReceived.setText(String.valueOf(binCardItem.getQuantityReceived()));
        }

        TextView textViewQuantityDispensed = (TextView) rowView.findViewById(R.id.textViewQuantityDispensed);
        if (binCardItem.getQuantityDispensed() > 0) {
            textViewQuantityDispensed.setText(String.valueOf(binCardItem.getQuantityDispensed()));
        }

        TextView textViewQuantityLost = (TextView) rowView.findViewById(R.id.textViewQuantityLost);
        if (binCardItem.getQuantityLost() > 0) {
            textViewQuantityLost.setText(String.valueOf(binCardItem.getQuantityLost()));
        }

        TextView textViewQuantityAdjusted = (TextView) rowView.findViewById(R.id.textViewQuantityAdjusted);
        if (binCardItem.getQuantityAdjusted() != 0) {
            textViewQuantityAdjusted.setText(String.valueOf(binCardItem.getQuantityAdjusted()));
        }

        TextView textViewQuantityStockBalance = (TextView) rowView.findViewById(R.id.textViewStockBalance);
        textViewQuantityStockBalance.setText(binCardItem.getStockBalance() == -1 ? "-" : String.valueOf(binCardItem.getStockBalance()));

        return rowView;
    }
}
