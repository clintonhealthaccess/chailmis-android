package org.clintonhealthaccess.lmis.app.adapters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
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

import com.google.common.base.Predicate;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.google.common.collect.Collections2.filter;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.toCalendar;

public class SelectedOrderCommoditiesAdapter extends ArrayAdapter<OrderCommodityViewModel> {

    public static final String DATE_FORMAT = "dd-MMM-yy";
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    public static final String ROUTINE = "Routine";
    public static final int MIN_ORDER_PERIOD = 7;
    private List<OrderReason> unexpectedOrderReasons;
    private List<OrderReason> orderReasons;


    public SelectedOrderCommoditiesAdapter(Context context, int resource, List<OrderCommodityViewModel> commodities, List<OrderReason> reasons) {
        super(context, resource, commodities);
        unexpectedOrderReasons = filterReasonsWithType(reasons, OrderReason.UNEXPECTED_QUANTITY_JSON_KEY);
        orderReasons = filterReasonsWithType(reasons, OrderReason.ORDER_REASONS_JSON_KEY);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = getRowView(parent);
        final OrderCommodityViewModel orderCommodityViewModel = getCommodityViewModel(position);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        final EditText editTextOrderQuantity = (EditText) rowView.findViewById(R.id.editTextOrderQuantity);
        final Spinner spinnerOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerOrderReasons);
        final Spinner spinnerUnexpectedReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);
        final TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);
        final TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);
        activateCancelButton((ImageButton) rowView.findViewById(R.id.imageButtonCancel), orderCommodityViewModel);
        textViewCommodityName.setText(orderCommodityViewModel.getName());
        setupSpinners(orderCommodityViewModel, spinnerOrderReasons, spinnerUnexpectedReasons, textViewStartDate, textViewEndDate);
        setupQuantity(orderCommodityViewModel, editTextOrderQuantity, spinnerUnexpectedReasons);
        setupDateControls(orderCommodityViewModel, spinnerOrderReasons, textViewStartDate, textViewEndDate);
        return rowView;
    }

    private void setupDateControls(OrderCommodityViewModel orderCommodityViewModel, Spinner spinnerOrderReasons, TextView textViewStartDate, TextView textViewEndDate) {
        initialiseDates(orderCommodityViewModel, textViewStartDate, textViewEndDate);
        textViewStartDate.addTextChangedListener(getStartDateTextWatcher(orderCommodityViewModel, spinnerOrderReasons, textViewStartDate, textViewEndDate));
        textViewEndDate.addTextChangedListener(getEditTextEndDateWatcher(orderCommodityViewModel));
        setDateTextClickListeners(textViewStartDate, textViewEndDate);
    }

    private void setupQuantity(OrderCommodityViewModel orderCommodityViewModel, EditText editTextOrderQuantity, Spinner spinnerUnexpectedReasons) {
        editTextOrderQuantity.setText(String.format("%d", orderCommodityViewModel.getQuantityEntered()));
        TextWatcher orderQuantityTextWatcher = new OrderQuantityTextWatcher(this, orderCommodityViewModel, spinnerUnexpectedReasons, editTextOrderQuantity);
        editTextOrderQuantity.addTextChangedListener(orderQuantityTextWatcher);
    }

    private OrderCommodityViewModel getCommodityViewModel(int position) {
        final OrderCommodityViewModel orderCommodityViewModel = getItem(position);
        //FIXME when order is pre-populated
        orderCommodityViewModel.setExpectedOrderQuantity(12);
        return orderCommodityViewModel;
    }

    private View getRowView(ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.selected_order_commodity_list_item, parent, false);
    }

    private void setupSpinners(OrderCommodityViewModel orderCommodityViewModel, Spinner spinnerOrderReasons, Spinner spinnerUnexpectedReasons, TextView textViewStartDate, TextView textViewEndDate) {
        setupOrderReasonsSpinner(spinnerOrderReasons, textViewStartDate, orderCommodityViewModel, textViewEndDate);
        setupUnexpectedReasonsSpinner(spinnerUnexpectedReasons, orderCommodityViewModel);
        setupUnexpectedReasonsSpinnerVisibility(orderCommodityViewModel, spinnerUnexpectedReasons);
    }

    private void setDateTextClickListeners(final TextView textViewStartDate, final TextView textViewEndDate) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog(v, textViewEndDate, textViewStartDate);
            }
        };
        textViewStartDate.setOnClickListener(onClickListener);
        textViewEndDate.setOnClickListener(onClickListener);
    }

    private void initialiseDates(OrderCommodityViewModel orderCommodityViewModel, TextView textViewStartDate, TextView textViewEndDate) {
        if (orderCommodityViewModel.getOrderPeriodStartDate() != null) {
            textViewStartDate.setText(SIMPLE_DATE_FORMAT.format(orderCommodityViewModel.getOrderPeriodStartDate()));
        }

        if (orderCommodityViewModel.getOrderPeriodEndDate() != null) {
            textViewEndDate.setText(SIMPLE_DATE_FORMAT.format(orderCommodityViewModel.getOrderPeriodEndDate()));
        }
    }

    public void setupUnexpectedReasonsSpinnerVisibility(OrderCommodityViewModel orderCommodityViewModel, Spinner spinnerUnexpectedQuantityReasons) {
        if (orderCommodityViewModel.quantityIsUnexpected()) {
            spinnerUnexpectedQuantityReasons.setVisibility(View.VISIBLE);
        } else {
            spinnerUnexpectedQuantityReasons.setVisibility(View.INVISIBLE);
        }
    }

    private LmisTextWatcher getStartDateTextWatcher(final OrderCommodityViewModel orderCommodityViewModel, final Spinner spinnerOrderReasons, final TextView textViewStartDate, final TextView textViewEndDate) {
        return new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                doUpdateEndDate(spinnerOrderReasons, orderCommodityViewModel, textViewStartDate, textViewEndDate);
                try {
                    String startDate = s.toString();
                    orderCommodityViewModel.setOrderPeriodStartDate(SIMPLE_DATE_FORMAT.parse(startDate));
                } catch (ParseException ignored) {
                }
            }
        };
    }

    protected TextWatcher getEditTextEndDateWatcher(final OrderCommodityViewModel orderItemViewModel) {
        return new LmisTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String endDate = s.toString();
                    orderItemViewModel.setOrderPeriodEndDate(SIMPLE_DATE_FORMAT.parse(endDate));
                } catch (ParseException ignored) {
                }
            }
        };
    }

    private void setupUnexpectedReasonsSpinner(Spinner spinnerUnexpectedQuantityReasons, final OrderCommodityViewModel orderCommodityViewModel) {
        spinnerUnexpectedQuantityReasons.setOnItemSelectedListener(new LmisOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //FIXME Unexpected reason not being set here.
                orderCommodityViewModel.setUnexpectedReasonPosition(position);
            }
        });
        setupSpinnerData(spinnerUnexpectedQuantityReasons, unexpectedOrderReasons, orderCommodityViewModel.getUnexpectedReasonPosition());

    }

    private void setupOrderReasonsSpinner(final Spinner spinnerOrderReasons, final TextView textViewStartDate, final OrderCommodityViewModel orderCommodityViewModel, final TextView textViewEndDate) {
        spinnerOrderReasons.setOnItemSelectedListener(new LmisOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orderCommodityViewModel.setOrderReasonPosition(position);
                OrderReason orderReason = getReason(((OrderReason) spinnerOrderReasons.getSelectedItem()).getReason());
                orderCommodityViewModel.setReasonForOrder(orderReason);
                doUpdateEndDate(spinnerOrderReasons, orderCommodityViewModel, textViewStartDate, textViewEndDate);
            }

        });
        setupSpinnerData(spinnerOrderReasons, orderReasons, orderCommodityViewModel.getOrderReasonPosition());
    }

    private void setupSpinnerData(Spinner spinner, List<OrderReason> reasons, Integer position) {

        ArrayAdapter<OrderReason> adapter = new ReasonAdapter(getContext(), R.layout.spinner_item, reasons);
        spinner.setAdapter(adapter);

        if (position != null) {
            spinner.setSelection(position);
        }
    }

    private OrderReason getReason(String reasonName) {
        for (OrderReason reason : orderReasons) {
            if (reason.getReason().equals(reasonName)) {
                return reason;
            }
        }
        return null;
    }

    private void doUpdateEndDate(Spinner spinner, OrderCommodityViewModel orderCommodityViewModel, TextView textViewStartDate, TextView textViewEndDate) {
        Integer orderReasonPosition = orderCommodityViewModel.getOrderReasonPosition();
        String item = ((OrderReason) spinner.getItemAtPosition(orderReasonPosition)).getReason();
        if (item.equalsIgnoreCase(ROUTINE)) {
            String startDate = textViewStartDate.getText().toString();
            populateEndDate(startDate, 30, textViewEndDate);
            textViewEndDate.setEnabled(false);
        } else {
            textViewEndDate.setEnabled(true);
        }
    }

    private void populateEndDate(String startDate, Integer orderDuration, TextView textViewEndDate) {
        if (!startDate.isEmpty()) {
            textViewEndDate.setText(computeEndDate(startDate, orderDuration));
        }
    }

    private String computeEndDate(String startDate, int addDays) {
        String endDate = null;
        try {
            Date date = SIMPLE_DATE_FORMAT.parse(startDate);
            endDate = SIMPLE_DATE_FORMAT.format(addDays(date, addDays));
        } catch (ParseException ignored) {
        }
        return endDate;
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

    private void activateCancelButton(ImageButton imageButtonCancel, final OrderCommodityViewModel orderCommodityViewModel) {
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CommodityToggledEvent(orderCommodityViewModel));
            }
        });
    }

    private void showDateDialog(View view, TextView textViewEndDate, TextView textViewStartDate) {
        final TextView textViewDate = (TextView) view;
        String date = textViewDate.getText().toString();
        Calendar calendar = getCalendarFromString(date);
        Calendar calendarMinDate = Calendar.getInstance();
        Calendar calendarMaxDate = null;
        if (textViewDate.getId() == R.id.textViewEndDate) {
            calendarMinDate = getMinEndDate(textViewStartDate);
            calendarMinDate.add(Calendar.DAY_OF_MONTH, MIN_ORDER_PERIOD);
        }
        if (textViewDate.getId() == R.id.textViewStartDate) {
            calendarMaxDate = getMaxStartDate(textViewEndDate);
            setTimeFieldsOnMaxStartDate(calendarMinDate, calendarMaxDate);
        }
        openDialog(textViewDate, calendar, calendarMinDate, calendarMaxDate);
    }

    private Calendar getCalendarFromString(String date) {
        Calendar calendar = Calendar.getInstance();
        if (!date.isEmpty()) {
            try {
                calendar = toCalendar(SIMPLE_DATE_FORMAT.parse(date));
            } catch (ParseException ignored) {
            }
        }
        return calendar;
    }

    //FIXME hack to stop app from crashing when start date is set to its absolute maximum
    private void setTimeFieldsOnMaxStartDate(Calendar calendarDateWithTimeFieldsSet, Calendar calendarMaxDate) {
        if (calendarMaxDate != null) {
            calendarMaxDate.set(Calendar.HOUR_OF_DAY, calendarDateWithTimeFieldsSet.get(Calendar.HOUR_OF_DAY));
            calendarMaxDate.set(Calendar.MINUTE, calendarDateWithTimeFieldsSet.get(Calendar.MINUTE));
            calendarMaxDate.set(Calendar.SECOND, calendarDateWithTimeFieldsSet.get(Calendar.SECOND));
            calendarMaxDate.set(Calendar.MILLISECOND, calendarDateWithTimeFieldsSet.get(Calendar.MILLISECOND));
            calendarMaxDate.add(Calendar.SECOND, 1);
        }
    }

    private Calendar getMaxStartDate(TextView textViewEndDate) {
        String endDateText = textViewEndDate.getText().toString();
        if (!endDateText.isEmpty()) {
            Calendar calendarMaxDate = Calendar.getInstance();
            try {
                calendarMaxDate.setTime(SIMPLE_DATE_FORMAT.parse(endDateText));
                calendarMaxDate.add(Calendar.DAY_OF_MONTH, -MIN_ORDER_PERIOD);
                return calendarMaxDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Calendar getMinEndDate(TextView textViewStartDate) {
        Calendar calendarMinDate = Calendar.getInstance();
        String startDateText = textViewStartDate.getText().toString();
        if (!startDateText.isEmpty()) {
            calendarMinDate = Calendar.getInstance();
            try {
                calendarMinDate.setTime(SIMPLE_DATE_FORMAT.parse(startDateText));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return calendarMinDate;
    }

    private void openDialog(final TextView textViewDate, Calendar calendar, Calendar calendarMinDate, Calendar calendarMaxDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                textViewDate.setText(getDateString(year, monthOfYear, dayOfMonth));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        setMaxAndMinDate(calendarMinDate, calendarMaxDate, datePickerDialog);
        datePickerDialog.show();
    }

    private void setMaxAndMinDate(Calendar calendarMinDate, Calendar calendarMaxDate, DatePickerDialog datePickerDialog) {
        DatePicker datePicker = datePickerDialog.getDatePicker();
        if (calendarMaxDate != null) {
            datePicker.setMaxDate(calendarMaxDate.getTimeInMillis());
            if (calendarMinDate.before(calendarMaxDate)) {
                datePicker.setMinDate(calendarMinDate.getTimeInMillis());
            }
        } else {
            datePicker.setMinDate(calendarMinDate.getTimeInMillis());
        }
    }

    private String getDateString(int year, int monthOfYear, int dayOfMonth) {
        Calendar setCalender = Calendar.getInstance();
        setCalender.set(year, monthOfYear, dayOfMonth);
        return SIMPLE_DATE_FORMAT.format(setCalender.getTime());
    }

    private class LmisOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

}
