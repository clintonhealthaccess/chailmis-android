package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.fragments.OrderConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.services.OrderService;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.ALLOW_CLICK_WHEN_OUT_OF_STOCK;

public class OrderActivity extends CommoditySelectableActivity {

    @Inject
    OrderService orderService;

    @InjectView(R.id.buttonSubmitOrder)
    Button buttonSubmitOrder;

    @InjectView(R.id.textViewSRVNo)
    TextView textViewSRVNo;
    private String nextSRVNumber;


    // FIXME: id need change here
    private int getSelectedCommoditiesAdapterId() {
        return R.layout.selected_order_commodity_list_item;
    }

    @Override
    protected void onCommoditySelectionChanged(List<CommodityViewModel> selectedCommodities) {
        if (selectedCommodities.size() > 0) {
            buttonSubmitOrder.setVisibility(View.VISIBLE);
        } else {
            buttonSubmitOrder.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        return new SelectedOrderCommoditiesAdapter(
                this, getSelectedCommoditiesAdapterId(), new ArrayList<CommodityViewModel>(), orderService.allOrderReasons());
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return ALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        nextSRVNumber = orderService.getNextSRVNumber();
        textViewSRVNo.setText(nextSRVNumber);
        buttonSubmitOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                OrderConfirmationFragment dialog = OrderConfirmationFragment.newInstance(generateOrder());
                dialog.show(fm, "confirmOrder");
            }
        });
    }

    protected Order generateOrder() {
        int numberOfItems = arrayAdapter.getCount();
        Order order = new Order();

        for (int i = 0; i < numberOfItems; i++) {
            CommodityViewModel commodityViewModel = (CommodityViewModel) arrayAdapter.getItem(i);
            OrderItem orderItem = new OrderItem(commodityViewModel);
            orderItem.setOrder(order);
            order.addItem(orderItem);
        }
        order.setSrvNumber(nextSRVNumber);
        return order;
    }
}
