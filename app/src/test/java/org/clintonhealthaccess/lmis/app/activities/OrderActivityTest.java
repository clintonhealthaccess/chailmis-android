/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.activities;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.OrderService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class OrderActivityTest {

    public static final String TEST_SRV_NUMBER = "AU-0009";
    private OrderActivity orderActivity;
    private OrderService orderServiceMock;
    private UserService userService;

    private OrderActivity getOrderActivity() {
        return setupActivity(OrderActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        orderServiceMock = mock(OrderService.class);
        List<OrderReason> emergencyReason = Arrays.asList(new OrderReason("Losses"));
        List<OrderType> types = Arrays.asList(new OrderType(OrderType.ROUTINE), new OrderType(OrderType.EMERGENCY));
        when(orderServiceMock.allOrderReasons()).thenReturn(emergencyReason);
        when(orderServiceMock.getNextSRVNumber()).thenReturn(TEST_SRV_NUMBER);
        when(orderServiceMock.allOrderTypes()).thenReturn(types);
        userService = mock(UserService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(OrderService.class).toInstance(orderServiceMock);
                bind(UserService.class).toInstance(userService);
            }
        });

        orderActivity = getOrderActivity();
    }

    @Test
    public void testBuildActivity() throws Exception {
        assertThat(orderActivity, not(nullValue()));
    }

    @Test
    public void shouldPassOrderTypesFromOrderServiceToAdapter() {
        assertThat(((OrderType) orderActivity.spinnerOrderType.getSelectedItem()).getName(), is(OrderType.ROUTINE));
    }

    @Test
    public void shouldToggleVisibilityOfSubmitButton() {
        List<BaseCommodityViewModel> selectedCommodities = newArrayList();
        selectedCommodities.add(new OrderCommodityViewModel(new Commodity("id", "name")));
        orderActivity.onCommoditySelectionChanged(selectedCommodities);
        assertThat(orderActivity.buttonSubmitOrder.getVisibility(), is(View.VISIBLE));

        selectedCommodities = new ArrayList<>();
        orderActivity.onCommoditySelectionChanged(selectedCommodities);
        assertThat(orderActivity.buttonSubmitOrder.getVisibility(), is(View.INVISIBLE));
    }

    @Test
    public void shouldCreateOrderFromSelectedCommodities() {
        OrderCommodityViewModel commodityViewModel1 = new OrderCommodityViewModel(new Commodity("id", "Commodity 1"), 10);
        OrderCommodityViewModel commodityViewModel2 = new OrderCommodityViewModel(new Commodity("id", "Commodity 2"), 10);

        List<OrderReason> orderReasons = Arrays.asList();
        List<OrderCommodityViewModel> commodityViewModels = Arrays.asList(commodityViewModel1, commodityViewModel2);
        OrderType type = new OrderType(OrderType.ROUTINE);
        orderActivity.arrayAdapter = new SelectedOrderCommoditiesAdapter(Robolectric.application, R.layout.selected_order_commodity_list_item, commodityViewModels, orderReasons, type);

        OrderItem orderItem2 = new OrderItem(commodityViewModel1);
        OrderItem orderItem1 = new OrderItem(commodityViewModel2);

        Order expectedOrder = new Order();
        expectedOrder.addItem(orderItem1);
        expectedOrder.addItem(orderItem2);

        assertThat(expectedOrder, is(orderActivity.generateOrder()));
    }

    @Test
    public void shouldDisplaySRVNumber() throws Exception {
        assertThat(orderActivity.textViewSRVNo, is(notNullValue()));
        assertThat(orderActivity.textViewSRVNo.getText().toString(), is(TEST_SRV_NUMBER));
    }

    @Test
    public void shouldConfirmOrderAndOrderItemsOnSubmit() {
        OrderCommodityViewModel commodityViewModel1 = new OrderCommodityViewModel(new Commodity("id", "Commodity 1"), 10);
        commodityViewModel1.setOrderPeriodEndDate(new Date());
        commodityViewModel1.setOrderPeriodStartDate(new Date());

        EventBus.getDefault().post(new CommodityToggledEvent(commodityViewModel1));

        Button buttonSubmitOrder = orderActivity.buttonSubmitOrder;

        assertThat(buttonSubmitOrder.getVisibility(), is(View.VISIBLE));
        buttonSubmitOrder.performClick();
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertThat(dialog, is(notNullValue()));
    }

    @Test
    public void shouldShowInvalidFieldsToastGivenSubmitWithEmptyFields() {
        OrderCommodityViewModel commodityViewModel1 = new OrderCommodityViewModel(new Commodity("id", "Commodity 1"), 10);
        EventBus.getDefault().post(new CommodityToggledEvent(commodityViewModel1));

        orderActivity.buttonSubmitOrder.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is(application.getResources().getString(R.string.fillInAllOrderItemValues)));
        assertThat(ShadowDialog.getLatestDialog(), is(nullValue()));
    }

    @Test
    public void shouldInitializeStartAndEndDateForOrderCommodityViewModels() throws Exception {

    }

    @Test
    public void shouldShowRoutineAsTheDefaultTypeForOrder() throws Exception {
        assertThat(((OrderType) orderActivity.spinnerOrderType.getSelectedItem()).getName(), is(OrderType.ROUTINE));
    }
}
