package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;

import roboguice.activity.RoboActionBarActivity;

public class BaseActivity extends RoboActionBarActivity {
    TextView textFacilityName;
    public static final String DATE_FORMAT = "dd MMMM yyyy";

    protected void setFacilityName(String text) {
        textFacilityName = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.textFacilityName);
        textFacilityName.setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        setFacilityName("Kabira Health Center");
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
