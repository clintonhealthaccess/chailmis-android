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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.BaseActivity;
import org.clintonhealthaccess.lmis.app.activities.ReportsActivity;
import org.clintonhealthaccess.lmis.app.adapters.FacilityStockReportAdapter;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.reports.FacilityStockReportItem;
import org.clintonhealthaccess.lmis.app.services.ReportsService;
import org.clintonhealthaccess.lmis.app.services.UserService;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import roboguice.inject.InjectView;

public class FacilityStockReportActivity extends BaseActivity {


    @InjectView(R.id.spinnerStartingMonth)
    Spinner spinnerStartingMonth;

    @InjectView(R.id.spinnerEndingMonth)
    Spinner spinnerEndingMonth;

    @InjectView(R.id.spinnerYear)
    Spinner spinnerYear;

    @InjectView(R.id.textViewReportName)
    TextView textViewReportName;

    @InjectView(R.id.listViewReport)
    ListView listViewReport;

    @Inject
    UserService userService;

    @Inject
    ReportsService reportsService;

    private Category category;
    private FacilityStockReportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_facility_stock_report);

        category = (Category) getIntent().getSerializableExtra(ReportsActivity.CATEGORY_BUNDLE_KEY);
        adapter = new FacilityStockReportAdapter(getApplicationContext(), R.layout.facility_stock_report_item, new ArrayList<FacilityStockReportItem>());
        textViewReportName.setText(String.format("Facility Stock Report for %s", category.getName()));

        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_black, getLastNYears(10));
        ArrayAdapter<String> startMonthAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_black, getMonths());
        spinnerStartingMonth.setAdapter(startMonthAdapter);
        spinnerStartingMonth.setSelection(0);

        setupEndMonthSpinner();

        spinnerYear.setAdapter(yearsAdapter);

        setupListeners();
        setItems();

        listViewReport.addHeaderView(getLayoutInflater().inflate(R.layout.facility_stock_report_header, null));
        listViewReport.setAdapter(adapter);
    }

    private void setupEndMonthSpinner() {
        int selectedIndex = spinnerStartingMonth.getSelectedItemPosition();
        Log.e("Selected Item", String.valueOf(selectedIndex));
        ArrayAdapter<String> endMonthAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_black, getMonths(selectedIndex));
        spinnerEndingMonth.setAdapter(endMonthAdapter);
    }

    private List<String> getMonths(int selectedIndex) {

        List<String> months = getMonths();
        if (selectedIndex >= 0) {
            return months.subList(selectedIndex, 12);
        }
        return months;
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

        spinnerEndingMonth.setOnItemSelectedListener(listener);

        spinnerYear.setOnItemSelectedListener(listener);

        spinnerStartingMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setItems();
                setupEndMonthSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setItems() {
        List<FacilityStockReportItem> facilityReportItemsForCategory = reportsService.getFacilityReportItemsForCategory(category, getYear(), getStartingMonth(), getEndingMonth());
        adapter.clear();
        adapter.addAll(facilityReportItemsForCategory);
        adapter.notifyDataSetChanged();
    }

    private String getYear() {
        return (String) spinnerYear.getSelectedItem();
    }

    private String getStartingMonth() {
        return (String) spinnerStartingMonth.getSelectedItem();
    }

    private String getEndingMonth() {
        return (String) spinnerEndingMonth.getSelectedItem();
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

    private ArrayList<String> getLastNYears(int numberOfYears) {
        ArrayList<String> years = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        for (int i = 0; i < numberOfYears; i++) {
            years.add(String.valueOf(currentYear - i));
        }
        return years;
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.transparent);
    }


}
