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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.ConfirmReceiveAdapter;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.services.ReceiveService;

import roboguice.fragment.RoboDialogFragment;

public class ReceiveConfirmFragment extends RoboDialogFragment {

    public static final String RECEIVE = "RECEIVE";
    private static final String CONTEXT = "CONTEXT";
    private Receive receive;


    @Inject
    private ReceiveService receiveService;

    public static ReceiveConfirmFragment newInstance(Receive receive) {
        ReceiveConfirmFragment receiveConfirmFragment = new ReceiveConfirmFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(RECEIVE, receive);
        receiveConfirmFragment.setArguments(bundle);
        return receiveConfirmFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receive = (Receive) getArguments().get(RECEIVE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive_confirmation, container, false);

        Button confirmButton = (Button) view.findViewById(R.id.button_receive_confirm);
        Button backButton = (Button) view.findViewById(R.id.button_receive_go_back);
        ListView listViewReceiveItems = (ListView) view.findViewById(R.id.listViewConfirmReceive);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ConfirmReceiveAdapter confirmReceiveAdapter = new ConfirmReceiveAdapter(getActivity().getApplicationContext(), R.layout.receive_confirm_list_item, receive.getReceiveItems());
        listViewReceiveItems.addHeaderView(inflater.inflate(R.layout.confirm_receive_header, container));
        listViewReceiveItems.setAdapter(confirmReceiveAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SaveReceiveTask(ReceiveConfirmFragment.this).execute();
            }
        });

        return view;
    }

    private class SaveReceiveTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog dialog;
        private ReceiveConfirmFragment fragment;

        public SaveReceiveTask(ReceiveConfirmFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(getActivity());
            this.dialog.setMessage(getString(R.string.receive_saving));
            this.dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                receiveService.saveReceive(receive);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Toast.makeText(fragment.getActivity().getApplicationContext(), getString(R.string.receive_successful), Toast.LENGTH_LONG).show();

                fragment.dismiss();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                    startActivity(activity.getIntent());
                }
            } else {
                Toast.makeText(fragment.getActivity().getApplicationContext(), getString(R.string.receive_failed), Toast.LENGTH_LONG).show();
            }

            this.dialog.dismiss();

        }
    }
}
