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

import android.os.AsyncTask;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.FacilityStockReportAdapter;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.views.LmisProgressDialog;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public class FacilityStockReportActivity extends MonthBasedReportBaseActivity<FacilityStockReportAdapter> {

    @InjectView(R.id.listViewDummyHeader)
    ListView listViewDummyHeader;

    private boolean isLoading = false;

    @Override
    String getReportName() {
        return String.format("Facility Stock Report for %s", category.getName());
    }

    @Override
    int getHeaderLayout() {
        return R.layout.facility_stock_report_header;
    }

    @Override
    FacilityStockReportAdapter getAdapter() {
        return new FacilityStockReportAdapter(getApplicationContext(), R.layout.facility_stock_report_item,
                new ArrayList<FacilityStockReportItem>(), getResources().getColor(R.color.m_grey));
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_facility_stock_report;
    }

    @Override
    void setItems() {
        if (!isLoading) {
            isLoading = true;
            new LoadReportAsyncTask().execute();
        }
    }

    @Override
    void afterCreate() {
        listViewDummyHeader.addHeaderView(getLayoutInflater().inflate(getHeaderLayout(), null));
        listViewDummyHeader.setAdapter(getAdapter());

        listViewReport.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem != 0) {
                    listViewDummyHeader.setVisibility(View.VISIBLE);
                } else {
                    listViewDummyHeader.setVisibility(View.GONE);
                }
            }
        });

    }

    private class LoadReportAsyncTask extends AsyncTask<Void, Void, List<FacilityStockReportItem>> {
        LmisProgressDialog dialog;

        private LoadReportAsyncTask() {
            this.dialog = new LmisProgressDialog(FacilityStockReportActivity.this, getString(R.string.loading_report));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.show();
        }

        @Override
        protected List<FacilityStockReportItem> doInBackground(Void... voids) {
            try {
                return reportsService.getFacilityReportItemsForCategory(category, getStartingYear(),
                        getStartingMonth(), getEndingYear(), getEndingMonth());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<FacilityStockReportItem> facilityStockReportItems) {
            if (facilityStockReportItems == null) {
                Toast.makeText(FacilityStockReportActivity.this, getString(R.string.report_generation_error), Toast.LENGTH_LONG).show();
                dialog.dismiss();
                isLoading = false;
                return;
            }
            reloadReport(facilityStockReportItems);
            dialog.dismiss();
        }
    }

    private void reloadReport(List<FacilityStockReportItem> facilityStockReportItems) {
        reloadColumnNames(facilityStockReportItems);
        adapter.clear();
        adapter.addAll(facilityStockReportItems);
        adapter.notifyDataSetChanged();
        isLoading = false;
    }

    private void reloadColumnNames(List<FacilityStockReportItem> facilityStockReportItems) {
        List<String> columnNames = new ArrayList<>();
        columnNames.add(getString(R.string.commodity));
        columnNames.addAll(FluentIterable.from(facilityStockReportItems).transform(new Function<FacilityStockReportItem, String>() {
            @Override
            public String apply(FacilityStockReportItem facilityStockReportItem) {
                return facilityStockReportItem.getCommodityName();
            }
        }).toList());
    }

}
