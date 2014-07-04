package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.services.ServiceException;
import org.clintonhealthaccess.lmis.app.services.UserService;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.clintonhealthaccess.lmis.app.R.id;
import static org.clintonhealthaccess.lmis.app.R.layout;

public class RegisterActivity extends RoboActionBarActivity {
    @Inject
    private UserService userService;

    @InjectView(id.textUsername)
    TextView textUsername;

    @InjectView(id.textPassword)
    TextView textPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_register);

        Button registerButton = (Button) findViewById(id.buttonRegister);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
    }

    private void doRegister(final String username, final String password) {
        AsyncTask<Void, Void, Boolean> registerTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    userService.register(username, password);
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                } catch (ServiceException e) {
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean succeeded) {
                if (succeeded) {
                    Toast.makeText(getApplicationContext(), getString(R.string.registration_successful_message),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.registration_failed_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerTask.execute();
    }

    private String getTextFromInputField(int inputFieldId) {
        TextView inputField = (TextView) findViewById(inputFieldId);
        return valueOf(inputField.getText());
    }


}
