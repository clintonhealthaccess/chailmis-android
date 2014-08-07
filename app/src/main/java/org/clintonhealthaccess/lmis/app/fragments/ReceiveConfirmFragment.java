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
import org.clintonhealthaccess.lmis.app.adapters.ConfirmReceiveAdapter;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.services.ReceiveService;

import roboguice.fragment.RoboDialogFragment;

public class ReceiveConfirmFragment extends RoboDialogFragment{

    public static final String RECEIVE = "RECEIVE";
    private Receive receive;

    @Inject
    private ReceiveService receiveService;

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
                receiveService.saveReceive(receive);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.receive_successful), Toast.LENGTH_LONG).show();
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
