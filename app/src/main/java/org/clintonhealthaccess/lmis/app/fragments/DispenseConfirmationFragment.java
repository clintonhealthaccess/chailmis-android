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
import org.clintonhealthaccess.lmis.app.adapters.ConfirmDispenseAdapter;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.services.DispensingService;

import roboguice.fragment.RoboDialogFragment;


public class DispenseConfirmationFragment extends RoboDialogFragment {

    public static final String DISPENSING = "param_dispensings";
    protected Dispensing dispensing;

    @Inject
    DispensingService dispensingService;

    Button buttonDispenseConfirm;
    Button buttonDispenseGoBack;
    private ListView listViewConfirmItems;
    private ConfirmDispenseAdapter confirmDispenseAdapter;

    public static DispenseConfirmationFragment newInstance(Dispensing dispensingList) {
        DispenseConfirmationFragment fragment = new DispenseConfirmationFragment();
        Bundle args = new Bundle();
        args.putSerializable(DISPENSING, dispensingList);
        fragment.setArguments(args);
        return fragment;
    }

    public DispenseConfirmationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dispensing = (Dispensing) getArguments().getSerializable(DISPENSING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dispense_confirmation, container, false);
        buttonDispenseConfirm = (Button) view.findViewById(R.id.buttonDispenseConfirm);
        buttonDispenseGoBack = (Button) view.findViewById(R.id.buttonDispenseGoBack);
        listViewConfirmItems = (ListView) view.findViewById(R.id.listViewConfirmItems);
        setUpButtons();
        setupDialog();
        confirmDispenseAdapter = new ConfirmDispenseAdapter(getActivity(), R.layout.confirm_commodity_list_item, dispensing.getDispensingItems(), dispensing);

        listViewConfirmItems.addHeaderView(inflater.inflate(R.layout.confirm_header, container));
        buttonDispenseConfirm.setText(getString(R.string.confirm));

        listViewConfirmItems.setAdapter(confirmDispenseAdapter);
        return view;
    }

    private void setupDialog() {
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    private void setUpButtons() {
        buttonDispenseConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveDispensing task = new SaveDispensing(DispenseConfirmationFragment.this);
                task.execute();
            }
        });

        buttonDispenseGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private class SaveDispensing extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog dialog;
        private DispenseConfirmationFragment fragment;

        public SaveDispensing(DispenseConfirmationFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(getActivity());
            this.dialog.setMessage(getString(R.string.saving_dispense));
            this.dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                dispensingService.addDispensing(dispensing);
            } catch (Exception ex) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                showToastMessage(getString(R.string.dispense_to_successful));

                fragment.dismiss();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                    startActivity(activity.getIntent());
                }
            } else {
                Toast.makeText(fragment.getActivity().getApplicationContext(), getString(R.string.failed_to_save), Toast.LENGTH_LONG).show();
            }

            this.dialog.dismiss();

        }
    }

}
