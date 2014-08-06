package org.clintonhealthaccess.lmis.app.activities;

import android.widget.ImageButton;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.ReceiveCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.ReceiveService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.Arrays;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertFalse;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;


@RunWith(RobolectricGradleTestRunner.class)
public class ReceiveActivityTest {


    private UserService userService;
    private ReceiveService mockReceiveService;

    private ReceiveActivity getReceiveActivity() {
        return buildActivity(ReceiveActivity.class).create().get();
    }

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        mockReceiveService = mock(ReceiveService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
                bind(ReceiveService.class).toInstance(mockReceiveService);
            }
        });

    }

    @Test
    public void testBuildActivity() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity, not(nullValue()));
    }

    @Ignore("W.I.P")
    @Test
    public void shouldRemoveSelectedCommodityFromListWhenCancelButtonIsClicked() {
        CommodityToggledEventDetails eventDetails = fireCommodityToggledEvent(getReceiveActivity());
        ReceiveCommoditiesAdapter adapter = (ReceiveCommoditiesAdapter) eventDetails.activity.arrayAdapter;

        ImageButton cancelButton = (ImageButton) getViewFromListRow(adapter, R.layout.receive_commodity_list_item, R.id.imageButtonCancel);
        cancelButton.performClick();

        assertFalse(eventDetails.activity.selectedCommodities.contains(eventDetails.commodityViewModel()));
        assertThat(eventDetails.activity.gridViewSelectedCommodities.getAdapter().getCount(), is(0));
    }


    @Test
    public void shouldShowAllocationIdAutoCompleteTextView() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity.textViewAllocationId, not(nullValue()));
    }

    @Test
    public void availableAllocationIdsShouldBeSelectableFromTheTextViewForAllocationId() throws Exception {
        String item1 = "UG-0001";
        String item2 = "UG-0002";
        when(mockReceiveService.getReadyAllocationIds()).thenReturn(new ArrayList<String>(Arrays.asList(item1, item2)));
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity.textViewAllocationId.getAdapter().getCount(), is(2));
        assertThat(receiveActivity.textViewAllocationId.getAdapter().getItem(0).toString(), is(item1));
    }

    @Test
    public void shouldShowAnErrorMessageForAllocationIdThatHasAlreadyBeenReceived() throws Exception {
        String item1 = "UG-0001";
        String item2 = "UG-0002";
        when(mockReceiveService.getCompletedIds()).thenReturn(new ArrayList<String>(Arrays.asList(item1, item2)));
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity.textViewAllocationId.getError(), is(nullValue()));
        receiveActivity.textViewAllocationId.setText(item1);
        assertThat(receiveActivity.textViewAllocationId.getError().toString(), is(application.getString(R.string.error_allocation_received)));
        receiveActivity.textViewAllocationId.setText("UG-12032");
        assertThat(receiveActivity.textViewAllocationId.getError(), is(nullValue()));

    }

    @Test
    public void shouldNotLetUserSubmitFormWhenAllocationIdHasError() throws Exception {

        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setText("aoiiouads");
        receiveActivity.buttonSubmitReceive.performClick();
        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(application.getString(R.string.receive_submit_validation_message_allocation_id)));
    }

    @Test
    public void shouldLetUserSubmitFormWhenAllocationIdHasNoError() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setText("UG-200");
        receiveActivity.buttonSubmitReceive.performClick();
        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), is(nullValue()));
    }

    @Test
    public void shouldShowAnErrorWhenTheAllocationIdIsOfWrongFormat() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setText("aoiiouads");
        assertThat(receiveActivity.textViewAllocationId.getError().toString(), is(application.getString(R.string.error_allocation_id_wrong_format)));
        receiveActivity.textViewAllocationId.setText("UG-12032");
        assertThat(receiveActivity.textViewAllocationId.getError(), is(nullValue()));
    }

    @Ignore("WIP")
    @Test
    public void shouldPresetTheQuantityForSelectedItemIfAllocationIdIsSet() throws Exception {
    }

    private CommodityToggledEventDetails fireCommodityToggledEvent(CommoditySelectableActivity activity) {
        BaseCommodityViewModel commodityViewModel = new BaseCommodityViewModel(new Commodity("name"));
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodityViewModel);
        EventBus.getDefault().post(commodityToggledEvent);
        return new CommodityToggledEventDetails(activity, commodityToggledEvent);
    }


    private class CommodityToggledEventDetails {
        private CommoditySelectableActivity activity;
        public CommodityToggledEvent commodityToggledEvent;

        public CommodityToggledEventDetails(CommoditySelectableActivity activity, CommodityToggledEvent commodityToggledEvent) {
            this.activity = activity;
            this.commodityToggledEvent = commodityToggledEvent;
        }

        public BaseCommodityViewModel commodityViewModel() {
            return this.commodityToggledEvent.getCommodity();
        }


    }
}
