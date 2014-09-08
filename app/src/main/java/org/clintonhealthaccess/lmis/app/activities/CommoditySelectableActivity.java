/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.clintonhealthaccess.lmis.app.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.fragments.ItemSelectFragment;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.services.CategoryService;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.Lists.newArrayList;

abstract public class CommoditySelectableActivity extends BaseActivity {
    @InjectView(R.id.gridViewSelectedCommodities)
    GridView gridViewSelectedCommodities;
    ArrayAdapter arrayAdapter;
    ArrayList<BaseCommodityViewModel> selectedCommodities = newArrayList();
    @Inject
    private CategoryService categoryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.transparent);
        setContentView(getLayoutId());
        setupCategories();
        beforeArrayAdapterCreate(savedInstanceState);
        arrayAdapter = getArrayAdapter();
        gridViewSelectedCommodities.setAdapter(arrayAdapter);

        afterCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    public void onEvent(CommodityToggledEvent event) {
        BaseCommodityViewModel commodity = event.getCommodity();
        if (selectedCommodities.contains(commodity)) {
            selectedCommodities.remove(commodity);
            arrayAdapter.remove(commodity);
        } else {
            arrayAdapter.add(commodity);
            selectedCommodities.add(commodity);
        }
        onCommoditySelectionChanged(selectedCommodities);
    }

    protected void onEachSelectedCommodity(SelectedCommodityHandler handler) {
        for (int i = 0; i < gridViewSelectedCommodities.getChildCount(); i++) {
            View view = gridViewSelectedCommodities.getChildAt(i);
            BaseCommodityViewModel commodityViewModel = (BaseCommodityViewModel) gridViewSelectedCommodities.getAdapter().getItem(i);
            handler.operate(view, commodityViewModel);
        }
    }

    protected void onCommoditySelectionChanged(List<BaseCommodityViewModel> selectedCommodities) {
        Button submitButton = getSubmitButton();
        if (selectedCommodities.size() > 0) {
            submitButton.setVisibility(View.VISIBLE);
        } else {
            submitButton.setVisibility(View.INVISIBLE);
        }
    }

    abstract protected Button getSubmitButton();

    abstract protected int getLayoutId();

    abstract protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy();

    abstract protected ArrayAdapter getArrayAdapter();

    abstract protected void afterCreate(Bundle savedInstanceState);

    abstract protected void beforeArrayAdapterCreate(Bundle savedInstanceState);

    abstract protected CommoditiesToViewModelsConverter getViewModelConverter();

    private void setupCategories() {
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
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                ItemSelectFragment dialog = ItemSelectFragment.newInstance(category, selectedCommodities,
                        getCheckBoxVisibilityStrategy(), getViewModelConverter());
                dialog.show(fragmentManager, "selectCommodities");
            }
        });
        return button;
    }

    protected interface SelectedCommodityHandler {
        void operate(View view, BaseCommodityViewModel commodityViewModel);
    }
}
