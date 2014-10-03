package com.thoughtworks.dhis.configurations;

import com.thoughtworks.dhis.models.ExcelCategory;
import com.thoughtworks.dhis.models.ExcelCommodity;

public interface IActor {
    public void beforeEachCategory(ExcelCategory category);

    public void afterEachCategory(ExcelCategory category);

    public void onEachCommodity(ExcelCommodity commodity);
}
