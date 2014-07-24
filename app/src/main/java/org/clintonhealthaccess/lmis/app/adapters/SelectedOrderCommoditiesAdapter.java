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
import android.widget.Toast;

import com.google.common.base.Predicate;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.events.OrderQuantityChangedEvent;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;
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
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.toCalendar;

public class SelectedOrderCommoditiesAdapter extends ArrayAdapter<CommodityViewModel> {

    public static final String DATE_FORMAT = "dd-MMM-yy";
    public static final String ROUTINE = "Routine";
    public static final int MIN_DIFFERENCE_BETWEEN_START_END = 7;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    private List<OrderReason> unexpectedOrderReasons;
    private List<OrderReason> orderReasons;


    public SelectedOrderCommoditiesAdapter(Context context, int resource, List<CommodityViewModel> commodities, List<OrderReason> reasons) {
        super(context, resource, commodities);
        unexpectedOrderReasons = filterReasonsWithType(reasons, OrderReason.UNEXPECTED_QUANTITY_JSON_KEY);
        orderReasons = filterReasonsWithType(reasons, OrderReason.ORDER_REASONS_JSON_KEY);
        EventBus.getDefault().register(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.selected_order_commodity_list_item, parent, false);
        final CommodityViewModel orderCommodityViewModel = getItem(position);
        //FIXME when order is pre-populated
        orderCommodityViewModel.setExpectedOrderQuantity(12);

        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        EditText editTextOrderQuantity = (EditText) rowView.findViewById(R.id.editTextOrderQuantity);
        final Spinner spinnerOrderReasons = (Spinner) rowView.findViewById(R.id.spinnerOrderReasons);
        Spinner spinnerUnexpectedQuantityReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);
        final TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);
        final TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);

        editTextOrderQuantity.setText(String.format("%d", orderCommodityViewModel.getQuantityEntered()));

        textViewCommodityName.setText(orderCommodityViewModel.getName());
        setupUnexpectedReasonsSpinnerVisibility(orderCommodityViewModel, spinnerUnexpectedQuantityReasons);
        initialiseDates(orderCommodityViewModel, textViewStartDate, textViewEndDate);

        textViewStartDate.addTextChangedListener(getStartDateTextWatcher(orderCommodityViewModel, spinnerOrderReasons, textViewStartDate, textViewEndDate));
        textViewEndDate.addTextChangedListener(getEditTextEndDateWatcher(orderCommodityViewModel));
        setDateTextClickListeners(textViewStartDate, textViewEndDate);
        activateCancelButton((ImageButton) rowView.findViewById(R.id.imageButtonCancel), orderCommodityViewModel);
        setupOrderReasonsSpinner(spinnerOrderReasons, textViewStartDate, orderCommodityViewModel, textViewEndDate);
        setupUnexpectedReasonsSpinner(spinnerUnexpectedQuantityReasons, orderCommodityViewModel);
        TextWatcher orderCommodityQuantityTextWatcher = new OrderQuantityTextWatcher(orderCommodityViewModel);
        editTextOrderQuantity.addTextChangedListener(orderCommodityQuantityTextWatcher);
        return rowView;
    }

    private void setDateTextClickListeners(TextView textViewStartDate, TextView textViewEndDate) {
        View.OnClickListener onClickListener = getOnDateTextViewClickListener(textViewStartDate, textViewEndDate);
        textViewStartDate.setOnClickListener(onClickListener);
        textViewEndDate.setOnClickListener(onClickListener);
    }

    private View.OnClickListener getOnDateTextViewClickListener(final TextView textViewStartDate, final TextView textViewEndDate) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog(v, textViewEndDate, textViewStartDate);
            }
        };
    }

    private LmisTextWatcher getStartDateTextWatcher(final CommodityViewModel orderCommodityViewModel, final Spinner spinnerOrderReasons, final TextView textViewStartDate, final TextView textViewEndDate) {
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

    private void initialiseDates(CommodityViewModel orderCommodityViewModel, TextView textViewStartDate, TextView textViewEndDate) {
        if (orderCommodityViewModel.getOrderPeriodStartDate() != null)
            textViewStartDate.setText(SIMPLE_DATE_FORMAT.format(orderCommodityViewModel.getOrderPeriodStartDate()));

        if (orderCommodityViewModel.getOrderPeriodEndDate() != null)
            textViewEndDate.setText(SIMPLE_DATE_FORMAT.format(orderCommodityViewModel.getOrderPeriodEndDate()));
    }

    private void setupUnexpectedReasonsSpinnerVisibility(CommodityViewModel orderCommodityViewModel, Spinner spinnerUnexpectedQuantityReasons) {
        if (orderCommodityViewModel.quantityIsUnexpected()) {
            spinnerUnexpectedQuantityReasons.setVisibility(View.VISIBLE);
        } else {
            spinnerUnexpectedQuantityReasons.setVisibility(View.INVISIBLE);
        }
    }

    protected TextWatcher getEditTextEndDateWatcher(final CommodityViewModel orderItemViewModel) {
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

    private void setupUnexpectedReasonsSpinner(Spinner spinnerUnexpectedQuantityReasons, final CommodityViewModel orderCommodityViewModel) {
        spinnerUnexpectedQuantityReasons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                orderCommodityViewModel.setUnexpectedReasonPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Integer unexpectedReasonPosition = orderCommodityViewModel.getUnexpectedReasonPosition();
        ArrayAdapter<OrderReason> adapter = new ReasonAdapter(getContext(), R.layout.spinner_item, unexpectedOrderReasons);
        spinnerUnexpectedQuantityReasons.setAdapter(adapter);

        if (unexpectedReasonPosition != null)
            spinnerUnexpectedQuantityReasons.setSelection(unexpectedReasonPosition);
    }

    private void setupOrderReasonsSpinner(final Spinner spinnerOrderReasons, final TextView textViewStartDate, final CommodityViewModel orderCommodityViewModel, final TextView textViewEndDate) {
        spinnerOrderReasons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orderCommodityViewModel.setOrderReasonPosition(position);
                OrderReason orderReason = getReason(spinnerOrderReasons.getAdapter().getItem(position).toString());
                orderCommodityViewModel.setReasonForOrder(orderReason);
                doUpdateEndDate(spinnerOrderReasons, orderCommodityViewModel, textViewStartDate, textViewEndDate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Integer orderReasonPosition = orderCommodityViewModel.getOrderReasonPosition();

        ArrayAdapter<OrderReason> adapter = new ReasonAdapter(getContext(), R.layout.spinner_item, orderReasons);
        spinnerOrderReasons.setAdapter(adapter);

        if (orderReasonPosition != null) spinnerOrderReasons.setSelection(orderReasonPosition);

    }

    OrderReason getReason(String reasonName) {
        for(OrderReason reason: orderReasons) {
            if(reason.getReason().equals(reasonName)) {
                return reason;
            }
        }
        return null;
    }

    private void doUpdateEndDate(Spinner spinner, CommodityViewModel orderCommodityViewModel, TextView textViewStartDate, TextView textViewEndDate) {
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

    private void activateCancelButton(ImageButton imageButtonCancel, final CommodityViewModel orderCommodityViewModel) {
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
        Calendar calendar = Calendar.getInstance();

        if (!date.isEmpty()) {
            try {
                calendar = toCalendar(SIMPLE_DATE_FORMAT.parse(date));
            } catch (ParseException ignored) {
            }
        }

        Calendar calendarMinDate = Calendar.getInstance();
        Calendar calendarMaxDate = null;

        if (textViewDate.getId() == R.id.textViewEndDate) {
            calendarMinDate = getMinEndDate(calendarMinDate, textViewStartDate);
        }

        if (textViewDate.getId() == R.id.textViewStartDate) {
            calendarMaxDate = getMaxStartDate(calendarMaxDate, textViewEndDate);
            setTimeFieldsOnMaxStartDate(calendarMinDate, calendarMaxDate);
        }

        openDialog(textViewDate, calendar, calendarMinDate, calendarMaxDate);
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

    private Calendar getMaxStartDate(Calendar calendarMaxDate, TextView textViewEndDate) {
        String endDateText = textViewEndDate.getText().toString();
        if (!endDateText.isEmpty()) {
            calendarMaxDate = Calendar.getInstance();
            try {
                calendarMaxDate.setTime(SIMPLE_DATE_FORMAT.parse(endDateText));
                calendarMaxDate.add(Calendar.DAY_OF_MONTH, -MIN_DIFFERENCE_BETWEEN_START_END);
                return calendarMaxDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Calendar getMinEndDate(Calendar calendarMinDate, TextView textViewStartDate) {
        String startDateText = textViewStartDate.getText().toString();
        if (!startDateText.isEmpty()) {
            calendarMinDate = Calendar.getInstance();
            try {
                calendarMinDate.setTime(SIMPLE_DATE_FORMAT.parse(startDateText));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        calendarMinDate.add(Calendar.DAY_OF_MONTH, MIN_DIFFERENCE_BETWEEN_START_END);
        return calendarMinDate;
    }

    private void openDialog(final TextView textViewDate, Calendar calendar, Calendar calendarMindate, Calendar calendarMaxDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                textViewDate.setText(getDateString(year, monthOfYear, dayOfMonth));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        DatePicker datePicker = datePickerDialog.getDatePicker();
        if (calendarMaxDate != null) {
            datePicker.setMaxDate(calendarMaxDate.getTimeInMillis());
            if (calendarMindate.before(calendarMaxDate))
                datePicker.setMinDate(calendarMindate.getTimeInMillis());
        } else {
            datePicker.setMinDate(calendarMindate.getTimeInMillis());
        }


        datePickerDialog.show();
    }

    public void onEventMainThread(OrderQuantityChangedEvent event) {
        CommodityViewModel commodityViewModel = event.getCommodityViewModel();
        if (commodityViewModel.quantityIsUnexpected()) {
            String commodityName = commodityViewModel.getName();
            String message = String.format(getContext().getString(R.string.unexpected_order_quantity_error), commodityViewModel.getExpectedOrderQuantity(), commodityName);
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        notifyDataSetChanged();
    }

    private String getDateString(int year, int monthOfYear, int dayOfMonth) {
        Calendar setCalender = Calendar.getInstance();
        setCalender.set(year, monthOfYear, dayOfMonth);
        return SIMPLE_DATE_FORMAT.format(setCalender.getTime());
    }
}
