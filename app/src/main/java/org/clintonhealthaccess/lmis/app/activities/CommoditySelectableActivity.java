package org.clintonhealthaccess.lmis.app.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.CategoryService;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.Lists.newArrayList;

abstract public class CommoditySelectableActivity extends BaseActivity {
    @Inject
    private CategoryService categoryService;

    @InjectView(R.id.listViewSelectedCommodities)
    ListView listViewSelectedCommodities;

    // FIXME: These methods should be private. Can we find a better way to test them?
    SelectedCommoditiesAdapter selectedCommoditiesAdapter;
    ArrayList<Commodity> selectedCommodities = newArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());

        setupCommodities();

        selectedCommoditiesAdapter = new SelectedCommoditiesAdapter(
                this, getSelectedCommoditiesAdapterId(), new ArrayList<Commodity>());

        listViewSelectedCommodities.setAdapter(selectedCommoditiesAdapter);

        afterCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    public void onEvent(CommodityToggledEvent event) {
        Commodity commodity = event.getCommodity();
        if (selectedCommodities.contains(commodity)) {
            selectedCommodities.remove(commodity);
            selectedCommoditiesAdapter.remove(commodity);
        } else {
            selectedCommoditiesAdapter.add(commodity);
            selectedCommodities.add(commodity);
        }
        selectedCommoditiesAdapter.notifyDataSetChanged();
        onCommoditySelectionChanged(selectedCommodities);
    }

    protected void onEachSelectedCommodity(SelectedCommodityHandler handler) {
        for (int i = 0; i < listViewSelectedCommodities.getChildCount(); i++) {
            View view = listViewSelectedCommodities.getChildAt(i);
            Commodity commodity = (Commodity) listViewSelectedCommodities.getAdapter().getItem(i);
            handler.operate(view, commodity);
        }
    }

    abstract protected int getSelectedCommoditiesAdapterId();

    abstract protected void onCommoditySelectionChanged(List<Commodity> selectedCommodities);

    abstract protected int getLayoutId();

    abstract protected void afterCreate(Bundle savedInstanceState);

    private void setupCommodities() {
        Drawable commodityButtonBackground = createCommodityButtonBackground();
        LinearLayout categoriesLayout = (LinearLayout) findViewById(R.id.layoutCategories);
        for (final Category category : categoryService.all()) {
            Button button = createCommoditySelectionButton(category, commodityButtonBackground);
            categoriesLayout.addView(button);
        }
    }

    private Drawable createCommodityButtonBackground() {
        Drawable commodityButtonBackground = getResources().getDrawable(R.drawable.arrow_black_right);
        commodityButtonBackground.setBounds(0, 0, 20, 30);
        return commodityButtonBackground;
    }

    private Button createCommoditySelectionButton(final Category category, Drawable background) {
        Button button = new Button(this);
        button.setBackgroundResource(R.drawable.category_button_on_overlay);
        button.setCompoundDrawables(null, null, background, null);
        button.setText(category.getName());
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                ItemSelectFragment dialog = ItemSelectFragment.newInstance(category, selectedCommodities);
                dialog.show(fm, "selectCommodities");
            }
        });
        return button;
    }

    protected interface SelectedCommodityHandler {
        void operate(View view, Commodity commodity);
    }
}
