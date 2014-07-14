package org.clintonhealthaccess.lmis.app.fragments;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

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
        confirmDispenseAdapter = new ConfirmDispenseAdapter(getActivity(), R.layout.confirm_commodity_list_item, dispensing.getDispensingItems());
        listViewConfirmItems.addHeaderView(inflater.inflate(R.layout.confirm_header,container));
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
                dispensingService.addDispensing(dispensing);
                dismiss();
                FragmentActivity activity = getActivity();
                if (activity != null)
                    activity.finish();
            }
        });

        buttonDispenseGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }


}
