package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

import java.util.List;

@Data
@Builder
public class Category {
    private String name;
    private String shortName;
    private String id;
    private List<CategoryOption> categoryOptions;
    private String dataDimensionType;
    private String dimensionType;
}
