package org.clintonhealthaccess.lmis.app.activities;

import android.view.View;
import android.widget.Spinner;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.services.OrderService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

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
public class OrderActivityTest {

    private OrderActivity orderActivity;
    private OrderService orderServiceMock;

    private OrderActivity getOrderActivity() {
        return buildActivity(OrderActivity.class).create().get();
    }

    @Before
    public void setUp() throws Exception {
        orderServiceMock = mock(OrderService.class);
        List<OrderReason> emergencyReason = Arrays.asList(new OrderReason("Emergency", OrderReason.ORDER_REASONS_JSON_KEY));
        when(orderServiceMock.allOrderReasons()).thenReturn(emergencyReason);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(OrderService.class).toInstance(orderServiceMock);
            }
        });

        orderActivity = getOrderActivity();
    }

    @Test
    public void testBuildActivity() throws Exception {
        assertThat(orderActivity, not(nullValue()));
    }

    @Test
    public void shouldPassOrderReasonsFromOrderServiceToAdapter() {
        SelectedOrderCommoditiesAdapter adapter = (SelectedOrderCommoditiesAdapter) orderActivity.gridViewSelectedCommodities.getAdapter();

        CommodityViewModel commodityViewModel = new CommodityViewModel(new Commodity("name"));
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodityViewModel);

        EventBus.getDefault().post(commodityToggledEvent);

        Spinner orderReasonsSpinner = (Spinner) getViewFromListRow(adapter, R.layout.selected_order_commodity_list_item, R.id.spinnerOrderReasons);
        assertThat(orderReasonsSpinner.getAdapter().getCount(), is(1));
        assertThat(((OrderReason) orderReasonsSpinner.getItemAtPosition(0)).getReason(), is("Emergency"));
    }

    @Test
    public void shouldToggleVisibilityOfSubmitButton() {
        List<CommodityViewModel> selectedCommodities = Arrays.asList(new CommodityViewModel(new Commodity("id", "name")));
        orderActivity.onCommoditySelectionChanged(selectedCommodities);
        assertThat(orderActivity.buttonSubmitOrder.getVisibility(), is(View.VISIBLE));

        selectedCommodities = new ArrayList<>();
        orderActivity.onCommoditySelectionChanged(selectedCommodities);
        assertThat(orderActivity.buttonSubmitOrder.getVisibility(), is(View.INVISIBLE));
    }

    @Test
    public void shouldCreateOrderFromSelectedCommodities() {
        CommodityViewModel commodityViewModel1 = new CommodityViewModel(new Commodity("id", "Commodity 1"), 10);
        CommodityViewModel commodityViewModel2 = new CommodityViewModel(new Commodity("id", "Commodity 2"), 10);

        List<OrderReason> orderReasons = Arrays.asList();
        List<CommodityViewModel> commodityViewModels = Arrays.asList(commodityViewModel1, commodityViewModel2);

        orderActivity.arrayAdapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, R.layout.selected_order_commodity_list_item, commodityViewModels, orderReasons);

        OrderItem orderItem2 = new OrderItem(commodityViewModel1);
        OrderItem orderItem1 = new OrderItem(commodityViewModel2);

        Order expectedOrder = new Order();
        expectedOrder.addItem(orderItem1);
        expectedOrder.addItem(orderItem2);

        assertThat(expectedOrder, is(orderActivity.generateOrder()));
    }
}
