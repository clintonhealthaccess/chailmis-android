package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.inject.Inject;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.services.UserService;

import java.util.ArrayList;
import java.util.Collections;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

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

    private void setupAlerts() {

    }

    private void setupGraph() {


    }


}
