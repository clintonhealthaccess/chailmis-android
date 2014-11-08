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

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.reports.FacilityConsumptionReportRH2Adapter;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityCommodityConsumptionRH1ReportItem;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityConsumptionReportRH2Item;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.views.LmisProgressDialog;

import java.util.ArrayList;
import java.util.List;

public class FacilityConsumptionReportRH2Activity extends MonthBasedReportBaseActivity{
    @Inject
    Context context;

    @Override
    String getReportName() {
        return getString(R.string.rh2_report_header);
    }

    @Override
    int getHeaderLayout() {
        return R.layout.facility_consumption_report_rh2_header;
    }

    @Override
    ArrayAdapter getAdapter() {
        return new FacilityConsumptionReportRH2Adapter(context, R.layout.facility_consumption_report_rh2_item, new ArrayList<FacilityConsumptionReportRH2Item>());
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_facility_stock_report;
    }

    @Override
    void setItems() {
        new LoadReportAsyncTask().execute();
    }

    private class LoadReportAsyncTask extends AsyncTask<Void, Void, List<FacilityConsumptionReportRH2Item>> {
        LmisProgressDialog dialog;

        private LoadReportAsyncTask() {
            this.dialog = new LmisProgressDialog(FacilityConsumptionReportRH2Activity.this, getString(R.string.loading_report));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.show();
        }

        @Override
        protected List<FacilityConsumptionReportRH2Item> doInBackground(Void... voids) {
            try {
                return reportsService.getFacilityConsumptionReportRH2Items(category, getStartingYear(),
                        getStartingMonth(), getEndingYear(), getEndingMonth());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<FacilityConsumptionReportRH2Item> facilityConsumptionReportRH2Items) {
            if (facilityConsumptionReportRH2Items == null) {
                Toast.makeText(FacilityConsumptionReportRH2Activity.this,
                        getString(R.string.report_generation_error), Toast.LENGTH_LONG).show();
                dialog.dismiss();
                return;
            }
            adapter.clear();
            adapter.addAll(facilityConsumptionReportRH2Items);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        }
    }
}
