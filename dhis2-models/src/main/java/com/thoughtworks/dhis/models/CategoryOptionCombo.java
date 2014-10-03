package com.thoughtworks.dhis.models;

import lombok.Data;
import lombok.experimental.Builder;

import java.util.List;

@Data
@Builder
public class CategoryOptionCombo {
    private String id;

    private String name, shortName;

    private String created;

    private String lastUpdated;

    private String href;

    private List<CategoryOption> categoryOptions;

    private CategoryCombo categoryCombo;
}
