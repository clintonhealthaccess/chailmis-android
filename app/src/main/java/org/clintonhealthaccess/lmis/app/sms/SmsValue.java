package org.clintonhealthaccess.lmis.app.sms;

import com.thoughtworks.dhis.models.DataValue;

import java.io.Serializable;

public class SmsValue implements Serializable {
    private String dataElement;
    private String value;

    SmsValue(DataValue dataValue) {
        this.dataElement = dataValue.getDataElement();
        this.value = dataValue.getValue();
    }

    @Override
    public String toString() {
        return dataElement + "." + value;
    }
}
