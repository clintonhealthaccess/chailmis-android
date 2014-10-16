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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.OrderService;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.app.sms.SmsSyncService;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static android.widget.Toast.LENGTH_SHORT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.clintonhealthaccess.lmis.app.R.id;
import static org.clintonhealthaccess.lmis.app.R.layout;

public class RegisterActivity extends RoboActionBarActivity {
    @Inject
    private UserService userService;

    @Inject
    private CommodityService commodityService;

    @Inject
    private StockService stockService;

    @Inject
    private OrderService orderService;

    @Inject
    private SmsSyncService smsSyncService;

    @InjectView(id.textUsername)
    private TextView textUsername;

    @InjectView(id.textPassword)
    private TextView textPassword;

    @InjectResource(R.string.registration_successful_message)
    private String registrationSuccessfulMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_register);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        Button registerButton = (Button) findViewById(id.buttonRegister);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndRegister();
            }
        });
    }

    private void validateAndRegister() {
        String username = textUsername.getText().toString();
        String password = textPassword.getText().toString();
        if (isBlank(username)) {
            textUsername.setError(getString(R.string.username_error));
            return;
        }
        if (isBlank(password)) {
            textPassword.setError(getString(R.string.password_error));
            return;
        }

        doRegister(username, password);
    }

    protected void doRegister(final String username, final String password) {
        AsyncTask<Void, Void, Boolean> registerTask = new RegisterTask(username, password);
        registerTask.execute();
    }

    private class RegisterTask extends AsyncTask<Void, Void, Boolean> {
        private final String username;
        private final String password;
        private Exception failureCause;

        private ProgressDialog dialog;

        public RegisterTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(RegisterActivity.this);
            this.dialog.setMessage("Registering");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            User user;
            try {
                user = userService.register(username, password);
                commodityService.initialise(user);
                orderService.syncOrderReasons();
                orderService.syncOrderTypes();
                smsSyncService.syncGatewayNumber();
            } catch (Exception e) {
                this.failureCause = e;
                Log.e("Registration Error", e.getLocalizedMessage());
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean succeeded) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (succeeded) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }

            String toastMessage = succeeded ? registrationSuccessfulMessage : failureCause.getMessage();
            Toast.makeText(getApplicationContext(), toastMessage, LENGTH_SHORT).show();
            Log.e("Registration", toastMessage);
        }
    }
}
