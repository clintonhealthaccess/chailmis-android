package org.clintonhealthaccess.lmis.app.activities;

import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class BaseActivityTest {


    private DispenseActivity getActivity() {
        return buildActivity(DispenseActivity.class).create().get();
    }

    @Test
    public void testBuildActivity() throws Exception {
        BaseActivity activity = getActivity();
        assertThat(activity, not(nullValue()));
    }

    @Test
    public void testLogoText() {
        BaseActivity activity = getActivity();
        TextView textViewAppName = (TextView) activity.getActionBar().getCustomView().findViewById(R.id.textAppName);
        assertThat(textViewAppName, is(notNullValue()));
        assertThat(textViewAppName.getText().toString(), is(activity.getResources().getString(R.string.app_name)));
    }
}
