package org.clintonhealthaccess.lmis.app.views;

import android.content.Context;
import android.widget.Button;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;

public class CategoryButton extends Button {
    private final Category category;

    public CategoryButton(Context context, Category category) {
        super(context);
        this.category = category;

        setBackgroundResource(R.drawable.category_button_on_overlay);
        setText(category.getName());
    }

    public boolean isOf(Category category) {
        return this.category.equals(category);
    }
}
