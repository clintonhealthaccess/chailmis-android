package org.clintonhealthaccess.lmis.app.sms;

import org.clintonhealthaccess.lmis.app.models.api.DataValue;

public class SmsValue {
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
