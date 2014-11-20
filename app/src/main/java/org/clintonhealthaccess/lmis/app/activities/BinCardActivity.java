/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

package org.clintonhealthaccess.lmis.app.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.BinCardAdapter;
import org.clintonhealthaccess.lmis.app.adapters.BinCardItemHeaderAdapter;
import org.clintonhealthaccess.lmis.app.adapters.SearchCommodityAdapter;
import org.clintonhealthaccess.lmis.app.adapters.SpinnerCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.models.BinCardHeader;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.reports.BinCard;
import org.clintonhealthaccess.lmis.app.models.reports.BinCardItem;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.ReportsService;
import org.clintonhealthaccess.lmis.app.views.LmisProgressDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static android.widget.AdapterView.OnItemSelectedListener;
import static com.google.common.collect.Lists.newArrayList;

@ContentView(R.layout.activity_bin_card)
public class BinCardActivity extends BaseActivity {

    @InjectView(R.id.spinnerCommodities)
    Spinner spinnerCommodities;

    @InjectView(R.id.autoCompleteTextViewCommodities)
    public AutoCompleteTextView autoCompleteTextViewCommodities;

    @InjectView(R.id.textViewMinimumStock)
    TextView textViewMinimumStock;

    @InjectView(R.id.textViewMaximumStock)
    TextView textViewMaximumStock;

    @InjectView(R.id.listViewBinCardItems)
    ListView listViewBinCarditems;

    @InjectView(R.id.listViewHeaderItems)
    ListView listViewHeaderitems;

    @InjectView(R.id.linearLayoutSearchContainer)
    LinearLayout linearLayoutSearchContainer;

    @InjectView(R.id.buttonLoadBinCard)
    Button buttonLoadReport;

    @InjectView(R.id.textViewBeforeLoad)
    TextView textViewBeforeLoad;

    @InjectView(R.id.linearLayoutBinCardItems)
    LinearLayout linearLayoutBinCardItems;

    @Inject
    CommodityService commodityService;
    @Inject
    private ReportsService reportsService;

    public SearchCommodityAdapter searchCommodityAdapter;
    private List<Commodity> commodities;
    private BinCardAdapter binCardAdapter;
    int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.transparent);

        commodities = commodityService.all();
        setUpAdapters();
        populateCommoditiesSpinner();
        setUpCommoditySearch();

        buttonLoadReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewBeforeLoad.setVisibility(View.GONE);
                linearLayoutBinCardItems.setVisibility(View.VISIBLE);
                hideKeyboard();
                populateBinCard((Commodity) spinnerCommodities.getItemAtPosition(currentPosition));
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(buttonLoadReport.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void setUpAdapters() {
        binCardAdapter = new BinCardAdapter(getApplicationContext(),
                R.layout.bin_card_item, new ArrayList<BinCardItem>());
        listViewBinCarditems.setAdapter(binCardAdapter);

        BinCardHeader binCardHeader
                = new BinCardHeader(getString(R.string.date),"Received from/Issued to",
                getString(R.string.quantity_received), getString(R.string.quantity_dispensed),
                getString(R.string.quantity_lost), getString(R.string.quantity_adjusted), getString(R.string.stock_balance));

        BinCardItemHeaderAdapter headerAdapter
                = new BinCardItemHeaderAdapter(getApplicationContext(), R.layout.bin_card_item, Arrays.asList(binCardHeader));
        listViewHeaderitems.setAdapter(headerAdapter);
    }

    private void populateCommoditiesSpinner() {
        ArrayAdapter<Commodity> spinnerCommoditiesAdapter = new SpinnerCommoditiesAdapter(getApplicationContext(),
                R.layout.spinner_item, commodities);
        spinnerCommodities.setAdapter(spinnerCommoditiesAdapter);
        spinnerCommodities.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    protected void setUpCommoditySearch() {
        //stop search field from getting default focus
        linearLayoutSearchContainer.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        linearLayoutSearchContainer.setFocusableInTouchMode(true);

        searchCommodityAdapter = new SearchCommodityAdapter(this, R.layout.search_commodity_item, newArrayList(commodities));
        autoCompleteTextViewCommodities.setAdapter(searchCommodityAdapter);
        autoCompleteTextViewCommodities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Commodity commodity = searchCommodityAdapter.getItem(position);
                spinnerCommodities.setSelection(commodities.indexOf(commodity));
                autoCompleteTextViewCommodities.setText("");
            }
        });
    }

    public void populateBinCard(Commodity commodity) {
        new BinCardAsyncTask().execute(commodity);
    }

    public void handleBinCard(BinCard binCard){
        this.textViewMinimumStock.setText(String.valueOf(binCard.getMinimumStockLevel()));
        this.textViewMaximumStock.setText(String.valueOf(binCard.getMaximumStockLevel()));
        this.binCardAdapter.clear();
        this.binCardAdapter.addAll(binCard.getBinCardItems());
        this.binCardAdapter.notifyDataSetChanged();
    }

    private class BinCardAsyncTask extends AsyncTask<Commodity, Void, BinCard> {
        LmisProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new LmisProgressDialog(BinCardActivity.this, getString(R.string.loading_report));
            this.dialog.show();
        }

        @Override
        protected BinCard doInBackground(Commodity... commodities) {
            try {
                return reportsService.generateBinCard(commodities[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BinCard binCard) {
            if(binCard==null){
                Toast.makeText(BinCardActivity.this, getString(R.string.report_generation_error), Toast.LENGTH_LONG).show();
                dialog.dismiss();
                return;
            }
            handleBinCard(binCard);
            dialog.dismiss();
        }
    }
}
