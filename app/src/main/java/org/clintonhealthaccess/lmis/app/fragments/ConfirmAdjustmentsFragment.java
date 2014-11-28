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
import android.os.AsyncTask;
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
import org.clintonhealthaccess.lmis.app.adapters.ConfirmAdjustmentsAdapter;
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.services.AdjustmentService;

import java.util.ArrayList;

import roboguice.fragment.RoboDialogFragment;


public class ConfirmAdjustmentsFragment extends RoboDialogFragment {

    private static final String ADJUSTMENTS = "adjustments";
    private ArrayList<Adjustment> adjustments;

    @Inject
    AdjustmentService adjustmentService;


    public static ConfirmAdjustmentsFragment newInstance(ArrayList<Adjustment> adjustments) {
        ConfirmAdjustmentsFragment fragment = new ConfirmAdjustmentsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ADJUSTMENTS, adjustments);
        fragment.setArguments(args);
        return fragment;
    }

    public ConfirmAdjustmentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            adjustments = (ArrayList<Adjustment>) getArguments().getSerializable(ADJUSTMENTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_adjustments, container, false);
        Button buttonConfirmAdjustments = (Button) view.findViewById(R.id.buttonAdjustmentConfirm);
        Button buttonCancelAdjustments = (Button) view.findViewById(R.id.buttonAdjustmentGoBack);
        ListView listViewConfirmItems = (ListView) view.findViewById(R.id.listViewConfirmItems);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        listViewConfirmItems.addHeaderView(inflater.inflate(R.layout.confirm_header_adjustment, container));
        listViewConfirmItems.setAdapter(new ConfirmAdjustmentsAdapter(getActivity().getApplicationContext(), R.layout.confirm_adjustment_list_item, adjustments));

        buttonConfirmAdjustments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AdjustmentSaveTask().execute();
            }
        });

        buttonCancelAdjustments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }


    private class AdjustmentSaveTask extends AsyncTask<Void, Void, Boolean> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Saving...");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                adjustmentService.save(adjustments);
                return true;
            } catch (Exception e) {

                throw e;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.adjustments_successful), Toast.LENGTH_LONG).show();
                dismiss();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                    startActivity(activity.getIntent());
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Failed to save Adjustments", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        }
    }


}
