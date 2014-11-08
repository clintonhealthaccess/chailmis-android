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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.BinCardHeader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinCardItemHeaderAdapter extends ArrayAdapter<BinCardHeader> {

    private final Context context;
    private final int resource;
    private final List<BinCardHeader> headerValues;

    public BinCardItemHeaderAdapter(Context context, int resource, List<BinCardHeader> headerValues) {
        super(context, resource, headerValues);
        this.context = context;
        this.resource = resource;
        this.headerValues = headerValues;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = layoutInflater.inflate(resource, parent, false);

        BinCardHeader binCardHeader = getItem(position);

        HashMap<Integer, String> hashMapHeaderIDName = new HashMap<>();
        hashMapHeaderIDName.put(R.id.textViewDate, binCardHeader.getDate());
        hashMapHeaderIDName.put(R.id.textViewReceivedFromIssuedTo, binCardHeader.getReceivedFromIssuedTo());
        hashMapHeaderIDName.put(R.id.textViewQuantityReceived, binCardHeader.getQuantityReceived());
        hashMapHeaderIDName.put(R.id.textViewQuantityDispensed, binCardHeader.getQuantityDispensed());
        hashMapHeaderIDName.put(R.id.textViewQuantityLost, binCardHeader.getQuantityLost());
        hashMapHeaderIDName.put(R.id.textViewStockBalance, binCardHeader.getStockBalance());

        for (Map.Entry<Integer, String> entry : hashMapHeaderIDName.entrySet()) {
            TextView textView = (TextView) rowView.findViewById(entry.getKey());
            textView.setText(entry.getValue());
            textView.setTypeface(null, Typeface.BOLD);
        }

        return rowView;
    }
}
