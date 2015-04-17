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

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommoditiesToViewModelsConverter;
import org.clintonhealthaccess.lmis.app.adapters.SearchCommodityAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.events.NumberTextViewFocusChanged;
import org.clintonhealthaccess.lmis.app.events.NumberTextViewOnClick;
import org.clintonhealthaccess.lmis.app.events.NumberTextViewOnTouch;
import org.clintonhealthaccess.lmis.app.fragments.ItemSelectFragment;
import org.clintonhealthaccess.lmis.app.listeners.NumberKeyBoardActionListener;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.CategoryService;
import org.clintonhealthaccess.lmis.app.services.CommodityService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

abstract public class CommoditySelectableActivity extends BaseActivity implements Serializable {

    @Inject
    CommodityService commodityService;

    public final static int CodeDelete = -5;
    public final static int CodeCancel = -3;
    protected final ViewValidator<EditText> INVALID_AMOUNT = new ViewValidator<EditText>(R.string.dispense_submit_validation_message_zero, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            try {
                return Integer.parseInt(editTextQuantity.getText().toString()) <= 0;
            } catch (NumberFormatException ex) {
                return false;
            }

        }
    }, R.id.editTextQuantity);
    protected final ViewValidator<EditText> EMPTY = new ViewValidator<EditText>(R.string.dispense_submit_validation_message_filled, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            return editTextQuantity.getText().toString().isEmpty();
        }
    }, R.id.editTextQuantity);
    protected final ViewValidator<EditText> HAS_ERROR = new ViewValidator<EditText>(R.string.dispense_submit_validation_message_errors, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            return editTextQuantity.getError() != null;
        }
    }, R.id.editTextQuantity);

    @InjectView(R.id.gridViewSelectedCommodities)
    GridView gridViewSelectedCommodities;

    ArrayAdapter arrayAdapter;

    ArrayList<BaseCommodityViewModel> selectedCommodities = newArrayList();

    Keyboard keyBoard;

    @Inject
    private CategoryService categoryService;

    @InjectView(R.id.keyBoardView)
    public KeyboardView keyBoardView;

    @InjectView(R.id.scrollCategoriesContainer)
    LinearLayout scrollCategoriesContainer;

    @InjectView(R.id.autoCompleteTextViewCommodities)
    AutoCompleteTextView autoCompleteTextViewCommodities;

    public SearchCommodityAdapter searchCommodityAdapter;

    public CommoditySelectableActivity thisActivity = this;

    public boolean dialogIsShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.transparent);
        setContentView(getLayoutId());
        setupKeyBoard();
        beforeSetUpCommoditySearch();
        setUpCommoditySearch();
        setupCategories();
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

        onCommodityToggledEventCompleted();
    }

    public void onCommodityToggledEventCompleted() {
        //Log.e("Toggled", "toggled");
        searchCommodityAdapter = new SearchCommodityAdapter(this,
                R.layout.search_commodity_item, newArrayList(getCommoditiesThatCanBeSelected()));
        autoCompleteTextViewCommodities.setAdapter(searchCommodityAdapter);
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

    abstract protected String getActivityName();

    protected boolean hasInvalidEditTextField(List<ViewValidator<EditText>> validators) {
        for (final ViewValidator validator : validators) {
            if (!validator.isValid()) {
                showToastMessage(validator.toastMessage());
                return true;
            }
        }
        return false;
    }

    protected boolean hasInvalidSpinnerField(List<ViewValidator<Spinner>> validators) {
        for (final ViewValidator validator : validators) {
            if (!validator.isValid()) {
                showToastMessage(validator.toastMessage());
                return true;
            }
        }
        return false;
    }

    abstract protected int getLayoutId();

    abstract protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy();

    abstract protected ArrayAdapter getArrayAdapter();

    protected void setupKeyBoard() {
        keyBoard = new Keyboard(CommoditySelectableActivity.this, R.xml.keyboard);
        keyBoardView.setKeyboard(keyBoard);
        keyBoardView.setPreviewEnabled(false);
        keyBoardView.setOnKeyboardActionListener(new NumberKeyBoardActionListener(this));
    }

    abstract protected void afterCreate(Bundle savedInstanceState);

    protected void setUpCommoditySearch() {
        //stop search field from getting default focus
        scrollCategoriesContainer.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollCategoriesContainer.setFocusableInTouchMode(true);

        searchCommodityAdapter = new SearchCommodityAdapter(this,
                R.layout.search_commodity_item, newArrayList(getCommoditiesThatCanBeSelected()));
        autoCompleteTextViewCommodities.setAdapter(searchCommodityAdapter);
        autoCompleteTextViewCommodities.setOnItemClickListener(getAutoCompleteTextViewCommoditiesAdapterListener());
    }

    protected void beforeSetUpCommoditySearch() {

    }

    abstract protected AdapterView.OnItemClickListener getAutoCompleteTextViewCommoditiesAdapterListener();

    private List<Commodity> getCommoditiesThatCanBeSelected() {
        return from(commodityService.all()).filter(new Predicate<Commodity>() {
            @Override
            public boolean apply(Commodity input) {
                return !commodityHasBeenSelected(input) && getCheckBoxVisibilityStrategy()
                        .allowClick(new BaseCommodityViewModel(input));
            }
        }).toList();
    }

    private boolean commodityHasBeenSelected(Commodity input) {
        return selectedCommodities.contains(new BaseCommodityViewModel(input));
    }


    public void hideCustomKeyboard() {
        keyBoardView.setVisibility(View.GONE);
        keyBoardView.setEnabled(false);
    }

    public void showCustomKeyboard(View v) {
        keyBoardView.setVisibility(View.VISIBLE);
        keyBoardView.setEnabled(true);
        if (v != null)
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public boolean isCustomKeyboardVisible() {
        return keyBoardView.getVisibility() == View.VISIBLE;
    }

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

                if (dialogIsShowing == true) {
                    return;
                }
                dialogIsShowing = true;

                FragmentManager fragmentManager = getSupportFragmentManager();
                ItemSelectFragment dialog = ItemSelectFragment.newInstance(thisActivity, category, selectedCommodities,
                        getCheckBoxVisibilityStrategy(), getViewModelConverter(), getActivityName());
                if (!dialog.isVisible()) {
                    dialog.show(fragmentManager, "selectCommodities");
                }
            }
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        button.setWidth(200);
        return button;
    }

    public class ViewValidator<T extends View> {
        private final Predicate<T> predicate;
        private int toastMessageStringId;
        private int viewId;

        public ViewValidator(int stringId, Predicate<T> predicate, int viewId) {
            toastMessageStringId = stringId;
            this.predicate = predicate;
            this.viewId = viewId;
        }

        public boolean isValid() {
            return filter(wrap(gridViewSelectedCommodities), new Predicate<View>() {
                @Override
                public boolean apply(View view) {
                    T childView = (T) view.findViewById(viewId);
                    return predicate.apply(childView);
                }
            }).isEmpty();
        }

        private List<View> wrap(GridView gridView) {
            List<View> result = newArrayList();
            for (int i = 0; i < gridView.getChildCount(); i++) {
                result.add(gridView.getChildAt(i));
            }
            return result;
        }

        public String toastMessage() {
            return getString(toastMessageStringId);
        }
    }

    public void onEvent(NumberTextViewFocusChanged event) {
        if (event.hasFocus) showCustomKeyboard(event.view);
        else hideCustomKeyboard();
    }

    public void onEvent(NumberTextViewOnClick event) {
        showCustomKeyboard(event.view);
    }

    public void onEvent(NumberTextViewOnTouch event) {
        int inType = event.view.getInputType();
        event.view.setInputType(InputType.TYPE_NULL);
        event.view.onTouchEvent(event.motionEvent);
        event.view.setInputType(inType);
    }

    public void clearAllSelectedItems(){
        ArrayList<CommodityToggledEvent> events = new ArrayList<>();

        for (BaseCommodityViewModel model : selectedCommodities) {
            events.add(new CommodityToggledEvent(model));
        }

        for(CommodityToggledEvent event : events){
            onEvent(event);
        }
    }
}
