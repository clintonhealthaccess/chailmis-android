package org.clintonhealthaccess.lmis.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewModels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.CommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.app.views.CategoryButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboDialogFragment;

public class ItemSelectFragment extends RoboDialogFragment {
    private static final String CATEGORY = "param_category";
    private static final String SELECTED_COMMODITIES = "param_selected_commodities";
    public static final String STRATEGY = "Adapter";

    @Inject
    private CategoryService categoryService;

    private Category category;

    GridView gridViewCommodities;


    private LinearLayout categoriesLayout;
    private HashMap<Category, CommoditiesAdapter> adapterHashMap;
    private ArrayList<CommodityViewModel> selectedCommodities;
    private Button buttonClose;
    private Boolean strategy;

    public ItemSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = (Category) getArguments().getSerializable(CATEGORY);
            selectedCommodities = (ArrayList<CommodityViewModel>) getArguments().getSerializable(SELECTED_COMMODITIES);
            strategy = (Boolean) getArguments().getSerializable(STRATEGY);
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

            List<CommodityViewModel> commodities = convertToViewModelsList(category.getCommodities());

            for (CommodityViewModel commodityViewModel : commodities) {
                if (selectedCommodities.contains(commodityViewModel)) {
                    commodityViewModel.toggleSelected();
                }
            }

            if (strategy) {
                adapterHashMap.put(category, new CommoditiesAdapter(getActivity(), R.layout.commodity_list_item, commodities, CommoditiesAdapter.DO_NOTHING));
            } else {
                adapterHashMap.put(category, new CommoditiesAdapter(getActivity(), R.layout.commodity_list_item, commodities));
            }

            categoriesLayout.addView(button);
        }

        showCommodities(category);
        return overlayView;
    }

    private List<CommodityViewModel> convertToViewModelsList(List<Commodity> commodities) {
        List<CommodityViewModel> listToReturn = new ArrayList<>();
        for (Commodity commodity : commodities) {
            listToReturn.add(new CommodityViewModel(commodity));
        }
        return listToReturn;
    }

    private void setupCloseButton(View overlayView) {
        buttonClose = (Button) overlayView.findViewById(R.id.buttonClose);
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
        gridViewCommodities.setAdapter(adapter);
        gridViewCommodities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CommodityViewModel commodityViewModel = adapter.getItem(i);
                if (!commodityViewModel.getCommodity().stockIsFinished()) {
                    commodityViewModel.toggleSelected();
                    EventBus.getDefault().post(new CommodityToggledEvent(adapter.getItem(i)));
                    adapter.notifyDataSetChanged();
                }
            }
        });

        for (int i = 0; i < categoriesLayout.getChildCount(); i++) {
            CategoryButton button = (CategoryButton) categoriesLayout.getChildAt(i);
            if (button.isOf(currentCategory)) {
                button.setSelected(true);
            } else {
                button.setSelected(false);
            }
        }
    }

    public static ItemSelectFragment newInstance(Category category, ArrayList<CommodityViewModel> selectedCommodities, boolean checkboxVisibilityFlag) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(CATEGORY, category);
        arguments.putSerializable(SELECTED_COMMODITIES, selectedCommodities);
        arguments.putSerializable(STRATEGY,  checkboxVisibilityFlag);

        ItemSelectFragment fragment = new ItemSelectFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

}
