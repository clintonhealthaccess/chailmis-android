package org.clintonhealthaccess.lmis.app.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.SelectedCommoditiesAdapter;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.fragments.DispenseConfirmationFragment;
import org.clintonhealthaccess.lmis.app.models.Dispensing;
import org.clintonhealthaccess.lmis.app.models.DispensingItem;
import org.clintonhealthaccess.lmis.app.services.DispensingService;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK;


public class DispenseActivity extends CommoditySelectableActivity {
    @InjectView(R.id.buttonSubmitDispense)
    Button buttonSubmitDispense;
    @InjectView(R.id.checkboxDispenseToFacility)
    CheckBox checkboxDispenseToFacility;
    @InjectView(R.id.textViewPrescriptionId)
    TextView textViewPrescriptionId;
    @InjectView(R.id.textViewPrescriptionText)
    TextView textViewPrescriptionText;
    @Inject
    DispensingService dispensingService;

    private final QuantityValidator INVALID_AMOUNT = new QuantityValidator(R.string.dispense_submit_validation_message_zero, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            try {
                return Integer.parseInt(editTextQuantity.getText().toString()) <= 0;
            } catch (NumberFormatException ex) {
                return false;
            }

        }
    });
    private final QuantityValidator EMPTY = new QuantityValidator(R.string.dispense_submit_validation_message_filled, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            return editTextQuantity.getText().toString().isEmpty();
        }
    });
    private final QuantityValidator HAS_ERROR = new QuantityValidator(R.string.dispense_submit_validation_message_errors, new Predicate<EditText>() {
        @Override
        public boolean apply(EditText editTextQuantity) {
            return editTextQuantity.getError() != null;
        }
    });

    private boolean hasInvalidField(List<QuantityValidator> validators) {
        for (final QuantityValidator validator : validators) {
            if (!validator.isValid()) {
                showToastMessage(validator.toastMessage());
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dispense;
    }

    @Override
    protected CommodityDisplayStrategy getCheckBoxVisibilityStrategy() {
        return DISALLOW_CLICK_WHEN_OUT_OF_STOCK;
    }

    protected ArrayAdapter getArrayAdapter() {
        return new SelectedCommoditiesAdapter(
                this, getSelectedCommoditiesAdapterId(), new ArrayList<CommodityViewModel>());
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        buttonSubmitDispense.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (hasInvalidField(of(INVALID_AMOUNT, EMPTY, HAS_ERROR))) {
                            return;
                        }

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        DispenseConfirmationFragment dialog = DispenseConfirmationFragment.newInstance(getDispensing());
                        dialog.show(fragmentManager, "confirmDispensing");
                    }
                }
        );

        checkboxDispenseToFacility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    textViewPrescriptionId.setVisibility(View.INVISIBLE);
                    textViewPrescriptionText.setVisibility(View.INVISIBLE);
                } else {
                    textViewPrescriptionId.setVisibility(View.VISIBLE);
                    textViewPrescriptionText.setVisibility(View.VISIBLE);
                }
            }
        });

        textViewPrescriptionId.setText(dispensingService.getNextPrescriptionId());
    }

    private int getSelectedCommoditiesAdapterId() {
        return R.layout.selected_commodity_list_item;
    }

    @Override
    protected void onCommoditySelectionChanged(List<CommodityViewModel> selectedCommodities) {
        if (selectedCommodities.size() > 0) {
            buttonSubmitDispense.setVisibility(View.VISIBLE);
        } else {
            buttonSubmitDispense.setVisibility(View.INVISIBLE);
        }
    }

    Dispensing getDispensing() {
        final Dispensing dispensing = new Dispensing();
        boolean dispenseToFacility = checkboxDispenseToFacility.isChecked();
        dispensing.setDispenseToFacility(dispenseToFacility);
        if (!dispenseToFacility) {
            dispensing.setPrescriptionId(textViewPrescriptionId.getText().toString());
        }
        onEachSelectedCommodity(new SelectedCommodityHandler() {
            @Override
            public void operate(View view, CommodityViewModel commodityViewModel) {
                EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                int quantity = parseInt(editTextQuantity.getText().toString());
                dispensing.getDispensingItems().add(new DispensingItem(commodityViewModel.getCommodity(), quantity));
            }
        });
        Log.e("DDnn", format(" dispensing items %d", dispensing.getDispensingItems().size()));
        return dispensing;
    }

    private class QuantityValidator {
        private final Predicate<EditText> predicate;
        private int toastMessageStringId;

        public QuantityValidator(int stringId, Predicate<EditText> predicate) {
            toastMessageStringId = stringId;
            this.predicate = predicate;
        }

        private boolean isValid() {
            return filter(wrap(gridViewSelectedCommodities), new Predicate<View>() {
                @Override
                public boolean apply(View view) {
                    EditText editTextQuantity = (EditText) view.findViewById(R.id.editTextQuantity);
                    return predicate.apply(editTextQuantity);
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
}
