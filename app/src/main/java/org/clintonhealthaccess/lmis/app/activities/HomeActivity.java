/*
 * Copyright (c) 2014, Clinton Health Access Initiative
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
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.sync.SyncManager;

import java.util.List;

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
        Crashlytics.start(this);
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

        buttonAdjustments.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(IS_ADJUSTMENT, true);
                Intent intent = new Intent(getApplicationContext(), DispenseActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void setupAlerts() {

    }

    private void setupGraph() {


    }

}
