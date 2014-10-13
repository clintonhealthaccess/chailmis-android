package com.thoughtworks.dhis.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Builder;

import static java.lang.String.format;

@Getter
@Setter
@Builder
@ToString
public class DataValue {
    private static final SimpleDateFormat MONTHLY_PERIOD_DATE_FORMAT = new SimpleDateFormat("yyyyMM");
    private static final SimpleDateFormat DAILY_PERIOD_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private String dataSet, dataElement, value, period, orgUnit, attributeOptionCombo;

    public Date periodAsDate() {
        try {
            return DAILY_PERIOD_DATE_FORMAT.parse(period);
        } catch (ParseException e) {
            try {
                return MONTHLY_PERIOD_DATE_FORMAT.parse(period);
            } catch (ParseException e1) {
                throw new Error(format("Invalid period date from DataValue [%s]", this), e);
            }
        }
    }
}
