package com.thoughtworks.dhis.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExcelCommodity {
    private final boolean nonLGA;
    private String name;

    public ExcelCommodity(String name, boolean nonLGA) {
        this.name = name;
        this.nonLGA = nonLGA;
    }
}
