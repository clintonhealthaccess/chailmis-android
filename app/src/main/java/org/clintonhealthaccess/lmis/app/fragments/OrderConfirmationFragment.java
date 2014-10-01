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

package org.clintonhealthaccess.lmis.app.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.ConfirmOrderAdapter;
import org.clintonhealthaccess.lmis.app.backgroundServices.AlertsGenerationIntentService;
import org.clintonhealthaccess.lmis.app.listeners.AlertClickListener;
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
        TextView textViewSRVNumber = (TextView) view.findViewById(R.id.textViewSRVNumber);
        ListView listViewOrderItems = (ListView) view.findViewById(R.id.listViewConfirmOrderItems);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setupConfirmButton(buttonOrderConfirm);
        textViewSRVNumber.setText(order.getSrvNumber());
        buttonOrderGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setupList(inflater, container, listViewOrderItems);
        return view;
    }

    private void setupList(LayoutInflater inflater, ViewGroup container, ListView listViewOrderItems) {
        listViewOrderItems.addHeaderView(inflater.inflate(R.layout.confirm_header_order, container));
        ConfirmOrderAdapter adapter = new ConfirmOrderAdapter(getActivity(), R.layout.confirm_order_list_item, order.getItems());
        listViewOrderItems.setAdapter(adapter);
    }

    private void setupConfirmButton(Button buttonOrderConfirm) {
        buttonOrderConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AsyncTask<Void, Void, Boolean> saveOrderTask = new AsyncTask<Void, Void, Boolean>() {
                    private ProgressDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        this.dialog = new ProgressDialog(getActivity());
                        this.dialog.setMessage(getString(R.string.order_saving));
                        this.dialog.show();
                    }

                    @Override
                    protected Boolean doInBackground(Void... params) {

                        try {
                            orderService.saveOrder(order);
                        } catch (Exception ex) {
//                            Log.e("order", ex.getMessage());
                            throw ex;
//                            return false;
                        }
                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        super.onPostExecute(success);
                        if (success) {
                            showToastMessage(getString(R.string.order_successful));
                            FragmentActivity activity = getActivity();
                            if (activity != null) {
                                activity.finish();
                                Intent intent = activity.getIntent();
                                intent.removeExtra(AlertClickListener.ORDER_TYPE);
                                startActivity(intent);
                            }
                        } else {
                            showToastMessage(getString(R.string.order_failed));
                        }
                        this.dialog.dismiss();
                        dismiss();
                    }
                };

                saveOrderTask.execute();

            }
        });
    }

    private void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

}
