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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.BaseActivity;
import org.clintonhealthaccess.lmis.app.activities.ReportsActivity;
import org.clintonhealthaccess.lmis.app.adapters.reports.MonthlyVaccineUtilizationReportAdapter;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.ReportType;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.models.reports.MonthlyVaccineUtilizationReportItem;
import org.clintonhealthaccess.lmis.app.models.reports.UtilizationItem;
import org.clintonhealthaccess.lmis.app.models.reports.UtilizationValue;
import org.clintonhealthaccess.lmis.app.services.ReportsService;
import org.clintonhealthaccess.lmis.app.views.LmisProgressDialog;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import roboguice.inject.InjectView;

public class MonthlyVaccineUtilizationReportActivity extends BaseActivity {

    @Inject
    ReportsService reportsService;

    @InjectView(R.id.spinnerYear)
    public Spinner spinnerYear;

    @InjectView(R.id.spinnerMonth)
    public Spinner spinnerMonth;

    @InjectView(R.id.textViewReportName)
    public TextView textViewReportName;

    @InjectView(R.id.linearLayoutCommodityNamesAndItemNames)
    LinearLayout linearLayoutCommodityNamesAndItemNames;

    @InjectView(R.id.linearLayoutUtilizationReportItemValues)
    LinearLayout linearLayoutUtilizationReportItemValues;

    private Category category;
    private ReportType reportType;
    public static final int NUMBER_OF_YEARS = 10;

    @Inject
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();


        category = (Category) getIntent().getSerializableExtra(ReportsActivity.CATEGORY_BUNDLE_KEY);
        reportType = (ReportType) getIntent().getSerializableExtra(ReportsActivity.REPORT_TYPE_BUNDLE_KEY);

        setContentView(R.layout.activity_monthly_vaccine_utilization_report);
        textViewReportName.setText(reportType.getName());

        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_black, getLastNYears(NUMBER_OF_YEARS));
        ArrayAdapter<String> startMonthAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_black, getMonths());
        spinnerMonth.setAdapter(startMonthAdapter);

        Calendar calendar = Calendar.getInstance();
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        spinnerYear.setAdapter(yearsAdapter);

        setupListeners();
    }

    protected void setupActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.transparent);
    }

    private void setupListeners() {

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        spinnerMonth.setOnItemSelectedListener(listener);
        spinnerYear.setOnItemSelectedListener(listener);
    }

    public List<String> getMonths() {
        ArrayList<String> strings = new ArrayList<>(Arrays.asList(new DateFormatSymbols().getMonths()));
        return FluentIterable.from(strings).filter(new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !input.isEmpty();
            }
        }).toList();
    }

    protected ArrayList<String> getLastNYears(int numberOfYears) {
        ArrayList<String> years = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        for (int i = 0; i < numberOfYears; i++) {
            years.add(String.valueOf(currentYear - i));
        }
        return years;
    }

    void setItems() {
        new LoadReportAsyncTask().execute();
    }

    private void reloadReport(List<MonthlyVaccineUtilizationReportItem> reportItems) {
        clearReportItems();

        for (MonthlyVaccineUtilizationReportItem reportItem : reportItems) {
            LinearLayout outerLayout = new LinearLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 230);
            outerLayout.setLayoutParams(params);
            outerLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView textView = new TextView(context);
            textView.setWidth(150);
            textView.setText(reportItem.getCommodityName());
            textView.setTextColor(getResources().getColor(R.color.black));
            outerLayout.addView(textView);

            ListView listViewItemNames = new ListView(context);
            listViewItemNames.setAdapter(new MonthlyVaccineUtilizationReportAdapter(context,
                    R.layout.monthly_vaccine_utilization_report_item, reportItem.getUtilizationItems(), true));
            LinearLayout.LayoutParams paramsListView = new LinearLayout.LayoutParams(240, ViewGroup.LayoutParams.MATCH_PARENT);
            listViewItemNames.setLayoutParams(paramsListView);
            outerLayout.addView(listViewItemNames);

            linearLayoutCommodityNamesAndItemNames.addView(outerLayout);


            ListView listViewItemValues = new ListView(context);
            LinearLayout.LayoutParams paramsListViewValues = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 230);
            listViewItemValues.setLayoutParams(paramsListViewValues);

            listViewItemValues.setAdapter(new MonthlyVaccineUtilizationReportAdapter(context,
                    R.layout.monthly_vaccine_utilization_report_item, reportItem.getUtilizationItems(), false));

            linearLayoutUtilizationReportItemValues.addView(listViewItemValues);
        }
    }

    private void clearReportItems() {
        linearLayoutCommodityNamesAndItemNames.removeAllViews();
        linearLayoutUtilizationReportItemValues.removeAllViews();
    }

    protected String getYear() {
        return (String) spinnerYear.getSelectedItem();
    }

    protected String getMonth() {
        return (String) spinnerMonth.getSelectedItem();
    }

    private class LoadReportAsyncTask extends AsyncTask<Void, Void, List<MonthlyVaccineUtilizationReportItem>> {
        LmisProgressDialog dialog;

        private LoadReportAsyncTask() {
            this.dialog = new LmisProgressDialog(MonthlyVaccineUtilizationReportActivity.this, getString(R.string.loading_report));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.show();
        }

        @Override
        protected List<MonthlyVaccineUtilizationReportItem> doInBackground(Void... voids) {
            try {
                return reportsService.getMonthlyVaccineUtilizationReportItems(category, getYear(),
                        getMonth(), reportType.equals(ReportType.MonthlyHealthFacilityDevicesUtilizationReport));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<MonthlyVaccineUtilizationReportItem> reportItems) {
            if (reportItems == null) {
                Toast.makeText(MonthlyVaccineUtilizationReportActivity.this,
                        getString(R.string.report_generation_error), Toast.LENGTH_LONG).show();
                dialog.dismiss();
                return;
            }
            reloadReport(reportItems);
            dialog.dismiss();
        }
    }

}
