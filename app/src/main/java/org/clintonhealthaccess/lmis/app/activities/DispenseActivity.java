package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;

import java.util.List;

public class DispenseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispense);

        LinearLayout categoriesLayout = (LinearLayout) findViewById(R.id.layoutCategories);

        List<Category> categoryList = Category.all();

        for (Category category : categoryList) {
            Button button = new Button(this);
            button.setText(category.getName());
            categoriesLayout.addView(button);
        }
    }


}
