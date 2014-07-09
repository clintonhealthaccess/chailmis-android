package org.clintonhealthaccess.lmis.app.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Dispensing;

import roboguice.fragment.RoboDialogFragment;


public class DispenseConfirmationFragment extends RoboDialogFragment {

    public static final String DISPENSING = "param_dispensings";
    private Dispensing dispensing;

    Button buttonDispenseConfirm;

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

        buttonDispenseConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }


}
