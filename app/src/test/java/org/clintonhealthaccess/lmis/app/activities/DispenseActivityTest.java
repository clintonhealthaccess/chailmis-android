package org.clintonhealthaccess.lmis.app.activities;

import android.view.View;
import android.widget.Button;
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
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowHandler;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestFixture.initialiseDefaultCommodities;
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
import static org.robolectric.shadows.ShadowToast.getTextOfLatestToast;

@RunWith(RobolectricGradleTestRunner.class)
public class DispenseActivityTest {
    @Inject
    private CategoryService categoryService;

    private DispenseActivity getActivity() {
        return buildActivity(DispenseActivity.class).create().get();
    }

    @Before
    public void setUp() throws Exception {
        setUpInjection(this);
        initialiseDefaultCommodities(application);
        categoryService.clearCache();
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
        assertThat(buttonAmount, is(7));

        for (int i = 1; i < buttonAmount; i++) {
            View childView = categoryLayout.getChildAt(i);
            assertThat(childView, instanceOf(Button.class));
        }
    }

    @Test
    public void shouldToggleSelectedItemsWhenToggleEventIsTriggered() throws Exception {
        CommodityToggledEventDetails eventDetails = fireCommodityToggledEvent();

        assertThat(eventDetails.dispenseActivity.selectedCommodities, contains(eventDetails.commodityViewModel()));

        refire(eventDetails.commodityToggledEvent);

        assertThat(eventDetails.dispenseActivity.selectedCommodities, not(contains(eventDetails.commodityViewModel())));
    }

    private void refire(CommodityToggledEvent commodityToggledEvent) {
        EventBus.getDefault().post(commodityToggledEvent);
    }

    private class CommodityToggledEventDetails {
        protected DispenseActivity dispenseActivity;
        protected CommodityToggledEvent commodityToggledEvent;

        public CommodityToggledEventDetails(DispenseActivity dispenseActivity, CommodityToggledEvent commodityToggledEvent) {
            this.dispenseActivity = dispenseActivity;
            this.commodityToggledEvent = commodityToggledEvent;
        }

        public CommodityViewModel commodityViewModel() {
            return  this.commodityToggledEvent.getCommodity();
        }
    }

    @Test
    public void listViewShouldToggleCommodityWhenToggleEventIsTriggered() throws Exception {
        CommodityToggledEventDetails eventDetails = fireCommodityToggledEvent();

        CommodityViewModel commodityInList = (CommodityViewModel)eventDetails.dispenseActivity.listViewSelectedCommodities.getAdapter().getItem(0);

        assertThat(commodityInList, is(eventDetails.commodityViewModel()));

        refire(eventDetails.commodityToggledEvent);

        assertThat(eventDetails.dispenseActivity.listViewSelectedCommodities.getAdapter().getCount(), is(0));
    }

    @Test
    public void shouldRemoveSelectedCommodityFromListWhenCancelButtonIsClicked() {
        CommodityToggledEventDetails eventDetails = fireCommodityToggledEvent();

        SelectedCommoditiesAdapter adapter = eventDetails.dispenseActivity.selectedCommoditiesAdapter;

        ImageButton cancelButton = (ImageButton) getViewFromListRow(adapter, R.layout.selected_commodity_list_item, R.id.imageButtonCancel);

        cancelButton.performClick();

        assertFalse(eventDetails.dispenseActivity.selectedCommodities.contains(eventDetails.commodityViewModel()));
        assertThat(eventDetails.dispenseActivity.listViewSelectedCommodities.getAdapter().getCount(), is(0));
    }

    @Test
    public void testThatSubmitButtonExists() throws Exception {
        DispenseActivity activity = getActivity();
        assertThat(activity.buttonSubmitDispense, is(notNullValue()));
    }

    @Test
    public void testThatDispenseToFacilityExists() throws Exception {
        DispenseActivity activity = getActivity();
        assertThat(activity.checkboxCommoditySelected, is(notNullValue()));
    }

    private CommodityToggledEventDetails fireCommodityToggledEvent() {
        DispenseActivity dispenseActivity = getActivity();
        CommodityViewModel commodityViewModel = new CommodityViewModel(new Commodity("name"));
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodityViewModel);

        refire(commodityToggledEvent);

        return new CommodityToggledEventDetails(dispenseActivity, commodityToggledEvent);
    }

    @Test
    public void shouldToggleSubmitButtonVisibility() throws Exception {
        DispenseActivity dispenseActivity = getActivity();

        assertFalse(dispenseActivity.buttonSubmitDispense.getVisibility() == View.VISIBLE);

        CommodityViewModel commodityViewModel = new CommodityViewModel(new Commodity("name"));
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodityViewModel);
        EventBus.getDefault().post(commodityToggledEvent);

        assertTrue(dispenseActivity.buttonSubmitDispense.getVisibility() == View.VISIBLE);
    }


    @Test
    public void testSubmitButtonLogicWhenDispensingIsInValid() throws Exception {
        DispenseActivity dispenseActivity = getActivity();

        ListView mockListView = mock(ListView.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);


        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);

        dispenseActivity.listViewSelectedCommodities = mockListView;

        assertFalse(dispenseActivity.dispensingIsValid());

        dispenseActivity.buttonSubmitDispense.callOnClick();

        ShadowHandler.idleMainLooper();
        assertThat(getTextOfLatestToast(), equalTo("Make sure all fields are filled"));

    }

    @Test
    public void getDispensingShouldGetItemsInTheListView() throws Exception {
        String commodityName = "food";

        DispenseActivity dispenseActivity = getActivity();

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
    public void testThatDispensingIsInvalidIfNoQuantitiesAreSet() throws Exception {
        DispenseActivity dispenseActivity = getActivity();

        ListView mockListView = mock(ListView.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);


        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);

        dispenseActivity.listViewSelectedCommodities = mockListView;

        assertFalse(dispenseActivity.dispensingIsValid());
    }

    @Test
    public void testThatDispensingIsInvalidIfAnyFieldHasAnError() throws Exception {
        DispenseActivity dispenseActivity = getActivity();

        ListView mockListView = mock(ListView.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);

        mockEditText.setText("12");
        mockEditText.setError("error");

        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);

        dispenseActivity.listViewSelectedCommodities = mockListView;

        assertFalse(dispenseActivity.dispensingIsValid());
    }

    @Test
    public void testThatDispensingIsValidIfQuantitiesAreSet() throws Exception {
        DispenseActivity dispenseActivity = getActivity();

        ListView mockListView = mock(ListView.class);
        View mockListItemView = mock(View.class);
        EditText mockEditText = new EditText(application);

        mockEditText.setText("12");

        when(mockListItemView.findViewById(R.id.editTextQuantity)).thenReturn(mockEditText);
        when(mockListView.getChildAt(anyInt())).thenReturn(mockListItemView);
        when(mockListView.getChildCount()).thenReturn(1);

        dispenseActivity.listViewSelectedCommodities = mockListView;

        assertTrue(dispenseActivity.dispensingIsValid());
    }
}
