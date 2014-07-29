package org.clintonhealthaccess.lmis.app.models.api;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

@Getter
@Setter
@Builder
public class CategoryCombo {
    private String name;
    private String id;
    private List<CategoryOptionCombos> categoryOptionCombos;

}
