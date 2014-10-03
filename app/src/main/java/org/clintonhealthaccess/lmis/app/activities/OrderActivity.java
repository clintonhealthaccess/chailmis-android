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

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.OrderTypeAdapter;
import org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.events.OrderTypeChanged;
import org.clintonhealthaccess.lmis.app.fragments.OrderConfirmationFragment;
import org.clintonhealthaccess.lmis.app.listeners.AlertClickListener;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.services.AlertsService;
import org.clintonhealthaccess.lmis.app.services.OrderService;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

import static com.google.common.collect.Lists.newArrayList;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.ALLOW_CLICK_WHEN_OUT_OF_STOCK;

public class OrderActivity extends CommoditySelectableActivity {

    @Inject
    OrderService orderService;

    @Inject
    AlertsService alertsService;

    @InjectView(R.id.buttonSubmitOrder)
    Button buttonSubmitOrder;

    @InjectView(R.id.textViewSRVNo)
    TextView textViewSRVNo;

    private String nextSRVNumber;

    @InjectView(R.id.spinnerOrderType)
    Spinner spinnerOrderType;

    private String prepopulatedOrderType;
    private Keyboard keyBoard;
    private KeyboardView keyBoardView;
    public final static int CodeDelete = -5;
    public final static int CodeCancel = -3;

