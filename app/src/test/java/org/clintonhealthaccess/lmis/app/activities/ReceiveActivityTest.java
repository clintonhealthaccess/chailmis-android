package org.clintonhealthaccess.lmis.app.activities;

import android.widget.ImageButton;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.ReceiveCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.SelectedCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertFalse;
import static org.clintonhealthaccess.lmis.utils.ListTestUtils.getViewFromListRow;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class ReceiveActivityTest {


    private UserService userService;

    private ReceiveActivity getReceiveActivity() {
        return buildActivity(ReceiveActivity.class).create().get();
    }

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
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
