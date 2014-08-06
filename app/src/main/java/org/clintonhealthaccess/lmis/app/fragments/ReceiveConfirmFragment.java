package org.clintonhealthaccess.lmis.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Receive;

import roboguice.fragment.RoboDialogFragment;

public class ReceiveConfirmFragment extends RoboDialogFragment{


    public static final String RECEIVE = "RECEIVE";
    private Receive receive;

    public static ReceiveConfirmFragment newInstance (Receive receive) {
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
        ListView listViewLossesItem = (ListView) view.findViewById(R.id.listViewConfirmReceive);

        return view;
    }
}