    private int getSelectedCommoditiesAdapterId() {
        return R.layout.selected_order_commodity_list_item;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    @Override
    protected Button getSubmitButton() {
        return buttonSubmitOrder;
    }

    @Override
    protected String getActivityName() {
        return "Ordering";
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        return new SelectedOrderCommoditiesAdapter(
                this, getSelectedCommoditiesAdapterId(), new ArrayList<OrderCommodityViewModel>(), orderService.allOrderReasons(), getOrderType(), this);
    }

    private OrderType getOrderType() {
        return (OrderType) spinnerOrderType.getSelectedItem();
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return ALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

    @Override
    protected void beforeArrayAdapterCreate(Bundle savedInstanceState) {
        keyBoard = new Keyboard(OrderActivity.this, R.xml.keyboard);
        keyBoardView = (KeyboardView) findViewById(R.id.keyBoardView);
        keyBoardView.setKeyboard(keyBoard);
        keyBoardView.setPreviewEnabled(false);
        keyBoardView.setOnKeyboardActionListener(new MyOnKeyboardActionListener());

//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        Intent intent = getIntent();
        prepopulatedOrderType = intent.getStringExtra(AlertClickListener.ORDER_TYPE);
        setupOrderTypes();
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        nextSRVNumber = orderService.getNextSRVNumber();
        textViewSRVNo.setText(nextSRVNumber);
        buttonSubmitOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOrderValid()) {
                    FragmentManager fm = getSupportFragmentManager();
                    OrderConfirmationFragment dialog = OrderConfirmationFragment.newInstance(generateOrder());
                    dialog.show(fm, "confirmOrder");
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.fillInAllOrderItemValues), Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (prepopulatedOrderType != null) {
            if (prepopulatedOrderType.equalsIgnoreCase(OrderType.EMERGENCY)) {
                for (OrderCommodityViewModel orderCommodityViewModel : alertsService.getOrderCommodityViewModelsForLowStockAlert()) {
                    onEvent(new CommodityToggledEvent(orderCommodityViewModel));
                }
            } else if (prepopulatedOrderType.equalsIgnoreCase(OrderType.ROUTINE)) {
                for (OrderCommodityViewModel orderCommodityViewModel : alertsService.getOrderViewModelsForRoutineOrderAlert()) {
                    onEvent(new CommodityToggledEvent(orderCommodityViewModel));
                }
            }


        }


    }

    public void setupEditTextForNumberInput(View edittext) {
        edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) showCustomKeyboard(v);
                else hideCustomKeyboard();
            }
        });
        edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });
        edittext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                return true;
            }
        });
    }

    public void hideCustomKeyboard() {
        keyBoardView.setVisibility(View.GONE);
        keyBoardView.setEnabled(false);
    }

    public void showCustomKeyboard(View v) {
        keyBoardView.setVisibility(View.VISIBLE);
        keyBoardView.setEnabled(true);
        if (v != null)
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public boolean isCustomKeyboardVisible() {
        return keyBoardView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onBackPressed() {
        if (isCustomKeyboardVisible()) hideCustomKeyboard();
        else this.finish();
    }

    private void setupOrderTypes() {
        List<OrderType> orderTypes = orderService.allOrderTypes();
        final ArrayAdapter<OrderType> adapter = new OrderTypeAdapter(this, R.layout.spinner_item, orderTypes);
        spinnerOrderType.setAdapter(adapter);

        OrderType routine = new OrderType(OrderType.ROUTINE);
        setOrderType(orderTypes, routine);

        if (prepopulatedOrderType != null) {
            OrderType preset = new OrderType(prepopulatedOrderType);
            setOrderType(orderTypes, preset);
            if (preset.isRoutine() && alertsService.getNumberOfRoutineOrderAlerts() > 1) {
                Toast.makeText(getApplicationContext(), getString(R.string.outstanding_routine_order_alerts_message), Toast.LENGTH_LONG).show();
            }
        }
        spinnerOrderType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new OrderTypeChanged(adapter.getItem(position)));
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setOrderType(List<OrderType> orderTypes, OrderType orderType) {
        if (orderTypes.contains(orderType)) {
            Log.i("Here", "Order type " + orderType.getName() + " orderType" + orderType.getId());
            spinnerOrderType.setSelection(orderTypes.indexOf(orderType));
        }
    }


    @Override
    protected CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<BaseCommodityViewModel> viewModels = newArrayList();
                for (Commodity commodity : commodities) {
                    OrderCommodityViewModel orderCommodityViewModel = setupOrderCommodityViewModel(commodity);

                    viewModels.add(orderCommodityViewModel);
                }
                return viewModels;
            }
        };
    }

    public static OrderCommodityViewModel setupOrderCommodityViewModel(Commodity commodity) {
        OrderCommodityViewModel orderCommodityViewModel = new OrderCommodityViewModel(commodity);
        orderCommodityViewModel.setOrderPeriodStartDate(orderCommodityViewModel.getExpectedStartDate());
        orderCommodityViewModel.setOrderPeriodEndDate(orderCommodityViewModel.getExpectedEndDate());
        return orderCommodityViewModel;
    }

    private boolean isOrderValid() {
        int numberOfItems = arrayAdapter.getCount();
        for (int i = 0; i < numberOfItems; i++) {
            OrderCommodityViewModel commodityViewModel = (OrderCommodityViewModel) arrayAdapter.getItem(i);
            if (!commodityViewModel.isValidAsOrderItem()) {
                return false;
            }
        }
        return true;
    }

    protected Order generateOrder() {
        int numberOfItems = arrayAdapter.getCount();
        Order order = new Order();
        order.setOrderType(getOrderType());
        System.out.println("Here order tpe is " + order.getOrderType().getName());
        order.setSrvNumber(nextSRVNumber);
        for (int i = 0; i < numberOfItems; i++) {
            OrderCommodityViewModel commodityViewModel = (OrderCommodityViewModel) arrayAdapter.getItem(i);
            OrderItem orderItem = new OrderItem(commodityViewModel);
            orderItem.setOrder(order);
            orderItem.setReasonForUnexpectedQuantity(commodityViewModel.getReasonForUnexpectedOrderQuantity());
            order.addItem(orderItem);
        }
        return order;
    }

    private class MyOnKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {
        @Override
        public void onPress(int primaryCode) {

        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {


            View focusCurrent = OrderActivity.this.getWindow().getCurrentFocus();
            if (focusCurrent == null || focusCurrent.getClass() != EditText.class) return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            // Handle key
            if (primaryCode == CodeCancel) {
                hideCustomKeyboard();
            } else if (primaryCode == CodeDelete) {
                if (editable != null && start > 0) editable.delete(start - 1, start);
            } else {// Insert character
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    }
}
