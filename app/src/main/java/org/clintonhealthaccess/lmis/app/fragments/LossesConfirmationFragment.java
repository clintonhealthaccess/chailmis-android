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
import org.clintonhealthaccess.lmis.app.adapters.ConfirmLossesAdapter;
import org.clintonhealthaccess.lmis.app.models.Loss;
import org.clintonhealthaccess.lmis.app.services.LossService;

import roboguice.fragment.RoboDialogFragment;

public class LossesConfirmationFragment extends RoboDialogFragment {

    public static final String LOSSES = "Losses";
    @Inject
    private LossService lossService;
    private Loss loss;

    public LossesConfirmationFragment() {
    }

    public static LossesConfirmationFragment newInstance(Loss loss) {
        LossesConfirmationFragment fragment = new LossesConfirmationFragment();
        Bundle args = new Bundle();
        args.putSerializable(LOSSES, loss);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loss = (Loss) getArguments().getSerializable(LOSSES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_losses_confirmation, container, false);

        Button confirmButton = (Button) view.findViewById(R.id.button_losses_confirm);
        Button backButton = (Button) view.findViewById(R.id.button_losses_goBack);
        ListView listViewLossesItem = (ListView) view.findViewById(R.id.listViewConfirmLosses);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        ConfirmLossesAdapter confirmLossesAdapter = new ConfirmLossesAdapter(getActivity().getApplicationContext(), R.layout.losses_confirm_list_item, loss.getLossItems());
        listViewLossesItem.addHeaderView(inflater.inflate(R.layout.confirm_losses_header, container));
        listViewLossesItem.setAdapter(confirmLossesAdapter);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lossService.saveLoss(loss);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.loss_successful), Toast.LENGTH_SHORT).show();
                dismiss();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                    startActivity(activity.getIntent());
                }
            }
        });

        return view;
    }


}
