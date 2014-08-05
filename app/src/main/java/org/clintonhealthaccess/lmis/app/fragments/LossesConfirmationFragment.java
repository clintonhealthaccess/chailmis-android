package org.clintonhealthaccess.lmis.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

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
            }
        });

        return view;
    }


}
