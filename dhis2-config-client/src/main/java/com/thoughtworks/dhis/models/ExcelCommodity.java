package com.thoughtworks.dhis.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExcelCommodity {
    private String name;
    public ExcelCommodity(String name) {
        this.name = name;
    }
}
