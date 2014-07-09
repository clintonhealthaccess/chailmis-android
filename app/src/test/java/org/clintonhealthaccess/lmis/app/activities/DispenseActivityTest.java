package org.clintonhealthaccess.lmis.app.activities;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.SelectedCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestFixture.initialiseDefaultCommodities;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class DispenseActivityTest {
    private DispenseActivity getActivity() {
        return buildActivity(DispenseActivity.class).create().get();
    }

    @Before
    public void setUp() throws Exception {
        initialiseDefaultCommodities(application);
    }

    @Test
    public void testBuildActivity() throws Exception {
        DispenseActivity activity = getActivity();
        assertThat(activity, not(nullValue()));
        TextView textViewAppName = (TextView) activity.getActionBar().getCustomView().findViewById(R.id.textAppName);
        assertThat(textViewAppName, is(notNullValue()));
        assertThat(textViewAppName.getText().toString(), is(activity.getResources().getString(R.string.app_name)));
    }

    @Test
    public void testShouldDisplayAllCategoriesAsButtons() throws Exception {
        DispenseActivity dispenseActivity = getActivity();

        LinearLayout categoryLayout = (LinearLayout) dispenseActivity.findViewById(R.id.layoutCategories);
        int buttonAmount = categoryLayout.getChildCount();
        assertThat(buttonAmount, is(6));

        for (int i = 0; i < buttonAmount; i++) {
            View childView = categoryLayout.getChildAt(i);
            assertThat(childView, instanceOf(Button.class));
        }
    }

    @Test
    public void shouldToggleSelectedItemsWhenToggleEventIsTriggered() throws Exception {
        DispenseActivity dispenseActivity = getActivity();
        Commodity commodity = new Commodity("name");
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodity);

        EventBus.getDefault().post(commodityToggledEvent);

        assertThat(dispenseActivity.selectedCommodities, contains(commodity));

        EventBus.getDefault().post(commodityToggledEvent);

        assertThat(dispenseActivity.selectedCommodities, not(contains(commodity)));
    }


    @Test
    public void listViewShouldToggleCommodityWhenToggleEventIsTriggered() throws Exception {
        DispenseActivity dispenseActivity = getActivity();
        Commodity commodity = new Commodity("name");
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodity);
        EventBus.getDefault().post(commodityToggledEvent);

        Commodity commodityInList = (Commodity) dispenseActivity.listViewSelectedCommodities.getAdapter().getItem(0);

        assertThat(commodityInList, is(commodity));

        EventBus.getDefault().post(commodityToggledEvent);

        assertThat(dispenseActivity.listViewSelectedCommodities.getAdapter().getCount(), is(0));
    }

    @Test
    public void testThatSubmitButtonIsWiredUp() throws Exception {
        DispenseActivity activity = getActivity();
        assertThat(activity.buttonSubmitDispense, is(notNullValue()));
    }

    @Test
    public void shouldRemoveSelectedCommodityFromListWhenCancelButtonIsClicked() {
        DispenseActivity dispenseActivity = getActivity();
        Commodity commodity = new Commodity("name");
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodity);
        EventBus.getDefault().post(commodityToggledEvent);

        SelectedCommoditiesAdapter adapter = dispenseActivity.selectedCommoditiesAdapter;

        ImageButton cancelButton = (ImageButton) getViewFromListRow(adapter, R.layout.selected_commodity_list_item, R.id.imageButtonCancel);

        cancelButton.performClick();

        assertFalse(dispenseActivity.selectedCommodities.contains(commodity));
        assertThat(dispenseActivity.listViewSelectedCommodities.getAdapter().getCount(), is(0));
    }

    @Test
    public void getDispensingShouldGetItemsInTheListView() throws Exception {

        DispenseActivity dispenseActivity = getActivity();

        String commodityName = "food";

        dispenseActivity.selectedCommoditiesAdapter.add(new Commodity(commodityName));
        dispenseActivity.selectedCommoditiesAdapter.add(new Commodity("commodity two"));

        dispenseActivity.selectedCommoditiesAdapter.notifyDataSetChanged();

        dispenseActivity.listViewSelectedCommodities.setAdapter(dispenseActivity.selectedCommoditiesAdapter);

        Dispensing dispensing = dispenseActivity.getDispensing();

        assertThat(dispensing.getDispensingItems().size(), is(2));
        assertThat(dispensing.getDispensingItems().get(0).getCommodity().getName(), is(commodityName));
    }

    @Test
    public void testSubmitButtonShouldBeHiddenIfThereAreNoItemsInTheList() throws Exception {

        DispenseActivity dispenseActivity = getActivity();

        assertFalse(dispenseActivity.buttonSubmitDispense.getVisibility() == View.VISIBLE);

        dispenseActivity.selectedCommodities.add(new Commodity("commodity one"));


        dispenseActivity.checkVisibilityOfSubmitButton();

        assertTrue(dispenseActivity.buttonSubmitDispense.getVisibility() == View.VISIBLE);


    }
}
