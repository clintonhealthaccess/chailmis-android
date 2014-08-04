package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.SelectedOrderCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.fragments.OrderConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.models.OrderItem;
import org.clintonhealthaccess.lmis.app.services.OrderService;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static com.google.common.collect.Lists.newArrayList;
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
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    @Override
    protected Button getSubmitButton() {
        return buttonSubmitOrder;
    }

    @Override
    protected ArrayAdapter getArrayAdapter() {
        return new SelectedOrderCommoditiesAdapter(
                this, getSelectedCommoditiesAdapterId(), new ArrayList<OrderCommodityViewModel>(), orderService.allOrderReasons());
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
                if (isOrderValid()) {
                    FragmentManager fm = getSupportFragmentManager();
                    OrderConfirmationFragment dialog = OrderConfirmationFragment.newInstance(generateOrder());
                    dialog.show(fm, "confirmOrder");
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.fillInAllOrderItemValues), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    protected CommoditiesToViewModelsConverter getViewModelConverter() {
        return new CommoditiesToViewModelsConverter() {
            @Override
            public List<? extends BaseCommodityViewModel> execute(List<Commodity> commodities) {
                List<BaseCommodityViewModel> viewModels = newArrayList();
                for (Commodity commodity : commodities) {
                    viewModels.add(new OrderCommodityViewModel(commodity));
                }
                return viewModels;
            }
        };
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

        for (int i = 0; i < numberOfItems; i++) {
            OrderCommodityViewModel commodityViewModel = (OrderCommodityViewModel) arrayAdapter.getItem(i);
            OrderItem orderItem = new OrderItem(commodityViewModel);
            orderItem.setOrder(order);
            order.addItem(orderItem);
        }
        order.setSrvNumber(nextSRVNumber);
        return order;
    }
}
