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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.BaseActivity;
import org.clintonhealthaccess.lmis.app.activities.ReportsActivity;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.services.ReportsService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.app.utils.DateUtil;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.inject.InjectView;

public abstract class MonthBasedReportBaseActivity<T extends ArrayAdapter> extends BaseActivity {

    public static final int NUMBER_OF_YEARS = 10;

    @InjectView(R.id.spinnerStartingMonth)
    Spinner spinnerStartingMonth;

    @InjectView(R.id.spinnerEndingMonth)
    Spinner spinnerEndingMonth;

    @InjectView(R.id.spinnerStartingYear)
    Spinner spinnerStartingYear;

    @InjectView(R.id.spinnerEndingYear)
    Spinner spinnerEndingYear;

    @InjectView(R.id.textViewReportName)
    TextView textViewReportName;

    @InjectView(R.id.listViewReport)
    ListView listViewReport;

    @InjectView(R.id.textViewEndingYear)
    TextView textViewEndingYear;

    @InjectView(R.id.textViewEndingMonth)
    TextView textViewEndingMonth;

    @InjectView(R.id.buttonLoadReport)
    Button buttonLoadReport;

    @InjectView(R.id.textViewBeforeLoad)
    TextView textViewBeforeLoad;

    @InjectView(R.id.textViewReloadReport)
    TextView textViewReloadReport;

    @InjectView(R.id.horizontalScrollView)
    HorizontalScrollView horizontalScrollView;

    @Inject
    UserService userService;

    @Inject
    ReportsService reportsService;

    Category category;

    T adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(getLayoutId());
        category = (Category) getIntent().getSerializableExtra(ReportsActivity.CATEGORY_BUNDLE_KEY);
        adapter = getAdapter();
        textViewReportName.setText(getReportName());
        setupSpinners();
        setupListViewHeader();
        listViewReport.setAdapter(adapter);
        afterCreate();
    }


    private void setupSpinners() {
        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_item_black, getLastNYears(NUMBER_OF_YEARS));
        spinnerStartingYear.setAdapter(yearsAdapter);
        spinnerStartingYear.setSelection(0);
        setupEndYearSpinner();

        ArrayAdapter<String> startMonthAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_item_black, getMonths());
        spinnerStartingMonth.setAdapter(startMonthAdapter);
        spinnerStartingMonth.setSelection(0);
        setupEndMonthSpinner();

        setupListeners();
        getStrategy().applyVisibilityStrategy(spinnerStartingMonth, spinnerEndingMonth, spinnerStartingYear, spinnerEndingYear, textViewEndingMonth, textViewEndingYear);
    }

    SpinnerVisibilityStrategy getStrategy() {
        return SpinnerVisibilityStrategy.allVisible;
    }


    void setupListViewHeader() {
        //listViewReport.addHeaderView(getLayoutInflater().inflate(getHeaderLayout(), null));
    }

    abstract String getReportName();

    abstract int getHeaderLayout();

    abstract T getAdapter();

    abstract int getLayoutId();

    abstract void setItems();

    abstract void afterCreate();

    private void setupListeners() {

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        spinnerEndingMonth.setOnItemSelectedListener(listener);

        spinnerStartingYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupEndYearSpinner();
                setupStartingMonthSpinner();
                clearAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerEndingYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupEndMonthSpinner();
                clearAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerStartingMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupEndMonthSpinner();
                clearAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void clearAdapter(){
        textViewReloadReport.setVisibility(View.VISIBLE);
        horizontalScrollView.setVisibility(View.GONE);
    }

    protected List<String> getMonths(int startIndex, int endIndex) {
        List<String> months = getMonths();
        startIndex = startIndex < 0 ? 0 : startIndex;
        return months.subList(startIndex, endIndex);
    }

    protected String getStartingYear() {
        return (String) spinnerStartingYear.getSelectedItem();
    }

    public String getEndingYear() {
        return (String) spinnerEndingYear.getSelectedItem();
    }

    protected String getStartingMonth() {
        return (String) spinnerStartingMonth.getSelectedItem();
    }

    protected String getEndingMonth() {
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

    private boolean startingYearIsCurrent() {
        SimpleDateFormat dateFormatYYYY = new SimpleDateFormat("yyyy");
        return spinnerStartingYear.getSelectedItem().toString()
                .equalsIgnoreCase(dateFormatYYYY.format(new Date()));
    }

    private boolean endingYearIsCurrent() {
        SimpleDateFormat dateFormatYYYY = new SimpleDateFormat("yyyy");
        if(spinnerEndingYear.getSelectedItem()==null) {
            spinnerStartingYear.setSelection(0);
        }
        return spinnerEndingYear.getSelectedItem().toString()
                .equalsIgnoreCase(dateFormatYYYY.format(new Date()));
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

    protected void setupEndYearSpinner() {
        int selectedIndex = spinnerStartingYear.getSelectedItemPosition();
        ArrayAdapter<String> endYearAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item_black, getLastNYears(NUMBER_OF_YEARS).subList(0, selectedIndex + 1));
        spinnerEndingYear.setAdapter(endYearAdapter);
    }

    protected void setupStartingMonthSpinner() {
        if (startingYearIsCurrent()) {
            ArrayAdapter<String> startMonthAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item_black,
                    getMonths(0, DateUtil.monthNumber() + 1));
            spinnerStartingMonth.setAdapter(startMonthAdapter);
        }else{
            ArrayAdapter<String> startMonthAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item_black,
                    getMonths());
            spinnerStartingMonth.setAdapter(startMonthAdapter);
        }
    }

    protected void setupEndMonthSpinner() {
        int selectedIndex = spinnerStartingMonth.getSelectedItemPosition();
        ArrayAdapter<String> endMonthAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item_black,
                getMonths(selectedIndex, endingYearIsCurrent() ? DateUtil.monthNumber() + 1 : 12));
        spinnerEndingMonth.setAdapter(endMonthAdapter);
    }

    protected void setupActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.transparent);
    }
}
