package org.clintonhealthaccess.lmis.app.models.api;

public class SmsValue {
    private String dataElement;
    private String value;

    public SmsValue(String dataElement, String value) {
        this.dataElement = dataElement;
        this.value = value;
    }

    @Override
    public String toString() {
        return dataElement + "." + value;
    }
}
