package org.clintonhealthaccess.lmis.app.activities;

import android.support.v4.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.SelectedCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.fragments.ItemSelectFragment;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DispenseActivity extends BaseActivity {
    @Inject
    private CommoditiesRepository commoditiesRepository;

    protected ListView listViewSelectedCommodities;
    private SelectedCommoditiesAdapter adapter;
    protected ArrayList<Commodity> selectedCommodities = new ArrayList<Commodity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispense);
        listViewSelectedCommodities = (ListView) findViewById(R.id.listViewSelectedCommodities);
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
                    ItemSelectFragment dialog = ItemSelectFragment.newInstance(category, selectedCommodities);
                    dialog.show(fm, "selectCommodities");
                }
            });
            categoriesLayout.addView(button);
        }

        adapter = new SelectedCommoditiesAdapter(this, R.layout.commodity_list_item, new ArrayList<Commodity>());
        listViewSelectedCommodities.setAdapter(adapter);
        EventBus.getDefault().register(this);
    }

    public void onEvent(CommodityToggledEvent event) {
        Commodity commodity = event.getCommodity();
        if (selectedCommodities.contains(commodity)) {
            selectedCommodities.remove(commodity);
            adapter.remove(commodity);
        } else {
            adapter.add(commodity);
            selectedCommodities.add(commodity);
        }
        adapter.notifyDataSetChanged();
    }

}
