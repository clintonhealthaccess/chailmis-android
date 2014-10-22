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

package org.clintonhealthaccess.lmis.app.activities.reports;

import android.graphics.Typeface;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.FacilityCommodityConsumptionReportRH1Adapter;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityCommodityConsumptionRH1ReportItem;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FacilityConsumptionReportRH1Activity extends MonthBasedReportBaseActivity<FacilityCommodityConsumptionReportRH1Adapter> {
    private static final String TAG = "REPORTS";

    @Override
    String getReportName() {
        return getString(R.string.rh1_report_header);
    }

    @Override
    int getHeaderLayout() {
        return R.layout.facility_consumption_report_rh1_header;
    }

    @Override
    FacilityCommodityConsumptionReportRH1Adapter getAdapter() {
        return new FacilityCommodityConsumptionReportRH1Adapter(getApplicationContext(), R.layout.facility_commodity_consumption_report_rh1_item, new ArrayList<FacilityCommodityConsumptionRH1ReportItem>());
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_facility_consumption_report_rh1;
    }

    @Override
    void setItems() {
        List<FacilityCommodityConsumptionRH1ReportItem> itemsForCategory = reportsService.getFacilityCommodityConsumptionReportRH1(category, getStartingYear(), getStartingMonth(), getStartingYear(), getStartingMonth());
        adapter.clear();
        adapter.addAll(itemsForCategory);
        adapter.notifyDataSetChanged();
    }

    @Override
    void setupListViewHeader() {
        LinearLayout linearLayout = buildHeaderView();
        listViewReport.addHeaderView(linearLayout);
    }

    LinearLayout buildHeaderView() {
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(getHeaderLayout(), null);
        try {
            Date startingDate = reportsService.convertToDate(getStartingYear(), getStartingMonth(), true);
            Date endDate = reportsService.convertToDate(getStartingYear(), getStartingMonth(), false);

            List<DateTime> days = reportsService.getDays(startingDate, endDate);
            LinearLayout layoutRows = (LinearLayout) linearLayout.findViewById(R.id.layoutDates);
            for (DateTime day : days) {
                TextView textView = new TextView(getApplicationContext());
                textView.setText(String.valueOf(day.getDayOfMonth()));
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setTypeface(null, Typeface.BOLD);
                textView.setLayoutParams(FacilityCommodityConsumptionReportRH1Adapter.PARAMS);
                layoutRows.addView(textView);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates:" + e.getLocalizedMessage());

        }
        return linearLayout;
    }

    @Override
    SpinnerVisibilityStrategy getStrategy() {
        return SpinnerVisibilityStrategy.startVisible;
    }
}
