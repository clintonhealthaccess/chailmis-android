package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesViewModelCommands;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.util.List;

import de.greenrobot.event.EventBus;

public class LossesCommoditiesAdapter extends ArrayAdapter<LossesCommodityViewModel> {

    private final int resource;

    public LossesCommoditiesAdapter(Context context, int resource, List<LossesCommodityViewModel> commodities) {
        super(context, resource, commodities);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);
        final LossesCommodityViewModel viewModel = getItem(position);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        textViewCommodityName.setText(viewModel.getName());

        setUpDamages(textViewCommodityName, viewModel, (EditText) rowView.findViewById(R.id.editTextDamages));
        setUpWastages(textViewCommodityName, viewModel, (EditText) rowView.findViewById(R.id.editTextWastages));
        setUpExpiries(textViewCommodityName, viewModel, (EditText) rowView.findViewById(R.id.editTextExpiries));
        setUpMissing(textViewCommodityName, viewModel, (EditText) rowView.findViewById(R.id.editTextMissing));
        activateCancelButton((ImageButton)rowView.findViewById(R.id.imageButtonCancel), viewModel);
        return rowView;
    }

    private void setUpWastages(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editTextWastages){
        editTextWastages.setText(String.valueOf(viewModel.getWastage()));
        setupTextWatcher(textViewCommodityName, editTextWastages, new LossesViewModelCommands.SetWastageCommand(), viewModel);
    }

    private void setUpDamages(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editTextDamages){
        editTextDamages.setText(String.valueOf(viewModel.getDamages()));
        setupTextWatcher(textViewCommodityName, editTextDamages, new LossesViewModelCommands.SetDamagesCommand(), viewModel);
    }

    private void setUpExpiries(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editTextExpiries){
        editTextExpiries.setText(String.valueOf(viewModel.getExpiries()));
        setupTextWatcher(textViewCommodityName, editTextExpiries, new LossesViewModelCommands.SetExpiriesCommand(), viewModel);
    }

    private void setUpMissing(TextView textViewCommodityName, LossesCommodityViewModel viewModel, EditText editTextMissing){
        editTextMissing.setText(String.valueOf(viewModel.getMissing()));
        setupTextWatcher(textViewCommodityName, editTextMissing, new LossesViewModelCommands.SetMissingCommand(), viewModel);
    }

    private void setupTextWatcher(final TextView textViewCommodityName, final EditText editText, final LossesViewModelCommands.Command command, final LossesCommodityViewModel viewModel) {
        editText.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                command.execute(viewModel, editable);
                int losses = viewModel.totalLosses();
                int stockOnHand = viewModel.getStockOnHand();
                if(losses > stockOnHand) {
                    textViewCommodityName.setError(String.format(getContext().getString(R.string.totalLossesGreaterThanStockAtHand), losses, stockOnHand));
                }else{
                    textViewCommodityName.setError(null);
                }
            }
        });
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final LossesCommodityViewModel viewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CommodityToggledEvent(viewModel));
            }
        });
    }

}
