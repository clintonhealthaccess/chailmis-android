package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
public class CategoryOption {
    private String name;
    private String shortName;
    private String id;
}
