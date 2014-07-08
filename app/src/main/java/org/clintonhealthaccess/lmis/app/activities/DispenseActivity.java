package org.clintonhealthaccess.lmis.app.activities;

import android.support.v4.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.fragments.ItemSelectFragment;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository;

import java.util.List;

public class DispenseActivity extends BaseActivity {
    @Inject
    private CommoditiesRepository commoditiesRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispense);

        LinearLayout categoriesLayout = (LinearLayout) findViewById(R.id.layoutCategories);

        List<Category> categoryList = commoditiesRepository.allCategories();

        Drawable drawable = getResources().getDrawable(R.drawable.arrow_black_right);
        drawable.setBounds(0, 0, 20,30);

        for (final Category category : categoryList) {
            Button button = new Button(this);

            button.setBackgroundResource(R.drawable.category_button_on_overlay);

            button.setCompoundDrawables(
                    null, null, drawable, null);

            button.setText(category.getName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getSupportFragmentManager();
                    ItemSelectFragment dialog = ItemSelectFragment.newInstance(category);
                    dialog.show(fm, "selectCommodities");
                }
            });
            categoriesLayout.addView(button);
        }
    }


}
