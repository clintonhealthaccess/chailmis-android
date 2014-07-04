package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.services.UserService;

import java.util.ArrayList;
import java.util.Collections;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_home)
public class HomeActivity extends RoboActionBarActivity {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    TextView textFacilityName;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!userService.userRegistered()) {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        }

        setupButtonEvents();
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        setFacilityName("Kabira Health Center");
        setupGraph();
        setupAlerts();
    }

    private void setupButtonEvents() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.buttonDispense:
                        Intent intent = new Intent(getApplicationContext(), DispenseActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.buttonReceive:
                        Intent receiveIntent = new Intent(getApplicationContext(), ReceiveActivity.class);
                        startActivity(receiveIntent);
                        break;
                    case R.id.buttonOrder:
                        Intent orderIntent = new Intent(getApplicationContext(), OrderActivity.class);
                        startActivity(orderIntent);
                        break;
                    case R.id.buttonLosses:
                        Intent lossesIntent = new Intent(getApplicationContext(), LossesActivity.class);
                        startActivity(lossesIntent);
                        break;
                    case R.id.buttonMessages:
                        Intent messagesIntent = new Intent(getApplicationContext(), MessagesActivity.class);
                        startActivity(messagesIntent);
                        break;
                    case R.id.buttonReports:
                        Intent reportsIntent = new Intent(getApplicationContext(), ReportsActivity.class);
                        startActivity(reportsIntent);
                        break;

                }

            }
        };
        buttonReceive.setOnClickListener(onClickListener);
        buttonOrder.setOnClickListener(onClickListener);
        buttonLosses.setOnClickListener(onClickListener);
        buttonReports.setOnClickListener(onClickListener);
        buttonMessages.setOnClickListener(onClickListener);
        buttonDispense.setOnClickListener(onClickListener);
    }

    private void setFacilityName(String text) {
        textFacilityName = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.textFacilityName);
        textFacilityName.setText(text);
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
        textFacilityName = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.textFacilityName);
        textFacilityName.setText("Kabira Health Center");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        menu.add(getDate()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    private String getDate() {
        return android.text.format.DateFormat.format(DATE_FORMAT, new java.util.Date()).toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
