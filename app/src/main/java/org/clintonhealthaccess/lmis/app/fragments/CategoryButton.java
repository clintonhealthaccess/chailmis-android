package org.clintonhealthaccess.lmis.app.fragments;

import android.content.Context;
import android.widget.Button;

import org.clintonhealthaccess.lmis.app.models.Category;

public class CategoryButton extends Button {
    private Category category;

    public CategoryButton(Context context, Category category) {
        super(context);
        this.category = category;
    }

    public boolean isOf(Category category) {
        return this.category.equals(category);
    }
}
