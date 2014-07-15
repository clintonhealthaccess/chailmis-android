package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.app.sync.SyncManager;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.ImmutableList.of;

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

    @Inject
    private SyncManager syncManager;

    private SparseArray<Class<? extends BaseActivity>> navigationRoutes =
            new SparseArray<Class<? extends BaseActivity>>() {
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
            finish();
        }

        setupButtonEvents();
        setupGraph();
        setupAlerts();
        setupAutoSync();
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

        List<Button> navigationButtons = of(buttonDispense, buttonOrder, buttonReceive,
                buttonLosses, buttonMessages, buttonReports);
        for (Button navigationButton : navigationButtons) {
            navigationButton.setOnClickListener(onClickListener);
        }
    }

    private void setupAlerts() {

    }

    private void setupGraph() {


    }

}
