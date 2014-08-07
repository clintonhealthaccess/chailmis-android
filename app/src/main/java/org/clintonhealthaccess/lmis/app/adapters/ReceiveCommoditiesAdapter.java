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
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.util.List;

import de.greenrobot.event.EventBus;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;

public class ReceiveCommoditiesAdapter extends ArrayAdapter<ReceiveCommodityViewModel> {

    private int resource;

    public ReceiveCommoditiesAdapter(Context context, int resource, List<ReceiveCommodityViewModel> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, parent, false);

        holder.textViewCommodityName = (TextView) convertView.findViewById(R.id.textViewCommodityName);
        holder.imageButtonCancel = (ImageButton) convertView.findViewById(R.id.imageButtonCancel);
        holder.editTextAllocatedQuantity = (EditText) convertView.findViewById(R.id.editTextAllocatedQuantity);
        holder.editTextReceivedQuantity = (EditText) convertView.findViewById(R.id.editTextReceivedQuantity);
        holder.textViewDifferenceQuantity = (TextView) convertView.findViewById(R.id.textViewDifferenceQuantity);


        ReceiveCommodityViewModel viewModel = getItem(position);
        initialiseQuantities(holder, viewModel);
        activateCancelButton(holder.imageButtonCancel, viewModel);

        setupTextWatchers(holder, viewModel);
        return convertView;
    }

    private void initialiseQuantities(ViewHolder holder, ReceiveCommodityViewModel viewModel) {
        holder.textViewCommodityName.setText(viewModel.getCommodity().getName());

        if (viewModel.getQuantityAllocated() != 0) {
            holder.editTextAllocatedQuantity.setText(String.valueOf(viewModel.getQuantityAllocated()));
        }
        if (viewModel.getQuantityReceived() != 0) {
            holder.editTextReceivedQuantity.setText(String.valueOf(viewModel.getQuantityReceived()));
        }

        holder.textViewDifferenceQuantity.setText(String.valueOf(viewModel.getDifference()));
        holder.editTextAllocatedQuantity.setEnabled(!viewModel.isQuantityAllocatedDisabled());
    }

    private void setupTextWatchers(final ViewHolder viewHolder, final ReceiveCommodityViewModel receiveCommodityViewModel) {
        viewHolder.editTextAllocatedQuantity.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                receiveCommodityViewModel.setQuantityAllocated(getIntFromString(s.toString()));
                viewHolder.textViewDifferenceQuantity.setText(String.valueOf(receiveCommodityViewModel.getDifference()));
            }
        });

        viewHolder.editTextReceivedQuantity.addTextChangedListener(new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                receiveCommodityViewModel.setQuantityReceived(getIntFromString(s.toString()));
                viewHolder.textViewDifferenceQuantity.setText(String.valueOf(receiveCommodityViewModel.getDifference()));
            }
        });
    }

    static class ViewHolder {
        TextView textViewCommodityName;
        ImageButton imageButtonCancel;
        EditText editTextAllocatedQuantity;
        EditText editTextReceivedQuantity;
        TextView textViewDifferenceQuantity;
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final ReceiveCommodityViewModel viewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CommodityToggledEvent(viewModel));
            }
        });
    }
}
