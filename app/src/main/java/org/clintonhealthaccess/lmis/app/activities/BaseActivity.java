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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.events.SyncedEvent;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.AlertsService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.app.sync.SyncManager;

import de.greenrobot.event.EventBus;

import static android.widget.Toast.LENGTH_SHORT;

public class BaseActivity extends OrmLiteActivity {
    @Inject
    UserService userService;

    @Inject
    SyncManager syncManager;

    @Inject
    AlertsService alertsService;

    @Inject
    SharedPreferences sharedPreferences;

    TextView textFacilityName;

    public String facility2LetterCode;

    public static final String DATE_FORMAT = "dd MMMM yyyy";

    protected View menuAlertItem;
    private static boolean manualSyncFinishing = false;

    protected void setFacilityName(String text) {
        textFacilityName.setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        textFacilityName = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.textFacilityName);
        if (!userService.userRegistered()) {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        } else {
            User user = userService.getRegisteredUser();
            String facilityName = user.getFacilityName() == null ? "" : user.getFacilityName();
            setFacilityName(facilityName);
            facility2LetterCode = facilityName.length() > 2 ?
                    facilityName.substring(0, 2).toUpperCase() :
                    facilityName.toUpperCase();
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater menuInflater = getMenuInflater();
            super.onCreateOptionsMenu(menu);
            menuInflater.inflate(R.menu.home, menu);
            final View alert_menu_item = menu.findItem(R.id.action_alert).getActionView();
            this.menuAlertItem = alert_menu_item;
            setupAlertButton(alert_menu_item);
            updateAlertCount();
            menu.add(getDate()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        } catch (Exception e) {
            //Exception when running tests
        }
        return true;
    }

    private String getLastSyncedTime() {
        return sharedPreferences.getString("Last_sync_time", null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(SyncedEvent syncedEvent) {
        manualSyncFinishing = false;
        invalidateOptionsMenu();
        Toast.makeText(this, "Sync data with server successfully!", Toast.LENGTH_LONG).show();
    }

    public void updateAlertCount(){
        if (this.menuAlertItem !=null ){
            setupAlertCount(this.menuAlertItem);
        }
    }


    private void setupAlertButton(View menu_hotlist) {
        menu_hotlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupAlertCount(View menu_hotlist) {
        final TextView textViewnumberOfAlerts = (TextView) menu_hotlist.findViewById(R.id.textViewAlertNumber);
        updateAlertCount(0, textViewnumberOfAlerts);
        AsyncTask<Void, Void, Integer> updateAlertCount = getAlertsCountUpdateTask(textViewnumberOfAlerts);
        updateAlertCount.execute();
    }

    private AsyncTask<Void, Void, Integer> getAlertsCountUpdateTask(final TextView textViewnumberOfAlerts) {
        return new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void[] params) {
                return alertsService.numberOfAlerts();
            }

            @Override
            protected void onPostExecute(Integer o) {
                super.onPostExecute(o);
                updateAlertCount(o, textViewnumberOfAlerts);
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_alert) {
            return true;
        } else if (item.getItemId() == R.id.action_sync) {
            syncManager.requestSync();
            manualSyncFinishing = true;
            item.setTitle(getString(R.string.syncing));
        }
        return false;
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (!manualSyncFinishing) {
            MenuItem syncItem = menu.findItem(R.id.action_sync);
            if (syncItem != null) {
                syncItem.setTitle(R.string.sync);
            }
        }

        String lastSyncedTime = getLastSyncedTime();
        if (StringUtils.isNotBlank(lastSyncedTime)) {
            MenuItem item = menu.findItem(R.id.action_last_sync_time);
            item.setTitle("last sync at:" + lastSyncedTime);
            item.setVisible(true);
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    public void updateAlertCount(final int value, final TextView textView) {
        if (textView == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value == 0)
                    textView.setVisibility(View.INVISIBLE);
                else {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(Integer.toString(value));
                }
            }
        });
    }

    private String getDate() {
        return android.text.format.DateFormat.format(DATE_FORMAT, new java.util.Date()).toString();
    }


    void showToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, LENGTH_SHORT).show();
    }
}
