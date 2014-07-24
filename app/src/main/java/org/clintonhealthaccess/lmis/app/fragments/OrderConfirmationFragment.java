package org.clintonhealthaccess.lmis.app.fragments;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.ConfirmOrderAdapter;
import org.clintonhealthaccess.lmis.app.models.Order;
import org.clintonhealthaccess.lmis.app.services.OrderService;

import roboguice.fragment.RoboDialogFragment;

public class OrderConfirmationFragment extends RoboDialogFragment {
    private static final String ORDER = "order";
    @Inject
    OrderService orderService;

    private Order order;


    public static OrderConfirmationFragment newInstance(Order order) {
        OrderConfirmationFragment fragment = new OrderConfirmationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderConfirmationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            order = (Order) getArguments().getSerializable(ORDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_confirmation, container, false);
        Button buttonOrderConfirm = (Button) view.findViewById(R.id.buttonOrderConfirm);
        Button buttonOrderGoBack = (Button) view.findViewById(R.id.buttonOrderGoBack);
        ListView listViewOrderItems = (ListView) view.findViewById(R.id.listViewConfirmOrderItems);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        buttonOrderConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderService.saveOrder(order);
                showToastMessage(getString(R.string.order_successful));
                dismiss();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                    startActivity(activity.getIntent());
                }
            }
        });

        buttonOrderGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        listViewOrderItems.addHeaderView(inflater.inflate(R.layout.confirm_header_order, container));
        ConfirmOrderAdapter adapter = new ConfirmOrderAdapter(getActivity(), R.layout.confirm_order_list_item, order.getItems());
        listViewOrderItems.setAdapter(adapter);
        return view;
    }

    private void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

}
