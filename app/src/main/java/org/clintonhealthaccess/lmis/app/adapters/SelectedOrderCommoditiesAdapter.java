package org.clintonhealthaccess.lmis.app.adapters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.OrderReason;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

public class SelectedOrderCommoditiesAdapter extends ArrayAdapter<CommodityViewModel> {

    private List<OrderReason> reasons;

    public SelectedOrderCommoditiesAdapter(Context context, int resource, List<CommodityViewModel> commodities, List<OrderReason> reasons) {
        super(context, resource, commodities);
        this.reasons = reasons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.selected_order_commodity_list_item, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);

        final CommodityViewModel commodityViewModel = getItem(position);
        textViewCommodityName.setText(commodityViewModel.getName());

        setupDateControls(rowView);

        activateCancelButton((ImageButton) rowView.findViewById(R.id.imageButtonCancel), commodityViewModel);

        setupOrderReasonsSpinner(rowView);

        return rowView;
    }

    private void setupOrderReasonsSpinner(View rowView) {
        Spinner spinnerOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerOrderReasons);

        List<OrderReason> orderReasons = filterReasonsWithType(reasons, OrderReason.ORDER_REASONS_JSON_KEY);

        List<String> strings = new ArrayList<>(transform(orderReasons, new Function<OrderReason, String>() {
            @Override
            public String apply(OrderReason reason) {
                return reason.getReason();
            }
        }));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, strings);
        spinnerOrderReasons.setAdapter(adapter);
    }

    private void setupDateControls(View rowView) {
        final TextView textViewStartDate = (TextView) rowView.findViewById(R.id.editTextStartDate);
        final TextView textViewEndDate = (TextView) rowView.findViewById(R.id.editTextEndDate);

        View.OnClickListener openDateDialog = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(view);
            }
        };
        textViewStartDate.setOnClickListener(openDateDialog);
        textViewEndDate.setOnClickListener(openDateDialog);
    }

    private List<OrderReason> filterReasonsWithType(List<OrderReason> reasons, final String type) {
        Collection<OrderReason> reasonsCollection = filter(reasons, new Predicate<OrderReason>() {
            @Override
            public boolean apply(OrderReason reason) {
                return reason.getType().equals(type);
            }
        });
        return new ArrayList<>(reasonsCollection);
    }

    private void activateCancelButton(ImageButton imageButtonCancel, final CommodityViewModel commodityViewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CommodityToggledEvent(commodityViewModel));
            }
        });
    }

    private void showDateDialog(View view) {
        final EditText editText = (EditText) view;
        String date = editText.getText().toString();

        if (date.isEmpty()) {
            final Calendar calendar = Calendar.getInstance();
            openDialog(editText, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            String[] dates = date.split("-");
            openDialog(editText, Integer.parseInt(dates[2]), (Integer.parseInt(dates[1]) - 1), Integer.parseInt(dates[0]));
        }
    }

    private void openDialog(final EditText editText, int mYear, int mMonth, int mDay) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                editText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
            }
        }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }


}
