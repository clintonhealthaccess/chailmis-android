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

package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.google.inject.Inject;
import com.thoughtworks.dhis.models.DataElementType;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.AlertsAdapter;
import org.clintonhealthaccess.lmis.app.adapters.NotificationMessageAdapter;
import org.clintonhealthaccess.lmis.app.listeners.AlertClickListener;
import org.clintonhealthaccess.lmis.app.listeners.NotificationClickListener;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
import org.clintonhealthaccess.lmis.app.models.alerts.NotificationMessage;
import org.clintonhealthaccess.lmis.app.services.AlertsService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.sync.SyncManager;
import org.clintonhealthaccess.lmis.app.views.graphs.StockOnHandGraphBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.ImmutableList.of;

@ContentView(R.layout.activity_home)
public class HomeActivity extends BaseActivity {
    public static final String IS_ADJUSTMENT = "is_adjustment";
    @InjectView(R.id.layoutGraph)
    LinearLayout layout;

    @InjectView(R.id.buttonDispense)
    Button buttonDispense;

    @InjectView(R.id.buttonReceive)
    Button buttonReceive;

    @InjectView(R.id.buttonOrder)
    Button buttonOrder;

    @InjectView(R.id.buttonLosses)
    Button buttonLosses;

    @InjectView(R.id.buttonAdjustments)
    Button buttonAdjustments;

    @InjectView(R.id.buttonReports)
    Button buttonReports;

    @InjectView(R.id.buttonMessages)
    Button buttonMessages;


    @InjectView(R.id.listViewAlerts)
    ListView listViewAlerts;

    @InjectView(R.id.listViewNotifications)
    ListView listViewNotifications;

    @Inject
    private SyncManager syncManager;

    @Inject
    AlertsService alertsService;

    @Inject
    CommodityService commodityService;

    private SparseArray<Class<? extends BaseActivity>> navigationRoutes =
            new SparseArray<Class<? extends BaseActivity>>() {
                {
                    put(R.id.buttonDispense, DispenseActivity.class);
                    put(R.id.buttonReceive, ReceiveActivity.class);
                    put(R.id.buttonOrder, OrderActivity.class);
                    put(R.id.buttonLosses, LossesActivity.class);
                    put(R.id.buttonMessages, MessagesActivity.class);
                    put(R.id.buttonReports, ReportsActivity.class);
                    put(R.id.buttonAdjustments, AdjustmentsActivity.class);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setupButtonEvents();
        setupGraph();

        setupAutoSync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupAlerts();

    }

    private void setupAutoSync() {
        syncManager.kickOff();
    }

    private void setupButtonEvents() {
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), navigationRoutes.get(view.getId())));
            }
        };

        asignListenerToButtons(onClickListener);

    }

    private void asignListenerToButtons(OnClickListener onClickListener) {
        List<Button> navigationButtons = of(buttonDispense, buttonOrder, buttonReceive,
                buttonLosses, buttonAdjustments, buttonMessages, buttonReports);
        for (Button navigationButton : navigationButtons) {
            navigationButton.setOnClickListener(onClickListener);
        }
    }


    private void setupAlerts() {
        AsyncTask<Void, Void, List<LowStockAlert>> getAlerts = createAlertsTask();
        getAlerts.execute();

        AsyncTask<Void, Void, List<? extends NotificationMessage>> getNotificationsMessageTask = createNotifcationsMessageTask();

        getNotificationsMessageTask.execute();
    }

    private AsyncTask<Void, Void, List<LowStockAlert>> createAlertsTask() {
        return new AsyncTask<Void, Void, List<LowStockAlert>>() {
            @Override
            protected List<LowStockAlert> doInBackground(Void[] params) {
                return alertsService.getTop5LowStockAlerts();
            }

            @Override
            protected void onPostExecute(List<LowStockAlert> lowStockAlerts) {
                super.onPostExecute(lowStockAlerts);
                AlertsAdapter adapter = new AlertsAdapter(getApplicationContext(), R.layout.alert_list_item, lowStockAlerts);
                listViewAlerts.setAdapter(adapter);
                listViewAlerts.setOnItemClickListener(new AlertClickListener(adapter, HomeActivity.this));
            }
        };
    }

    private AsyncTask<Void, Void, List<? extends NotificationMessage>> createNotifcationsMessageTask() {
        return new AsyncTask<Void, Void, List<? extends NotificationMessage>>() {
            @Override
            protected List<? extends NotificationMessage> doInBackground(Void... params) {
                return alertsService.getNotificationMessagesForHomePage();
            }

            @Override
            protected void onPostExecute(List<? extends NotificationMessage> notificationMessages) {
                NotificationMessageAdapter adapter = new NotificationMessageAdapter(getApplicationContext(), R.layout.notification_message_item, notificationMessages);
                listViewNotifications.setAdapter(adapter);
                listViewNotifications.setOnItemClickListener(new NotificationClickListener(adapter, HomeActivity.this));
            }
        };
    }

    private void setupGraph() {
        List<StockOnHandGraphBar> bars = new ArrayList<>();

        Map<Integer, Integer> colors = new HashMap<>();
        colors.put(0, getResources().getColor(R.color.chart0));
        colors.put(1, getResources().getColor(R.color.chart1));
        colors.put(2, getResources().getColor(R.color.chart2));
        colors.put(3, getResources().getColor(R.color.chart3));
        colors.put(4, getResources().getColor(R.color.chart4));
        colors.put(5, getResources().getColor(R.color.chart5));

        int count = 0;
        for (Commodity commodity : commodityService.getMost5HighlyConsumedCommodities()) {
            bars.add(new StockOnHandGraphBar(commodity.getName(), commodity.getLatestValueFromCommodityActionByName(DataElementType.MINIMUM_STOCK_LEVEL.toString()), commodity.getLatestValueFromCommodityActionByName(DataElementType.MAXIMUM_STOCK_LEVEL.toString()), commodity.getLatestValueFromCommodityActionByName(DataElementType.MONTHS_OF_STOCK_ON_HAND.toString()), colors.get(count), commodity.getStockOnHand()));
            count++;
        }
        LinearLayout barChartLayout = (LinearLayout) findViewById(R.id.barChart);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int graphHeight = 2 * height / 3;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, graphHeight);
        barChartLayout.setLayoutParams(params);
        int biggestValue = getBigestValue(bars);

        for (StockOnHandGraphBar bar : bars) {
            barChartLayout.addView(bar.getView(getApplicationContext(), biggestValue, graphHeight));
        }
    }

    private int getBigestValue(List<StockOnHandGraphBar> bars) {
        int biggestValue = 0;
        for (StockOnHandGraphBar bar : bars) {
            biggestValue = checkValue(biggestValue, bar.getMaximumThreshold());
            biggestValue = checkValue(biggestValue, bar.getMinimumThreshold());
            biggestValue = checkValue(biggestValue, bar.getMonthsOfStockOnHand());
        }
        return biggestValue;
    }

    private int checkValue(int biggestValue, int maximumThreshold) {
        if (biggestValue < maximumThreshold) {
            biggestValue = maximumThreshold;
        }
        return biggestValue;
    }


}
