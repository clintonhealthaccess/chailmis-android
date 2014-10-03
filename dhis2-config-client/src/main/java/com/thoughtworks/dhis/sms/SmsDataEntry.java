package com.thoughtworks.dhis.sms;

import com.thoughtworks.dhis.models.DataElement;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SmsDataEntry {
    private final String name;
    private final String shortCode;

    public SmsDataEntry(DataElement dataElement) {
        this.name = dataElement.getName();
        this.shortCode = dataElement.getId();
    }
}
