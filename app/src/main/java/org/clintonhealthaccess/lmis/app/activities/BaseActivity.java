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
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.UserService;

import roboguice.activity.RoboActionBarActivity;

import static android.widget.Toast.LENGTH_SHORT;

public class BaseActivity extends RoboActionBarActivity {
    @Inject
    UserService userService;

    TextView textFacilityName;
    public static final String DATE_FORMAT = "dd MMMM yyyy";

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
            setFacilityName(user.getFacilityName());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater menuInflater = getMenuInflater();
            super.onCreateOptionsMenu(menu);
            menuInflater.inflate(R.menu.home, menu);
            final View menu_hotlist = menu.findItem(R.id.action_alert).getActionView();
            menu_hotlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
                    startActivity(intent);
                }
            });
            TextView textViewnumberOfAlerts = (TextView) menu_hotlist.findViewById(R.id.textViewAlertNumber);
            updateAlertCount(5, textViewnumberOfAlerts);
            menu.add(getDate()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        } catch (Exception e) {
            //Exception when running tests
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_alert) {

            return true;
        }
        return false;
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
