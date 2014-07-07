package org.clintonhealthaccess.lmis.app.fragments;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.List;

import roboguice.inject.InjectView;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.parseColor;
import static android.util.Log.i;

public class ItemSelectFragment extends DialogFragment {
    private static final String CATEGORY = "param_category";
    private Category category;

    public ItemSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = (Category) getArguments().getSerializable(CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Window window = getDialog().getWindow();

        window.setGravity(Gravity.TOP | Gravity.LEFT);

        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 200;
        params.y = 50;
        window.setAttributes(params);

        getDialog().setCanceledOnTouchOutside(false);

        final View overlayView = inflater.inflate(R.layout.fragment_item_select, container, false);

        LinearLayout categoriesLayout = (LinearLayout) overlayView.findViewById(R.id.itemSelectOverlayCategories);

        List<Category> categoryList = Category.all();

        for (final Category category : categoryList) {
            Button button = new CategoryButton(getActivity(), category);

            button.setText(category.getName());

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCommodities(overlayView, category);
                }
            });

            categoriesLayout.addView(button);
        }

        showCommodities(overlayView, category);
        return overlayView;
    }

    private void showCommodities(View overlayView, Category currentCategory) {
        final LinearLayout itemsLayout = (LinearLayout) overlayView.findViewById(R.id.itemSelectOverlayItems);
        itemsLayout.removeViews(0, itemsLayout.getChildCount());
        for (Commodity commodity : currentCategory.getCommodities()) {
            Button commodityButton = new Button(getActivity());
            commodityButton.setText(commodity.getName());
            itemsLayout.addView(commodityButton);
        }

        LinearLayout categoriesLayout = (LinearLayout) overlayView.findViewById(R.id.itemSelectOverlayCategories);
        for (int i = 0; i < categoriesLayout.getChildCount(); i++) {
            CategoryButton button = (CategoryButton) categoriesLayout.getChildAt(i);
            if (button.isOf(currentCategory)) {
                button.setBackgroundColor(parseColor("#E5E4E2"));
            } else {
                button.setBackgroundColor(parseColor("#CCCCCC"));
            }
        }
    }

    public static ItemSelectFragment newInstance(Category category) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(CATEGORY, category);

        ItemSelectFragment fragment = new ItemSelectFragment();
        fragment.setArguments(arguments);
        return fragment;
    }
}
