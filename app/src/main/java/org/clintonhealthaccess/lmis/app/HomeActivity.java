package org.clintonhealthaccess.lmis.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import roboguice.activity.RoboActionBarActivity;


public class HomeActivity extends RoboActionBarActivity {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final int DATE_TIME_ID = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}
