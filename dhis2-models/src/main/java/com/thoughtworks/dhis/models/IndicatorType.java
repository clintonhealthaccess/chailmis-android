package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class IndicatorType {
    private String id, name;
    private Boolean number;
    private int factor;
}
