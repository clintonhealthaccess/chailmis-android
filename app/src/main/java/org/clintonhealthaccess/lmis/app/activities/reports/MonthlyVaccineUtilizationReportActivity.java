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
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.ReportType;
import org.clintonhealthaccess.lmis.app.services.ReportsService;

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

    @InjectView(R.id.listViewReport)
    private ListView listViewReport;

    private Category category;

    private ArrayAdapter adapter;

    public static final int NUMBER_OF_YEARS = 10;

    @Inject
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_vaccine_utilization_report);
        textViewReportName.setText(ReportType.MonthlyHealthFacilityVaccinesUtilizationReport.getName());

        category = (Category) getIntent().getSerializableExtra(ReportsActivity.CATEGORY_BUNDLE_KEY);
        setupCommodities(category);

        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_black, getLastNYears(NUMBER_OF_YEARS));
        ArrayAdapter<String> startMonthAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item_black, getMonths());
        spinnerMonth.setAdapter(startMonthAdapter);

        Calendar calendar = Calendar.getInstance();
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH));
        spinnerYear.setAdapter(yearsAdapter);

        setupListeners();
        adapter = new ArrayAdapter(context, R.layout.notification_message_item, new ArrayList());
//        setItems();
        setupListViewHeader();
        listViewReport.setAdapter(adapter);
    }

    private void setupCommodities(Category category) {
        Drawable commodityButtonBackground = createCommodityButtonBackground();
        LinearLayout commoditiesLayout = (LinearLayout) findViewById(R.id.layoutCommodities);
        for (final Commodity commodity: category.getCommodities()) {
            Button button = createCommoditySelectionButton(commodity, commodityButtonBackground);
            commoditiesLayout.addView(button);
        }
    }

    private Drawable createCommodityButtonBackground() {
        Drawable commodityButtonBackground = getResources().getDrawable(R.drawable.arrow_black_right);
        commodityButtonBackground.setBounds(0, 0, 20, 30);
        return commodityButtonBackground;
    }


    private Button createCommoditySelectionButton(final Commodity commodity, Drawable background) {
        Button button = new Button(this);
        button.setBackgroundResource(R.drawable.category_button_on_overlay);
        button.setCompoundDrawables(null, null, background, null);
        button.setText(commodity.getName());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return button;
    }


    private void setupListViewHeader() {
        listViewReport.addHeaderView(getLayoutInflater().inflate(R.layout.monthly_vaccine_utilization_report_header, null));
    }

     private void  setupListeners() {

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //setItems();
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

//    void setItems() {
//        //List<VaccineUtilizationReportItem> vaccineUtilizationItems = reportsService.getVaccineUtilizationItems(category, getYear(), getMonth());
//        adapter.clear();
//        adapter.addAll(new ArrayList<>());
//        adapter.notifyDataSetChanged();
//    }

    protected String getYear() {
        return (String) spinnerYear.getSelectedItem();
    }

    protected String getMonth() {
        return (String) spinnerMonth.getSelectedItem();
    }

}
