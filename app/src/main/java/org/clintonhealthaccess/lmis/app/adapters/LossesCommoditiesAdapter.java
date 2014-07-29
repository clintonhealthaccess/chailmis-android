package org.clintonhealthaccess.lmis.app.adapters;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesViewModelCommands;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.util.List;

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
        EditText editTextWastages = (EditText) rowView.findViewById(R.id.editTextWastages);
        EditText editTextDamages = (EditText) rowView.findViewById(R.id.editTextDamages);
        EditText editTextExpiries = (EditText) rowView.findViewById(R.id.editTextExpiries);
        EditText editTextMissing = (EditText) rowView.findViewById(R.id.editTextMissing);

        final LossesCommodityViewModel viewModel = getItem(position);

        setupTextWatcher(editTextWastages, new LossesViewModelCommands.SetWastageCommand(), viewModel);
        setupTextWatcher(editTextDamages, new LossesViewModelCommands.SetDamagesCommand(), viewModel);
        setupTextWatcher(editTextExpiries, new LossesViewModelCommands.SetExpiriesCommand(), viewModel);
        setupTextWatcher(editTextMissing, new LossesViewModelCommands.SetMissingCommand(), viewModel);

        return rowView;
    }

    private void setupTextWatcher(EditText editText, final LossesViewModelCommands.Command command, final LossesCommodityViewModel viewModel) {
        editText.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                command.execute(viewModel, editable);
            }
        });
    }

}
