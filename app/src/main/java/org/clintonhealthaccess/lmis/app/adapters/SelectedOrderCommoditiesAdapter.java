package org.clintonhealthaccess.lmis.app.adapters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

    public static final String DATE_FORMAT = "dd-MM-yy";
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
        final View rowView = inflater.inflate(R.layout.selected_order_commodity_list_item, parent, false);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        spinnerOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerOrderReasons);
        spinnerUnexpectedQuantityReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);
        editTextOrderQuantity = (EditText) rowView.findViewById(R.id.editTextOrderQuantity);

        final CommodityViewModel orderCommodityViewModel = getItem(position);
        orderCommodityViewModel.setQuantityPopulated(12);
        editTextOrderQuantity.setText(String.format("%d", orderCommodityViewModel.getQuantityEntered()));
        textViewCommodityName.setText(orderCommodityViewModel.getName());
        if (orderCommodityViewModel.quantityIsUnexpected()) {
            spinnerUnexpectedQuantityReasons.setVisibility(View.VISIBLE);
        } else {
            spinnerUnexpectedQuantityReasons.setVisibility(View.INVISIBLE);
        }

        setupDateControls(rowView, orderCommodityViewModel);

        activateCancelButton((ImageButton) rowView.findViewById(R.id.imageButtonCancel), orderCommodityViewModel);
        setupReasonsSpinner(OrderReason.UNEXPECTED_QUANTITY_JSON_KEY, spinnerUnexpectedQuantityReasons, orderCommodityViewModel.getOrderReasonPosition());
        setupReasonsSpinner(OrderReason.ORDER_REASONS_JSON_KEY, spinnerOrderReasons, rowView, orderCommodityViewModel);

        TextWatcher orderCommodityQuantityTextWatcher = new OrderQuantityTextWatcher(orderCommodityViewModel);

        editTextOrderQuantity.addTextChangedListener(orderCommodityQuantityTextWatcher);

        return rowView;
    }

    private TextWatcher getEditTextStartDateWatcher(final View rowView, final EditText editTextStartDate, final CommodityViewModel orderItemViewModel) {

        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                doUpdateEndDate(spinnerOrderReasons, rowView, orderItemViewModel);
                try {
                    orderItemViewModel.setOrderPeriodStartDate(simpleDateFormat.parse(editTextStartDate.getText().toString()));
                } catch (ParseException ignored) {
                }
            }
        };
    }

    private TextWatcher getEditTextEndDateWatcher(final EditText editTextEndDate, final CommodityViewModel orderItemViewModel) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    orderItemViewModel.setOrderPeriodEndDate(simpleDateFormat.parse(editTextEndDate.getText().toString()));
                } catch (ParseException ignored) {
                }
            }
        };
    }

    private void setupReasonsSpinner(String jsonKey, Spinner spinner, Integer orderReasonPosition) {
        List<OrderReason> orderReasons = filterReasonsWithType(reasons, jsonKey);
        List<String> strings = new ArrayList<>(transform(orderReasons, new Function<OrderReason, String>() {
            @Override
            public String apply(OrderReason reason) {
                return reason.getReason();
            }
        }));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, strings);
        spinner.setAdapter(adapter);

        if (orderReasonPosition != null) spinner.setSelection(orderReasonPosition);
    }

    private void setupReasonsSpinner(String jsonKey, Spinner spinner, final View rowView, final CommodityViewModel orderCommodityViewModel) {
        spinnerOrderReasons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orderCommodityViewModel.setOrderReasonPosition(position);
                doUpdateEndDate(spinnerOrderReasons, rowView, orderCommodityViewModel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        setupReasonsSpinner(jsonKey, spinner, orderCommodityViewModel.getOrderReasonPosition());
    }

    private void doUpdateEndDate(Spinner spinner, View rowView, CommodityViewModel orderCommodityViewModel) {
        String item = spinner.getItemAtPosition(orderCommodityViewModel.getOrderReasonPosition()).toString();
        EditText editTextEndDate = (EditText) rowView.findViewById(R.id.editTextEndDate);

        if (item.equalsIgnoreCase(ROUTINE)) {
            String startDate = ((TextView) rowView.findViewById(R.id.editTextStartDate)).getText().toString();
            populateEndDate(rowView, startDate, orderCommodityViewModel.getOrderDuration());
            editTextEndDate.setEnabled(false);
        } else {
            editTextEndDate.setEnabled(true);
        }
    }

    private void populateEndDate(View rowView, String startDate, Integer orderDuration) {
        if (!startDate.isEmpty()) {
            EditText editTextEndDate = (EditText) rowView.findViewById(R.id.editTextEndDate);
            editTextEndDate.setText(computeEndDate(startDate, orderDuration));
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

    private void setupDateControls(View rowView, CommodityViewModel orderCommodityViewModel) {
        final EditText editTextStartDate = (EditText) rowView.findViewById(R.id.editTextStartDate);
        final EditText editTextEndDate = (EditText) rowView.findViewById(R.id.editTextEndDate);

        if (orderCommodityViewModel.getOrderPeriodStartDate() != null)
            editTextStartDate.setText(simpleDateFormat.format(orderCommodityViewModel.getOrderPeriodStartDate()));

        if (orderCommodityViewModel.getOrderPeriodEndDate() != null)
            editTextEndDate.setText(simpleDateFormat.format(orderCommodityViewModel.getOrderPeriodEndDate()));

        editTextStartDate.addTextChangedListener(getEditTextStartDateWatcher(rowView, editTextStartDate, orderCommodityViewModel));
        editTextEndDate.addTextChangedListener(getEditTextEndDateWatcher(editTextEndDate, orderCommodityViewModel));
        editTextStartDate.setOnFocusChangeListener(getOpenDialogListener());
        editTextEndDate.setOnFocusChangeListener(getOpenDialogListener());
    }

    private View.OnFocusChangeListener getOpenDialogListener() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    showDateDialog(v);
            }
        };
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
        CommodityViewModel commodityViewModel = event.getCommodityViewModel();
        if (commodityViewModel.quantityIsUnexpected()) {
            String commodityName = commodityViewModel.getName();
            String message = String.format(getContext().getString(R.string.unexpected_order_quantity_error), commodityViewModel.getQuantityPopulated(), commodityName);
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        notifyDataSetChanged();
    }

    private String getDateString(int year, int monthOfYear, int dayOfMonth) {
        Calendar setCalender = Calendar.getInstance();
        setCalender.set(year, monthOfYear, dayOfMonth);
        return simpleDateFormat.format(setCalender.getTime());
    }
}
