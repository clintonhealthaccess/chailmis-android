package org.clintonhealthaccess.lmis.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.adapters.CommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository;
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

    @Inject
    CommoditiesRepository commoditiesRepository;

    private Category category;

    ListView listViewCommodities;


    private LinearLayout categoriesLayout;
    private HashMap<Category, CommoditiesAdapter> adapterHashMap;
    private ArrayList<Commodity> selectedCommodities;
    private Button buttonClose;

    public ItemSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = (Category) getArguments().getSerializable(CATEGORY);
            selectedCommodities = (ArrayList<Commodity>) getArguments().getSerializable(SELECTED_COMMODITIES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupDialog();

        final View overlayView = inflater.inflate(R.layout.fragment_item_select, container, false);
        categoriesLayout = (LinearLayout) overlayView.findViewById(R.id.itemSelectOverlayCategories);
        setupCloseButton(overlayView);
        listViewCommodities = (ListView) overlayView.findViewById(R.id.listViewCommodities);
        List<Category> categoryList = commoditiesRepository.allCategories();
        adapterHashMap = new LinkedHashMap<>();

        for (final Category category : categoryList) {
            Button button = new CategoryButton(getActivity(), category);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCommodities(category);
                }
            });

            for (Commodity commodity : category.getCommodities()) {
                if (selectedCommodities.contains(commodity)) {
                    commodity.toggleSelected();
                }
            }

            adapterHashMap.put(category, new CommoditiesAdapter(getActivity(), R.layout.commodity_list_item, category.getCommodities()));
            categoriesLayout.addView(button);
        }

        showCommodities(category);
        return overlayView;
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
        listViewCommodities.setAdapter(adapter);
        listViewCommodities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.getItem(i).toggleSelected();
                adapter.notifyDataSetChanged();
                EventBus.getDefault().post(new CommodityToggledEvent(adapter.getItem(i)));
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

    public static ItemSelectFragment newInstance(Category category, ArrayList<Commodity> selectedCommodities) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(CATEGORY, category);
        arguments.putSerializable(SELECTED_COMMODITIES, selectedCommodities);

        ItemSelectFragment fragment = new ItemSelectFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

}
