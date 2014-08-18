/*
 * Copyright (c) 2014, Clinton Health Access Initiative
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

package org.clintonhealthaccess.lmis.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.adapters.CommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.app.views.CategoryButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import roboguice.fragment.RoboDialogFragment;

public class ItemSelectFragment extends RoboDialogFragment {
    public static final String COMMODITY_DISPLAY_STRATEGY = "Adapter";
    public static final String COMMODITIES_TO_VIEW_MODELS_CONVERTER = "param_view_model_generator";
    private static final String CATEGORY = "param_category";
    private static final String SELECTED_COMMODITIES = "param_selected_commodities";
    GridView gridViewCommodities;
    @Inject
    private CategoryService categoryService;
    private Category category;
    private LinearLayout categoriesLayout;
    private HashMap<Category, CommoditiesAdapter> adapterHashMap;
    private ArrayList<? extends BaseCommodityViewModel> selectedCommodities;
    private CommodityDisplayStrategy commodityDisplayStrategy;
    private CommoditiesToViewModelsConverter viewModelsConverter;

    public ItemSelectFragment() {
        // Required empty public constructor
    }

    public static ItemSelectFragment newInstance(Category category, ArrayList<?> selectedCommodities,
                                                 CommodityDisplayStrategy commodityDisplayStrategy, CommoditiesToViewModelsConverter generator) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(CATEGORY, category);
        arguments.putSerializable(SELECTED_COMMODITIES, selectedCommodities);
        arguments.putSerializable(COMMODITY_DISPLAY_STRATEGY, commodityDisplayStrategy);
        arguments.putSerializable(COMMODITIES_TO_VIEW_MODELS_CONVERTER, generator);

        ItemSelectFragment fragment = new ItemSelectFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = (Category) getArguments().getSerializable(CATEGORY);
            selectedCommodities = (ArrayList<? extends BaseCommodityViewModel>) getArguments().getSerializable(SELECTED_COMMODITIES);
            commodityDisplayStrategy = (CommodityDisplayStrategy) getArguments().getSerializable(COMMODITY_DISPLAY_STRATEGY);
            viewModelsConverter = (CommoditiesToViewModelsConverter) getArguments().getSerializable(COMMODITIES_TO_VIEW_MODELS_CONVERTER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupDialog();

        final View overlayView = inflater.inflate(R.layout.fragment_item_select, container, false);
        categoriesLayout = (LinearLayout) overlayView.findViewById(R.id.itemSelectOverlayCategories);
        setupCloseButton(overlayView);
        gridViewCommodities = (GridView) overlayView.findViewById(R.id.gridViewCommodities);
        List<Category> categoryList = categoryService.all();
        adapterHashMap = new LinkedHashMap<>();

        for (final Category category : categoryList) {
            Button button = new CategoryButton(getActivity(), category);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCommodities(category);
                }
            });

            List<? extends BaseCommodityViewModel> commodities = viewModelsConverter.execute(category.getCommodities());

            for (BaseCommodityViewModel viewModel : commodities) {
                if (selectedCommodities.contains(viewModel)) {
                    viewModel.toggleSelected();
                }
            }

            adapterHashMap.put(category, new CommoditiesAdapter(getActivity(), R.layout.commodity_list_item, (List<BaseCommodityViewModel>) commodities, commodityDisplayStrategy));
            categoriesLayout.addView(button);
        }

        showCommodities(category);
        return overlayView;
    }

    private void setupCloseButton(View overlayView) {
        Button buttonClose = (Button) overlayView.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void setupDialog() {
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    private void showCommodities(Category currentCategory) {
        final CommoditiesAdapter adapter = adapterHashMap.get(currentCategory);
        adapter.adaptGridViewCommodities(gridViewCommodities, commodityDisplayStrategy);

        for (int i = 0; i < categoriesLayout.getChildCount(); i++) {
            CategoryButton button = (CategoryButton) categoriesLayout.getChildAt(i);
            if (button.isOf(currentCategory)) {
                button.setSelected(true);
            } else {
                button.setSelected(false);
            }
        }
    }

}
