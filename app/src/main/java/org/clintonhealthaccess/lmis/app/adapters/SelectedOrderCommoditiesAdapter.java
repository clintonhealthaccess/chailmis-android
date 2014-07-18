package org.clintonhealthaccess.lmis.app.adapters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import org.clintonhealthaccess.lmis.app.events.OrderQuantityChangedEvent;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.watchers.OrderQuantityTextWatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.toCalendar;

public class SelectedOrderCommoditiesAdapter extends ArrayAdapter<CommodityViewModel> {

    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String ROUTINE = "Routine";
    private List<OrderReason> reasons;
    private EditText editTextOrderQuantity;
    private Spinner spinnerOrderReasons;
    private Spinner spinnerUnexpectedQuantityReasons;
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

    public SelectedOrderCommoditiesAdapter(Context context, int resource, List<CommodityViewModel> commodities, List<OrderReason> reasons) {
        super(context, resource, commodities);
        this.reasons = reasons;
        EventBus.getDefault().register(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.selected_order_commodity_list_item, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        spinnerOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerOrderReasons);
        spinnerUnexpectedQuantityReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);
        editTextOrderQuantity = (EditText) rowView.findViewById(R.id.editTextOrderQuantity);
        final CommodityViewModel orderItemViewModel = getItem(position);
        orderItemViewModel.setQuantityPopulated(12);
        editTextOrderQuantity.setText(String.format("%d", orderItemViewModel.getQuantityEntered()));

        textViewCommodityName.setText(orderItemViewModel.getName());
        if (orderItemViewModel.quantityIsUnexpected()) {
            spinnerUnexpectedQuantityReasons.setVisibility(View.VISIBLE);
        } else {
            spinnerUnexpectedQuantityReasons.setVisibility(View.INVISIBLE);
        }

        setupDateControls(rowView);

        activateCancelButton((ImageButton) rowView.findViewById(R.id.imageButtonCancel), orderItemViewModel);

        setupReasonsSpinner(OrderReason.UNEXPECTED_QUANTITY_JSON_KEY, spinnerUnexpectedQuantityReasons);
        setupReasonsSpinner(OrderReason.ORDER_REASONS_JSON_KEY, spinnerOrderReasons, rowView);

        TextWatcher orderCommodityQuantityTextWatcher = new OrderQuantityTextWatcher(orderItemViewModel);
        editTextOrderQuantity.addTextChangedListener(orderCommodityQuantityTextWatcher);

        return rowView;
    }

    private void setupReasonsSpinner(String jsonKey, Spinner spinner) {
        List<OrderReason> orderReasons = filterReasonsWithType(reasons, jsonKey);
        List<String> strings = new ArrayList<>(transform(orderReasons, new Function<OrderReason, String>() {
            @Override
            public String apply(OrderReason reason) {
                return reason.getReason();
            }
        }));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, strings);
        spinner.setAdapter(adapter);
    }

    private void setupReasonsSpinner(String jsonKey, Spinner spinner, final View rowView) {
        spinnerOrderReasons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                doUpdateEndDate(spinnerOrderReasons, rowView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupReasonsSpinner(jsonKey, spinner);
    }

    private void doUpdateEndDate(Spinner spinner, View rowView) {
        String item = spinner.getSelectedItem().toString();
        if (item.equalsIgnoreCase(ROUTINE)) {
            String startDate = ((TextView) rowView.findViewById(R.id.editTextStartDate)).getText().toString();
            populateEndDate(rowView, startDate);
        }
    }

    private void populateEndDate(View rowView, String startDate) {
        if (!startDate.isEmpty()) {
            TextView textViewEndDate = (TextView) rowView.findViewById(R.id.editTextEndDate);
            textViewEndDate.setText(computeEndDate(startDate, 30));
        }
    }

    private String computeEndDate(String startDate, int addDays) {
        String endDate = null;
        try {
            Date date = simpleDateFormat.parse(startDate);
            endDate = simpleDateFormat.format(addDays(date, addDays));
        } catch (ParseException ignored) {
        }
        return endDate;
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

    private void activateCancelButton(ImageButton imageButtonCancel, final CommodityViewModel orderItemViewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CommodityToggledEvent(orderItemViewModel));
            }
        });
    }

    private void showDateDialog(View view) {
        final EditText editText = (EditText) view;
        String date = editText.getText().toString();
        Calendar calendar = Calendar.getInstance();

        if (!date.isEmpty()) {
            try {
                calendar = toCalendar(simpleDateFormat.parse(date));
            } catch (ParseException ignored) {
            }
        }
        openDialog(editText, calendar);
    }

    private void openDialog(final EditText editText, Calendar calendar) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                editText.setText(getDateString(year, monthOfYear, dayOfMonth));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    public void onEventMainThread(OrderQuantityChangedEvent event) {
        notifyDataSetChanged();
    }

    private String getDateString(int year, int monthOfYear, int dayOfMonth) {
        Calendar setCalender = Calendar.getInstance();
        setCalender.set(year, monthOfYear, dayOfMonth);
        return simpleDateFormat.format(setCalender.getTime());
    }
}
