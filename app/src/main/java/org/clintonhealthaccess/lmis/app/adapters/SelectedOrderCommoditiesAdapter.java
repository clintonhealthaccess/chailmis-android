/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

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

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.OrderActivity;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.OrderCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.events.OrderTypeChanged;
import org.clintonhealthaccess.lmis.app.models.OrderReason;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.utils.ViewHelpers;
import org.clintonhealthaccess.lmis.app.watchers.LmisTextWatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static org.apache.commons.lang3.time.DateUtils.toCalendar;

public class SelectedOrderCommoditiesAdapter extends ArrayAdapter<OrderCommodityViewModel> {

    public static final String DATE_FORMAT = "dd-MMM-yy";
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    public static final String ROUTINE = "Routine";
    public static final int MIN_ORDER_PERIOD = 7;
    private OrderActivity orderActivity;
    private List<OrderReason> unexpectedOrderReasons = new ArrayList<>();
    private OrderType orderType;


    public SelectedOrderCommoditiesAdapter(OrderActivity orderActivity, int resource, List<OrderCommodityViewModel> commodities, List<OrderReason> reasons, OrderType type) {
        super(orderActivity.getApplicationContext(), resource, commodities);
        unexpectedOrderReasons.addAll(reasons);
        unexpectedOrderReasons.add(0, new OrderReason(orderActivity.getString(R.string.select_reason)));
        this.orderType = type;
        this.orderActivity = orderActivity;
        EventBus.getDefault().register(this);
    }

    public void onEvent(OrderTypeChanged event) {
        this.orderType = event.getOrderType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = getRowView(parent);
        final OrderCommodityViewModel orderCommodityViewModel = getCommodityViewModel(position);
        TextView textViewCommodityName = (TextView) rowView.findViewById(R.id.textViewCommodityName);
        final EditText editTextOrderQuantity = (EditText) rowView.findViewById(R.id.editTextOrderQuantity);
        final Spinner spinnerUnexpectedReasons = (Spinner) rowView.findViewById(R.id.spinnerUnexpectedQuantityReasons);
        final TextView textViewStartDate = (TextView) rowView.findViewById(R.id.textViewStartDate);
        final TextView textViewEndDate = (TextView) rowView.findViewById(R.id.textViewEndDate);

        orderActivity.setupEditTextForNumberInput(editTextOrderQuantity);

        activateCancelButton((ImageButton) rowView.findViewById(R.id.imageButtonCancel), orderCommodityViewModel);
        textViewCommodityName.setText(orderCommodityViewModel.getName());
        setupSpinners(orderCommodityViewModel, spinnerUnexpectedReasons, textViewStartDate, textViewEndDate);
        setupQuantity(orderCommodityViewModel, editTextOrderQuantity, spinnerUnexpectedReasons);
        initialiseDates(orderCommodityViewModel, textViewStartDate, textViewEndDate);
        textViewStartDate.addTextChangedListener(new StartDateTextWatcher(orderCommodityViewModel, textViewStartDate, textViewEndDate, spinnerUnexpectedReasons));
        textViewEndDate.addTextChangedListener(new EndDateTextWatcher(orderCommodityViewModel, spinnerUnexpectedReasons));
        setDateTextClickListeners(textViewStartDate, textViewEndDate);
        setVisibilityOfUnexpectedReasonsSpinner(null, null, orderCommodityViewModel, spinnerUnexpectedReasons);
        doUpdateEndDate(orderCommodityViewModel, textViewStartDate, textViewEndDate);
        return rowView;
    }

    private void setupQuantity(OrderCommodityViewModel orderCommodityViewModel, EditText editTextOrderQuantity, Spinner spinnerUnexpectedReasons) {
        if (orderCommodityViewModel.getQuantityEntered() != 0) {
            editTextOrderQuantity.setText(String.format("%d", orderCommodityViewModel.getQuantityEntered()));
        }
        TextWatcher orderQuantityTextWatcher = new OrderQuantityTextWatcher(orderCommodityViewModel, spinnerUnexpectedReasons, editTextOrderQuantity, this);
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

    private void setupSpinners(final OrderCommodityViewModel orderCommodityViewModel, final Spinner spinnerUnexpectedReasons, final TextView textViewStartDate, final TextView textViewEndDate) {
        spinnerUnexpectedReasons.setPrompt("Select Reason");
        setupUnexpectedReasonsSpinner(spinnerUnexpectedReasons, orderCommodityViewModel);
        setVisibilityOfUnexpectedReasonsSpinner(null, null, orderCommodityViewModel, spinnerUnexpectedReasons);
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

    private void setVisibilityOfUnexpectedReasonsSpinner(String dateText, Date actualDate, OrderCommodityViewModel orderCommodityViewModel, Spinner spinnerUnexpectedReasons) {
        boolean isRoutine = orderType.isRoutine();
        if (orderCommodityViewModel.isUnexpectedReasonsSpinnerVisible(dateText, actualDate, isRoutine)) {
            spinnerUnexpectedReasons.setVisibility(View.VISIBLE);
        } else {
            spinnerUnexpectedReasons.setVisibility(View.INVISIBLE);
        }
    }


    private void setupUnexpectedReasonsSpinner(final Spinner spinnerUnexpectedReasons, final OrderCommodityViewModel orderCommodityViewModel) {
        spinnerUnexpectedReasons.setOnItemSelectedListener(new LmisOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orderCommodityViewModel.setUnexpectedReasonPosition(position);
                if (position != 0) {
                    orderCommodityViewModel.setReasonForUnexpectedOrderQuantity((OrderReason) spinnerUnexpectedReasons.getAdapter().getItem(position));
                }
            }
        });
        setupSpinnerData(spinnerUnexpectedReasons, unexpectedOrderReasons, orderCommodityViewModel.getUnexpectedReasonPosition());

    }

