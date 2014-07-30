package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesViewModelCommands;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.util.List;

import de.greenrobot.event.EventBus;

public class LossesCommoditiesAdapter extends ArrayAdapter<LossesCommodityViewModel> {

    private int resource;

    public LossesCommoditiesAdapter(Context context, int resource, List<LossesCommodityViewModel> commodities) {
        super(context, resource, commodities);

        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(resource, parent, false);

        final LossesCommodityViewModel viewModel = getItem(position);

        EditText editTextWastages = (EditText) rowView.findViewById(R.id.editTextWastages);
        EditText editTextDamages = (EditText) rowView.findViewById(R.id.editTextDamages);
        EditText editTextExpiries = (EditText) rowView.findViewById(R.id.editTextExpiries);
        EditText editTextMissing = (EditText) rowView.findViewById(R.id.editTextMissing);

        preloadDataInto(viewModel, editTextWastages, editTextDamages, editTextExpiries, editTextMissing);

        syncChangesWithViewModelFrom(viewModel, editTextWastages, editTextDamages, editTextExpiries, editTextMissing);

        ImageButton imageButtonCancel = (ImageButton)rowView.findViewById(R.id.imageButtonCancel);
        activateCancelButton(imageButtonCancel, viewModel);
        return rowView;
    }

    private void syncChangesWithViewModelFrom(LossesCommodityViewModel viewModel, EditText editTextWastages, EditText editTextDamages, EditText editTextExpiries, EditText editTextMissing) {
        setupTextWatcher(editTextWastages, new LossesViewModelCommands.SetWastageCommand(), viewModel);
        setupTextWatcher(editTextDamages, new LossesViewModelCommands.SetDamagesCommand(), viewModel);
        setupTextWatcher(editTextExpiries, new LossesViewModelCommands.SetExpiriesCommand(), viewModel);
        setupTextWatcher(editTextMissing, new LossesViewModelCommands.SetMissingCommand(), viewModel);
    }

    private void preloadDataInto(LossesCommodityViewModel viewModel, EditText editTextWastages, EditText editTextDamages, EditText editTextExpiries, EditText editTextMissing) {
        editTextWastages.setText(String.valueOf(viewModel.getWastage()));
        editTextDamages.setText(String.valueOf(viewModel.getDamages()));
        editTextExpiries.setText(String.valueOf(viewModel.getExpiries()));
        editTextMissing.setText(String.valueOf(viewModel.getMissing()));
    }

    private void setupTextWatcher(EditText editText, final LossesViewModelCommands.Command command, final LossesCommodityViewModel viewModel) {
        editText.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                command.execute(viewModel, editable);
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
