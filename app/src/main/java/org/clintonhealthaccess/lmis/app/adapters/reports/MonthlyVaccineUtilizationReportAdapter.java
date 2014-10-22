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

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.reports.UtilizationItem;
import org.clintonhealthaccess.lmis.app.models.reports.UtilizationValue;

import java.util.List;

public class MonthlyVaccineUtilizationReportAdapter extends ArrayAdapter<UtilizationItem> {
    private int resource;
    private boolean isNamesListView;
    public LinearLayout.LayoutParams PARAMS = getLayoutParams();

    public MonthlyVaccineUtilizationReportAdapter(Context context, int resource,
                                                  List<UtilizationItem> items, boolean isNamesListView) {
        super(context, resource, items);
        this.resource = resource;
        this.isNamesListView = isNamesListView;
    }

    private static LinearLayout.LayoutParams getLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(0, 0, 10, 0);
        return params;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(resource, parent, false);
        UtilizationItem item = getItem(position);


        if (isNamesListView) {
            TextView textView = new TextView(getContext());
            textView.setText(item.getName());
            textView.setTextColor(getContext().getResources().getColor(R.color.black));
            setBold(position, textView);
            linearLayout.addView(textView);
        } else {
            for (UtilizationValue value : item.getUtilizationValues()) {
                TextView textView = new TextView(getContext());
                textView.setText(String.valueOf(value.getValue()));
                textView.setLayoutParams(PARAMS);
                textView.setGravity(1);
                textView.setTextColor(getContext().getResources().getColor(R.color.black));

                setBold(position, textView);
                linearLayout.addView(textView);
            }
        }

        return linearLayout;
    }

    private void setBold(int position, TextView textView) {
        if (position == 0) {
            textView.setTypeface(null, Typeface.BOLD);
        }
    }
}

