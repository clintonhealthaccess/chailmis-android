package org.clintonhealthaccess.lmis.app;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import roboguice.activity.RoboActionBarActivity;


public class HomeActivity extends RoboActionBarActivity {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    private TextView textFacilityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
        textFacilityName = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.textFacilityName);
        textFacilityName.setText("Kabira Health Center");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String dateString = android.text.format.DateFormat.format(DATE_FORMAT, new java.util.Date()).toString();
        getMenuInflater().inflate(R.menu.home, menu);
        menu.add(dateString).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
