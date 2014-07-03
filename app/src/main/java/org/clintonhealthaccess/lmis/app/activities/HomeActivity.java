package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

import org.clintonhealthaccess.lmis.app.R;

import java.util.ArrayList;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;


public class HomeActivity extends RoboActionBarActivity {

    public static final String DATE_FORMAT = "dd/MM/yyyy";

    private TextView textFacilityName;

    @InjectView(R.id.layoutGraph)
    private LinearLayout layout;

    @InjectView(R.id.listViewAlerts)
    private ListView listViewAlerts;

    @InjectView(R.id.listViewNotifications)
    private ListView listViewNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!userRegistered()) {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        }

        setContentView(R.layout.activity_home);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        getSupportActionBar().setCustomView(R.layout.action_bar);

        setFacilityName("Kabira Health Center");

        setupGraph();

        setupAlerts();
    }

    private boolean userRegistered() {
        // TODO: more logic goes here
        return true;
    }

    private void setFacilityName(String text) {
        textFacilityName = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.textFacilityName);
        textFacilityName.setText(text);
    }

    private void setupAlerts() {
        String[] values = new String[]{"Low Stock for Coartem", "Low Stock for Panadol", "Low Stock for Hedex"};

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list);
        listViewAlerts.setAdapter(adapter);


        String[] notificationValues = new String[]{"You Have a new Allocation"};

        ArrayList<String> notificationList = new ArrayList<String>();
        for (int j = 0; j < notificationValues.length; ++j) {
            notificationList.add(notificationValues[j]);
        }
        ArrayAdapter notificationAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, notificationList);
        listViewNotifications.setAdapter(notificationAdapter);
    }

    private void setupGraph() {
        GraphView graphView = new BarGraphView(
                this
                , "Commodity Consumption"
        );
        GraphViewSeries exampleSeries = new GraphViewSeries(new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(1, 3.0d)
                , new GraphView.GraphViewData(2, 12d)
                , new GraphView.GraphViewData(3, 4d)
                , new GraphView.GraphViewData(4, 10d)
                , new GraphView.GraphViewData(5, 6d)

        });
        graphView.addSeries(exampleSeries);

        layout.addView(graphView);
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
