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
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
import org.clintonhealthaccess.lmis.app.models.ReportType;
import org.clintonhealthaccess.lmis.app.models.reports.MonthlyVaccineUtilizationReportItem;
import org.clintonhealthaccess.lmis.app.services.ReportsService;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;
import org.clintonhealthaccess.lmis.app.views.LmisProgressDialog;
import org.clintonhealthaccess.lmis.app.views.NonScrollListView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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

    @InjectView(R.id.buttonLoadReport)
    Button buttonLoadReport;

    @InjectView(R.id.scrollViewReportItems)
    ScrollView scrollViewReportItems;

    @InjectView(R.id.textViewReloadReport)
    TextView textViewReloadReport;

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

        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item_large, getLastNYears(NUMBER_OF_YEARS));
        spinnerYear.setAdapter(yearsAdapter);
        setupMonthSpinner();

        setupListeners();

        buttonLoadReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewReloadReport.setVisibility(View.GONE);
                scrollViewReportItems.setVisibility(View.VISIBLE);
                setItems();
            }
        });
    }

    public void clearAdapter(){
        textViewReloadReport.setVisibility(View.VISIBLE);
        scrollViewReportItems.setVisibility(View.GONE);
    }

    private void setupMonthSpinner() {
        ArrayAdapter<String> startMonthAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_item_large, getMonths(0, yearIsCurrent() ? DateUtil.monthNumber() + 1 : 12));
        spinnerMonth.setAdapter(startMonthAdapter);

        if(yearIsCurrent()) {
            Calendar calendar = Calendar.getInstance();
            spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        }
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
                setupMonthSpinner();
                clearAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerYear.setOnItemSelectedListener(listener);
    }

    private boolean yearIsCurrent() {
        SimpleDateFormat dateFormatYYYY = new SimpleDateFormat("yyyy");
        return spinnerYear.getSelectedItem().toString()
                .equalsIgnoreCase(dateFormatYYYY.format(new Date()));
    }

    protected List<String> getMonths(int startIndex, int endIndex) {
        List<String> months = getMonths();
        startIndex = startIndex < 0 ? 0 : startIndex;
        return months.subList(startIndex, endIndex);
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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            outerLayout.setLayoutParams(params);
            outerLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView textView = new TextView(context);
            textView.setWidth(150);
            textView.setText(reportItem.getCommodityName());
            textView.setTextColor(getResources().getColor(R.color.black));
            outerLayout.addView(textView);

            NonScrollListView listViewItemNames = new NonScrollListView(context);
            listViewItemNames.setAdapter(new MonthlyVaccineUtilizationReportAdapter(context,
                    R.layout.monthly_vaccine_utilization_report_item, reportItem.getUtilizationItems(), true));
            LinearLayout.LayoutParams paramsListView = new LinearLayout.LayoutParams(240, ViewGroup.LayoutParams.MATCH_PARENT);
            listViewItemNames.setLayoutParams(paramsListView);
            outerLayout.addView(listViewItemNames);

            linearLayoutCommodityNamesAndItemNames.addView(outerLayout);


            NonScrollListView listViewItemValues = new NonScrollListView(context);
            LinearLayout.LayoutParams paramsListViewValues = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
