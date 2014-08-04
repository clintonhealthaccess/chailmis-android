package org.clintonhealthaccess.lmis.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Loss;

import roboguice.fragment.RoboDialogFragment;

public class LossesConfirmationFragment extends RoboDialogFragment {

    public static final String LOSSES = "Losses";

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_losses_confirmation, container, false);
        return view;
    }
}
