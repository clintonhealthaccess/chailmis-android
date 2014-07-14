package org.clintonhealthaccess.lmis.app.activities;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewModels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.SelectedCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;

import de.greenrobot.event.EventBus;

import static android.view.View.VISIBLE;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class DispenseActivityTest {

    @Inject
    private CommodityService commodityService;

    public static DispenseActivity getDispenseActivity() {
        return buildActivity(DispenseActivity.class).create().get();
    }

    @Before
    public void setUp() throws Exception {
        setUpInjection(this);
        commodityService.initialise();
    }

    @Test
    public void testBuildActivity() throws Exception {
        DispenseActivity activity = getDispenseActivity();
        assertThat(activity, not(nullValue()));
        TextView textViewAppName = (TextView) activity.getActionBar().getCustomView().findViewById(R.id.textAppName);
        assertThat(textViewAppName, is(notNullValue()));
        assertThat(textViewAppName.getText().toString(), is(activity.getResources().getString(R.string.app_name)));
    }

    @Test
    public void testShouldDisplayAllCategoriesAsButtons() throws Exception {
        DispenseActivity dispenseActivity = getDispenseActivity();

        LinearLayout categoryLayout = (LinearLayout) dispenseActivity.findViewById(R.id.layoutCategories);
        int buttonAmount = categoryLayout.getChildCount();
        assertThat(buttonAmount, is(7));

        for (int i = 1; i < buttonAmount; i++) {
            View childView = categoryLayout.getChildAt(i);
            assertThat(childView, instanceOf(Button.class));
        }
    }

    @Test
    public void shouldToggleSelectedItemsWhenToggleEventIsTriggered() throws Exception {
        CommodityToggledEventDetails eventDetails = fireCommodityToggledEvent(getDispenseActivity());

        assertThat(eventDetails.dispenseActivity.selectedCommodities, contains(eventDetails.commodityViewModel()));

        refire(eventDetails.commodityToggledEvent);

        assertThat(eventDetails.dispenseActivity.selectedCommodities, not(contains(eventDetails.commodityViewModel())));
    }

    @Test
    public void listViewShouldToggleCommodityWhenToggleEventIsTriggered() throws Exception {
        CommodityToggledEventDetails eventDetails = fireCommodityToggledEvent(getDispenseActivity());

        CommodityViewModel commodityInList = (CommodityViewModel) eventDetails.dispenseActivity.listViewSelectedCommodities.getAdapter().getItem(0);

        assertThat(commodityInList, is(eventDetails.commodityViewModel()));

        refire(eventDetails.commodityToggledEvent);

        assertThat(eventDetails.dispenseActivity.listViewSelectedCommodities.getAdapter().getCount(), is(0));
    }

    @Test
    public void shouldRemoveSelectedCommodityFromListWhenCancelButtonIsClicked() {
        CommodityToggledEventDetails eventDetails = fireCommodityToggledEvent(getDispenseActivity());

        SelectedCommoditiesAdapter adapter = eventDetails.dispenseActivity.selectedCommoditiesAdapter;

        ImageButton cancelButton = (ImageButton) getViewFromListRow(adapter, R.layout.selected_commodity_list_item, R.id.imageButtonCancel);

        cancelButton.performClick();

        assertFalse(eventDetails.dispenseActivity.selectedCommodities.contains(eventDetails.commodityViewModel()));
        assertThat(eventDetails.dispenseActivity.listViewSelectedCommodities.getAdapter().getCount(), is(0));
    }

    @Test
    public void testThatSubmitButtonExists() throws Exception {
        DispenseActivity activity = getDispenseActivity();
        assertThat(activity.buttonSubmitDispense, is(notNullValue()));
    }

    @Test
    public void testThatDispenseToFacilityExists() throws Exception {
        DispenseActivity activity = getDispenseActivity();
        assertThat(activity.checkboxCommoditySelected, is(notNullValue()));
    }

    private void refire(CommodityToggledEvent commodityToggledEvent) {
        EventBus.getDefault().post(commodityToggledEvent);
    }

    private CommodityToggledEventDetails fireCommodityToggledEvent(DispenseActivity dispenseActivity) {
        CommodityViewModel commodityViewModel = new CommodityViewModel(new Commodity("name"));
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodityViewModel);

        refire(commodityToggledEvent);

        return new CommodityToggledEventDetails(dispenseActivity, commodityToggledEvent);
    }

    private class CommodityToggledEventDetails {
        public DispenseActivity dispenseActivity;
        public CommodityToggledEvent commodityToggledEvent;

        public CommodityToggledEventDetails(DispenseActivity dispenseActivity, CommodityToggledEvent commodityToggledEvent) {
            this.dispenseActivity = dispenseActivity;
            this.commodityToggledEvent = commodityToggledEvent;
        }

        public CommodityViewModel commodityViewModel() {
            return this.commodityToggledEvent.getCommodity();
        }
    }

    @Test
    public void shouldToggleSubmitButtonVisibility() throws Exception {
        DispenseActivity dispenseActivity = getDispenseActivity();

        assertThat(dispenseActivity.buttonSubmitDispense.getVisibility(), not(is(VISIBLE)));

        CommodityViewModel commodityViewModel = new CommodityViewModel(new Commodity("name"));
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodityViewModel);
        EventBus.getDefault().post(commodityToggledEvent);

        assertThat(dispenseActivity.buttonSubmitDispense.getVisibility(), is(VISIBLE));
    }


    @Test
    public void getDispensingShouldGetItemsInTheListView() throws Exception {
        String commodityName = "food";

        DispenseActivity dispenseActivity = getDispenseActivity();

        dispenseActivity.checkboxCommoditySelected.setChecked(true);

        ListView mockListView = mock(ListView.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);

        SelectedCommoditiesAdapter mockSelectedCommoditiesAdapter = mock(SelectedCommoditiesAdapter.class);

        mockEditText.setText("12");

        Commodity commodity = new Commodity(commodityName);
        when(mockSelectedCommoditiesAdapter.getItem(anyInt())).thenReturn(new CommodityViewModel(commodity));
        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);
        when(mockListView.getAdapter()).thenReturn(mockSelectedCommoditiesAdapter);

        dispenseActivity.listViewSelectedCommodities = mockListView;


        Dispensing dispensing = dispenseActivity.getDispensing();

        assertThat(dispensing.getDispensingItems().size(), is(1));
        assertThat(dispensing.isDispenseToFacility(), is(true));

        assertThat(dispensing.getDispensingItems().get(0).getQuantity(), is(12));
        assertThat(dispensing.getDispensingItems().get(0).getCommodity().getName(), is(commodityName));
    }

    @Test
    public void testThatIfAllDispensingItemsHaveQuantitiesNoToastIsMade() throws Exception {

        DispenseActivity dispenseActivity = getDispenseActivity();

        ListView mockListView = mock(ListView.class);
        SelectedCommoditiesAdapter mockCommoditiesAdapter = mock(SelectedCommoditiesAdapter.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);
        mockEditText.setText("12");

        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);
        when(mockCommoditiesAdapter.getItem(anyInt())).thenReturn(new CommodityViewModel(new Commodity("food")));
        when(mockListView.getAdapter()).thenReturn(mockCommoditiesAdapter);

        dispenseActivity.listViewSelectedCommodities = mockListView;
        dispenseActivity.findViewById(R.id.buttonSubmitDispense).callOnClick();
        ShadowHandler.idleMainLooper();

        assertThat(ShadowToast.getTextOfLatestToast(), is(Matchers.nullValue()));


    }

    @Test
    public void testThatIfAnyOfTheDispensingItemsHaveErrorsAToastIsMade() throws Exception {
        DispenseActivity dispenseActivity = getDispenseActivity();

        ListView mockListView = mock(ListView.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);

        mockEditText.setText("12");
        mockEditText.setError("error");

        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);

        dispenseActivity.listViewSelectedCommodities = mockListView;
        dispenseActivity.findViewById(R.id.buttonSubmitDispense).callOnClick();
        ShadowHandler.idleMainLooper();

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(application.getString(R.string.dispense_submit_validation_message_errors)));
    }

    @Test
    public void testThatIfAllDispensingItemsHaveNoQuantitiesAToastIsMade() throws Exception {
        DispenseActivity dispenseActivity = getDispenseActivity();

        ListView mockListView = mock(ListView.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);


        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);

        dispenseActivity.listViewSelectedCommodities = mockListView;
        dispenseActivity.findViewById(R.id.buttonSubmitDispense).callOnClick();
        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(application.getString(R.string.dispense_submit_validation_message_filled)));
    }

    @Test
    public void testThatIfAllDispensingItemsHaveZeroQuantitiesAToastIsMade() throws Exception {
        DispenseActivity dispenseActivity = getDispenseActivity();

        ListView mockListView = mock(ListView.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);

        mockEditText.setText("0");

        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);

        dispenseActivity.listViewSelectedCommodities = mockListView;
        dispenseActivity.findViewById(R.id.buttonSubmitDispense).callOnClick();
        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(application.getString(R.string.dispense_submit_validation_message_zero)));
    }

    @Test
    public void testThatIfDispenseToAnotherFacilityCheckBox() throws Exception {

        DispenseActivity dispenseActivity = getDispenseActivity();

        ((CheckBox) dispenseActivity.findViewById(R.id.checkboxDispenseToFacility)).setChecked(true);

        Dispensing dispensing = dispenseActivity.getDispensing();

        assertTrue(dispensing.isDispenseToFacility());

        ((CheckBox) dispenseActivity.findViewById(R.id.checkboxDispenseToFacility)).setChecked(false);

        dispensing = dispenseActivity.getDispensing();

        assertFalse(dispensing.isDispenseToFacility());


    }
}