    private void setupSpinnerData(Spinner spinner, List<OrderReason> reasons, Integer position) {

        ArrayAdapter<OrderReason> adapter = new ReasonAdapter(getContext(), R.layout.spinner_item, reasons);
        spinner.setAdapter(adapter);

        if (position != null) {
            spinner.setSelection(position);
        } else {
            OrderReason routine = new OrderReason("Routine");
            if (reasons.contains(routine)) {
                spinner.setSelection(reasons.indexOf(routine));
            }
        }
    }

    private void doUpdateEndDate(OrderCommodityViewModel orderCommodityViewModel, TextView textViewStartDate, TextView textViewEndDate) {
        if (orderType.isRoutine()) {
            textViewEndDate.setText(SIMPLE_DATE_FORMAT.format(orderCommodityViewModel.getExpectedEndDate()));
            textViewEndDate.setEnabled(false);
        } else {
            textViewEndDate.setEnabled(true);
        }
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

    //FIXME hack to stop app from crashing when start period is set to its absolute maximum
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


    private class OrderQuantityTextWatcher extends LmisTextWatcher {
        private final OrderCommodityViewModel orderCommodityViewModel;
        private final Spinner spinnerUnexpectedReasons;
        private final EditText editTextOrderQuantity;
        private SelectedOrderCommoditiesAdapter adapter;

        public OrderQuantityTextWatcher(OrderCommodityViewModel orderCommodityViewModel1, Spinner spinnerUnexpectedReasons1, EditText editTextOrderQuantity1, SelectedOrderCommoditiesAdapter adapter1) {
            orderCommodityViewModel = orderCommodityViewModel1;
            spinnerUnexpectedReasons = spinnerUnexpectedReasons1;
            editTextOrderQuantity = editTextOrderQuantity1;
            adapter = adapter1;
        }

        @Override
        public void afterTextChanged(final Editable editable) {
            int quantityInt = ViewHelpers.getIntFromString(editable.toString());
            orderCommodityViewModel.setQuantityEntered(quantityInt);
            if (orderCommodityViewModel.quantityIsUnexpected()) {
                String commodityName = orderCommodityViewModel.getName();
                String message = getErrorMessage(commodityName);
                Toast.makeText(adapter.getContext(), message, Toast.LENGTH_SHORT).show();
            }
            setVisibilityOfUnexpectedReasonsSpinner(null, null, orderCommodityViewModel, spinnerUnexpectedReasons);
            if (quantityInt <= 0) {
                editTextOrderQuantity.setError(adapter.getContext().getString(R.string.orderQuantityMustBeGreaterThanZero));
            }
        }

        private String getErrorMessage(String commodityName) {
            String formatString = adapter.getContext().getString(R.string.unexpected_order_quantity_error);
            return String.format(formatString, commodityName, orderCommodityViewModel.getQuantityEntered());
        }
    }

    private class StartDateTextWatcher extends LmisTextWatcher {
        private final OrderCommodityViewModel orderCommodityViewModel;
        private final TextView textViewStartDate;
        private final TextView textViewEndDate;
        private final Spinner spinnerUnexpectedReasons;

        public StartDateTextWatcher(OrderCommodityViewModel orderCommodityViewModel, TextView textViewStartDate, TextView textViewEndDate, Spinner spinnerUnexpectedReasons) {
            this.orderCommodityViewModel = orderCommodityViewModel;
            this.textViewStartDate = textViewStartDate;
            this.textViewEndDate = textViewEndDate;
            this.spinnerUnexpectedReasons = spinnerUnexpectedReasons;
        }

        @Override
        public void afterTextChanged(Editable s) {
            doUpdateEndDate(orderCommodityViewModel, textViewStartDate, textViewEndDate);
            String startDate = s.toString();

            setVisibilityOfUnexpectedReasonsSpinner(startDate, orderCommodityViewModel.getOrderPeriodStartDate(), orderCommodityViewModel, spinnerUnexpectedReasons);

            try {
                orderCommodityViewModel.setOrderPeriodStartDate(SIMPLE_DATE_FORMAT.parse(startDate));
            } catch (ParseException ignored) {
            }
        }
    }

    private class EndDateTextWatcher extends LmisTextWatcher {
        private final OrderCommodityViewModel orderCommodityViewModel;
        private final Spinner spinnerUnexpectedReasons;

        public EndDateTextWatcher(OrderCommodityViewModel orderCommodityViewModel, Spinner spinnerUnexpectedReasons) {
            this.orderCommodityViewModel = orderCommodityViewModel;
            this.spinnerUnexpectedReasons = spinnerUnexpectedReasons;
        }

        @Override
        public void afterTextChanged(Editable s) {
            String endDate = s.toString();
            setVisibilityOfUnexpectedReasonsSpinner(endDate, orderCommodityViewModel.getOrderPeriodEndDate(), orderCommodityViewModel, spinnerUnexpectedReasons);
            try {
                orderCommodityViewModel.setOrderPeriodEndDate(SIMPLE_DATE_FORMAT.parse(endDate));
            } catch (ParseException ignored) {

            }
        }
    }
}
