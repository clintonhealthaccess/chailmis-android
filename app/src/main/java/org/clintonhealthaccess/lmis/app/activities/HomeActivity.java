package org.clintonhealthaccess.lmis.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.services.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;

@ContentView(R.layout.activity_home)
public class HomeActivity extends BaseActivity {
    @InjectView(R.id.layoutGraph)
    LinearLayout layout;

    @InjectView(R.id.listViewAlerts)
    ListView listViewAlerts;

    @InjectView(R.id.listViewNotifications)
    ListView listViewNotifications;

    @InjectView(R.id.buttonDispense)
    Button buttonDispense;

    @InjectView(R.id.buttonReceive)
    Button buttonReceive;

    @InjectView(R.id.buttonOrder)
    Button buttonOrder;

    @InjectView(R.id.buttonLosses)
    Button buttonLosses;

    @InjectView(R.id.buttonReports)
    Button buttonReports;

    @InjectView(R.id.buttonMessages)
    Button buttonMessages;

    @Inject
    private UserService userService;

    private Map<Integer, Class<? extends BaseActivity>> buttonRoutes =
            new HashMap<Integer, Class<? extends BaseActivity>>() {
                {
                    put(R.id.buttonDispense, DispenseActivity.class);
                    put(R.id.buttonReceive, ReceiveActivity.class);
                    put(R.id.buttonOrder, OrderActivity.class);
                    put(R.id.buttonLosses, LossesActivity.class);
                    put(R.id.buttonMessages, MessagesActivity.class);
                    put(R.id.buttonReports, ReportsActivity.class);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!userService.userRegistered()) {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        }

        setupButtonEvents();

        setupGraph();
        setupAlerts();
    }

    private void setupButtonEvents() {
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                Class<? extends BaseActivity> activityClass = buttonRoutes.get(view.getId());
                Intent intent = new Intent(getApplicationContext(), activityClass);
                startActivity(intent);
            }
        };
        ImmutableList<Button> navigationButtons =
                ImmutableList.of(buttonDispense, buttonOrder, buttonReceive, buttonLosses, buttonMessages, buttonReports);
        for (Button navigationButton : navigationButtons) {
            navigationButton.setOnClickListener(onClickListener);
        }
    }

    private void setupAlerts() {
        String[] values = new String[]{"Low Stock for Coartem", "Low Stock for Panadol", "Low Stock for Hedex"};

        ArrayList<String> list = new ArrayList<String>();
        Collections.addAll(list, values);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list);
        listViewAlerts.setAdapter(adapter);

        String[] notificationValues = new String[]{"You Have a new Allocation"};

        ArrayList<String> notificationList = new ArrayList<String>();
        Collections.addAll(notificationList, notificationValues);
        ArrayAdapter notificationAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, notificationList);
        listViewNotifications.setAdapter(notificationAdapter);
    }

    private void setupGraph() {
        GraphView graphView = new BarGraphView(this, "Commodity Consumption");
        GraphViewSeries exampleSeries = new GraphViewSeries(new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(1, 3.0d),
                new GraphView.GraphViewData(2, 12d),
                new GraphView.GraphViewData(3, 4d),
                new GraphView.GraphViewData(4, 10d),
                new GraphView.GraphViewData(5, 6d)
        });
        graphView.addSeries(exampleSeries);

        layout.addView(graphView);

    }


}
