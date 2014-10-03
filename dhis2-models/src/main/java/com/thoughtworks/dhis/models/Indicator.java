package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class Indicator {
    private String id, name, numerator, numeratorDescription, denominator, denominatorDescription,shortName;
    private IndicatorType indicatorType;
}
