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

import android.content.Intent;
import android.widget.Spinner;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.ReceiveCommodityViewModel;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.AllocationService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.fest.assertions.api.ANDROID;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.Arrays;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.setupActivity;


@RunWith(RobolectricGradleTestRunner.class)
public class ReceiveActivityTest {

    public static final String PANADOL = "Panadol";

    public static final String VALID_ALLOCATION_ID = "TW200";

    private UserService userService;

    private AllocationService mockAllocationService;

    private ReceiveActivity getReceiveActivity() {
        return setupActivity(ReceiveActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        mockAllocationService = mock(AllocationService.class);
        when(userService.userRegistered()).thenReturn(true);
        when(userService.getRegisteredUser()).thenReturn(
                new User("Tw Office", "pass", "place", "Tw Kla Office"));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
                bind(AllocationService.class).toInstance(mockAllocationService);
            }
        });
    }

    @Test
    public void testBuildActivity() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity, not(nullValue()));
    }

    @Test
    public void shouldHaveAKeyBoardView() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        MatcherAssert.assertThat(receiveActivity.keyBoardView, not(nullValue()));
    }

    @Test
    public void shouldHaveCorrect2LetterCodeInFacilityName() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertNotNull(receiveActivity.facility2LetterCode);
        assertEquals(receiveActivity.facility2LetterCode, "TW");
    }

    @Test
    public void shouldShowAllocationIdAutoCompleteTextView() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity.textViewAllocationId, not(nullValue()));
    }

    @Test
    public void availableAllocationIdsShouldBeSelectableFromTheTextViewForAllocationId() throws Exception {
        String item1 = "TW0001";
        String item2 = "TW0002";
        when(mockAllocationService.getYetToBeReceivedAllocationIds()).thenReturn(Arrays.asList(item1, item2));
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity.textViewAllocationId.getAdapter().getCount(), is(2));
        assertThat(receiveActivity.textViewAllocationId.getAdapter().getItem(0).toString(), is(item1));
    }

    @Test
    public void shouldShowAnErrorMessageForAllocationIdThatHasAlreadyBeenReceived() throws Exception {
        String item1 = "TW0001";
        String item2 = "TW0002";
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>(Arrays.asList(item1, item2)));
        ReceiveActivity receiveActivity = getReceiveActivity();
        setSource(receiveActivity, application.getString(R.string.zonal_store_for_receive));
        assertThat(receiveActivity.textViewAllocationId.getError(), is(nullValue()));

        receiveActivity.textViewAllocationId.setText(item1);
        receiveActivity.validateAllocationId();
        assertThat(receiveActivity.textViewAllocationId.getError().toString(), is(application.getString(R.string.error_allocation_received)));

        receiveActivity.validateAllocationId();
        receiveActivity.textViewAllocationId.setText("TW12032");
        assertThat(receiveActivity.textViewAllocationId.getError(), is(nullValue()));
    }

    @Test
    public void shouldNotLetUserSubmitFormWhenAllocationIdHasError() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        setSource(receiveActivity, application.getString(R.string.lga_for_receive));
        receiveActivity.textViewAllocationId.setText("aoiiouads");
        receiveActivity.buttonSubmitReceive.performClick();

        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(application.getString(R.string.receive_submit_validation_message_allocation_id)));
    }

    @Test
    public void shouldLetUserSubmitFormWhenAllocationIdAndQuantitiesAreValid() throws Exception {
        performSubmitWithValidFields();
        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), is(nullValue()));
    }

    @Test
    public void shouldShowAnErrorWhenTheAllocationIdIsOfWrongFormat() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        setSource(receiveActivity, application.getString(R.string.lga_for_receive));
        receiveActivity.textViewAllocationId.findFocus();
        receiveActivity.textViewAllocationId.setText("aoiiouads");
        receiveActivity.textViewAllocationId.clearFocus();
        assertThat(receiveActivity.textViewAllocationId.getError().toString(),
                is(String.format(application.getString(R.string.error_allocation_id_wrong_format),
                        receiveActivity.getAllocationIdFormat()))
        );
        receiveActivity.textViewAllocationId.setText("TW12032");
        assertThat(receiveActivity.textViewAllocationId.getError(), is(nullValue()));
    }

    @Test
    public void shouldPresetTheQuantityForSelectedItemIfAllocationIdIsSet() throws Exception {
        String item1 = "TW0001";
        String item2 = "TW0002";
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<>(Arrays.asList(item1, item2)));
        Allocation allocation = mock(Allocation.class);
        AllocationItem item = new AllocationItem();
        item.setCommodity(new Commodity("food"));
        item.setQuantity(10);
        item.setAllocation(allocation);
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>());
        when(allocation.getAllocationItems()).thenReturn(new ArrayList<>(Arrays.asList(item)));
        when(allocation.isReceived()).thenReturn(false);
        when(mockAllocationService.getAllocationByLmisId(anyString())).thenReturn(allocation);
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.setAllocation(item2);
        receiveActivity.textViewAllocationId.setText("TW0002");
        assertThat(receiveActivity.textViewAllocationId.getError(), is(nullValue()));
        assertThat(receiveActivity.arrayAdapter.getCount(), is(1));
    }

    @Test
    public void shouldToastInvalidFieldMessageWhenFieldsAreInvalid() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setText(VALID_ALLOCATION_ID);
        fireCommodityToggledEvent(receiveActivity);
        receiveActivity.getSubmitButton().performClick();
        assertThat(ShadowToast.getTextOfLatestToast(), is(receiveActivity.getResources().getString(R.string.receive_quantities_validation_error_message)));
    }

    @Test
    public void shouldOpenConfirmReceiveDialogWhenSubmitButtonClickedGivenValidFields() throws Exception {
        performSubmitWithValidFields();
        assertThat(ShadowToast.getLatestToast(), is(nullValue()));
        assertThat(ShadowDialog.getLatestDialog(), is(notNullValue()));
    }

    @Test
    public void shouldGenerateAReceiveFromSelectedReceiveCommodities() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity(PANADOL), 3, 6);
        EventBus.getDefault().post(new CommodityToggledEvent(viewModel));
        Receive receive = receiveActivity.generateReceive();

        assertThat(receive.getReceiveItems().size(), is(1));
        assertThat(receive.getReceiveItems().get(0).getCommodity().getName(), is(PANADOL));
        assertThat(receive.getSource(), is(application.getString(R.string.others)));
    }

    @Test
    public void shouldGenerateReceiveWithSource() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity(PANADOL), 3, 6);
        EventBus.getDefault().post(new CommodityToggledEvent(viewModel));
        String applicationString = application.getString(R.string.zonal_store_for_receive);
        setSource(receiveActivity, applicationString);
        Receive receive = receiveActivity.generateReceive();
        assertThat(receive.getSource(), is(application.getString(R.string.zonal_store)));
    }

    @Test
    public void shouldDisableAndHideAllocationIfNotRecievingFromLGA() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        setSource(receiveActivity, application.getString(R.string.zonal_store_for_receive));
        ANDROID.assertThat(receiveActivity.textViewAllocationId).isDisabled();
        ANDROID.assertThat(receiveActivity.textViewAllocationId).isInvisible();
        ANDROID.assertThat(receiveActivity.textViewAllocationLabel).isInvisible();
        assertThat(receiveActivity.textViewAllocationId.getError(), nullValue());
    }

    @Test
    public void shouldRemoveErrorOnAllocationIdIfNotRecievingFromLGA() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setError("Pre-set error message");
        setSource(receiveActivity, application.getString(R.string.zonal_store_for_receive));

        assertThat(receiveActivity.textViewAllocationId.getError(), nullValue());
    }

    @Test
    public void shouldEnableAllocationIfReceivingFromLGA() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setText("INVALIDALLOCATIONID");
        setSource(receiveActivity, application.getString(R.string.zonal_store_for_receive));
        assertThat(receiveActivity.textViewAllocationId.getError(), nullValue());
        ANDROID.assertThat(receiveActivity.textViewAllocationId).isDisabled();

        receiveActivity.setAllocation("INVALIDALLOCATIONID");

        setSource(receiveActivity, application.getString(R.string.lga_for_receive));
        ANDROID.assertThat(receiveActivity.textViewAllocationId).isEnabled();
        ANDROID.assertThat(receiveActivity.textViewAllocationId).isVisible();
        ANDROID.assertThat(receiveActivity.textViewAllocationLabel).isVisible();
        receiveActivity.validateAllocationId();
        CharSequence error = receiveActivity.textViewAllocationId.getError();
        assertThat(error, notNullValue());
        assertThat(error.toString(), is(String.format(
                application.getString(R.string.error_allocation_id_wrong_format),
                receiveActivity.getAllocationIdFormat())));
    }

    private void setSource(ReceiveActivity receiveActivity, String string) {
        int position2 = receiveActivity.getReceiveSources().indexOf(string);
        receiveActivity.spinnerSource.setSelection(position2);
    }

    @Test
    public void shouldNotRequireAllocationIdWhenNotRecievingFromLGA() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        setSource(receiveActivity, application.getString(R.string.zonal_store_for_receive));
        ANDROID.assertThat(receiveActivity.textViewAllocationId).isDisabled();
        setupValidCommodity(receiveActivity);
        receiveActivity.getSubmitButton().performClick();
        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), is(nullValue()));
    }

    @Test
    public void shouldClearAllocationWhenNotReceivingFromLGA() throws Exception {
        String item1 = VALID_ALLOCATION_ID;
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>(Arrays.asList(item1)));
        Allocation allocation = mock(Allocation.class);
        when(allocation.getAllocationId()).thenReturn(VALID_ALLOCATION_ID);
        AllocationItem item = new AllocationItem();
        item.setCommodity(new Commodity("food"));
        item.setQuantity(10);
        item.setAllocation(allocation);
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>());
        when(allocation.getAllocationItems()).thenReturn(new ArrayList<AllocationItem>(Arrays.asList(item)));
        when(allocation.isReceived()).thenReturn(false);
        when(mockAllocationService.getAllocationByLmisId(anyString())).thenReturn(allocation);

        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setText(VALID_ALLOCATION_ID);
        receiveActivity.setAllocation(VALID_ALLOCATION_ID);
        assertThat(receiveActivity.allocation, is(notNullValue()));
        assertThat(receiveActivity.allocation.getAllocationId(), is(VALID_ALLOCATION_ID));
        receiveActivity.spinnerSource.setSelection(1);
        assertThat(receiveActivity.allocation, is(nullValue()));
    }

    @Test
    public void shouldSetAllocationInReceiveActivity() throws Exception {
        String item1 = VALID_ALLOCATION_ID;
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>(Arrays.asList(item1)));
        Allocation allocation = mock(Allocation.class);
        when(allocation.getAllocationId()).thenReturn(VALID_ALLOCATION_ID);
        AllocationItem item = new AllocationItem();
        item.setCommodity(new Commodity("food"));
        item.setQuantity(10);
        item.setAllocation(allocation);
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>());
        when(allocation.getAllocationItems()).thenReturn(new ArrayList<AllocationItem>(Arrays.asList(item)));
        when(allocation.isReceived()).thenReturn(false);
        when(mockAllocationService.getAllocationByLmisId(anyString())).thenReturn(allocation);

        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setText(VALID_ALLOCATION_ID);
        receiveActivity.setAllocation(VALID_ALLOCATION_ID);

        assertThat(receiveActivity.allocation, is(notNullValue()));
        assertThat(receiveActivity.allocation.getAllocationId(), is(VALID_ALLOCATION_ID));
    }

    @Test
    public void shouldPopulateAllocationInReceive() throws Exception {
        String item1 = VALID_ALLOCATION_ID;
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>(Arrays.asList(item1)));
        Allocation allocation = mock(Allocation.class);
        AllocationItem item = new AllocationItem();
        item.setCommodity(new Commodity("food"));
        item.setQuantity(10);
        item.setAllocation(allocation);
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>());
        when(allocation.getAllocationItems()).thenReturn(new ArrayList<AllocationItem>(Arrays.asList(item)));
        when(allocation.isReceived()).thenReturn(false);
        when(mockAllocationService.getAllocationByLmisId(anyString())).thenReturn(allocation);

        ReceiveActivity receiveActivity = getReceiveActivity();
        setSource(receiveActivity, application.getString(R.string.lga_for_receive));
        receiveActivity.textViewAllocationId.setText(VALID_ALLOCATION_ID);
        receiveActivity.setAllocation(VALID_ALLOCATION_ID);

        Receive receive = receiveActivity.generateReceive();
        assertThat(receive.getAllocation().getAllocationId(), is(allocation.getAllocationId()));
    }

    @Test
    public void shouldPrepopulateWithSetAllocationIdIfAvailable() throws Exception {
        String item1 = VALID_ALLOCATION_ID;
        Allocation allocation = mock(Allocation.class);
        AllocationItem item = new AllocationItem();
        item.setCommodity(new Commodity("food"));
        item.setQuantity(10);
        item.setAllocation(allocation);
        when(allocation.getAllocationId()).thenReturn(VALID_ALLOCATION_ID);
        when(mockAllocationService.getReceivedAllocationIds()).thenReturn(new ArrayList<String>());
        when(allocation.getAllocationItems()).thenReturn(new ArrayList<AllocationItem>(Arrays.asList(item)));
        when(allocation.isReceived()).thenReturn(false);
        when(mockAllocationService.getAllocationByLmisId(anyString())).thenReturn(allocation);
        Intent intent = new Intent();
        intent.putExtra(ReceiveActivity.ALLOCATION_ID, item1);
        ReceiveActivity receiveActivity = Robolectric.buildActivity(ReceiveActivity.class).withIntent(intent).create().start().resume().visible().get();
        setSource(receiveActivity, application.getString(R.string.lga_for_receive));
        assertThat(receiveActivity.allocation, is(notNullValue()));
        assertThat(receiveActivity.allocation.getAllocationId(), is(VALID_ALLOCATION_ID));
    }

    @Test
    public void shouldHaveASpinnerForSourceOfCommodities() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity.spinnerSource, not(nullValue()));
    }

    @Test
    public void shouldHaveOptionsInSourceSpinner() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        assertThat(receiveActivity.spinnerSource.getAdapter().getCount(), is(greaterThan(1)));
    }

    @Test
    public void shouldNotAllowYouToSelectLGACommoditiesWhenZonalStoreIsSelected() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.spinnerSource = mock(Spinner.class);
        when(receiveActivity.spinnerSource.getSelectedItem()).thenReturn(application.getString(R.string.zonal_store_for_receive));
        Commodity commodity = mock(Commodity.class);
        when(commodity.isNonLGA()).thenReturn(false);
        assertFalse(receiveActivity.getCheckBoxVisibilityStrategy().allowClick(new BaseCommodityViewModel(commodity)));
    }

    @Test
    public void shouldAllowYouToSelectNonLGACommoditiesWhenZonalStoreIsSelected() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.spinnerSource = mock(Spinner.class);
        when(receiveActivity.spinnerSource.getSelectedItem()).thenReturn(application.getString(R.string.zonal_store_for_receive));
        Commodity commodity = mock(Commodity.class);
        when(commodity.isNonLGA()).thenReturn(true);
        assertTrue(receiveActivity.getCheckBoxVisibilityStrategy().allowClick(new BaseCommodityViewModel(commodity)));
    }

    @Test
    public void shouldAllowYouToSelectLGANonVaccinesCommoditiesWhenLGAIsSelected() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.spinnerSource = mock(Spinner.class);
        when(receiveActivity.spinnerSource.getSelectedItem()).thenReturn(application.getString(R.string.lga_for_receive));
        Commodity commodity = mock(Commodity.class);
        when(commodity.getCategory()).thenReturn(new Category("NonVaccines"));
        receiveActivity.allocation = mock(Allocation.class);
        when(commodity.isNonLGA()).thenReturn(false);
        assertTrue(receiveActivity.getCheckBoxVisibilityStrategy().allowClick(new BaseCommodityViewModel(commodity)));
    }

    @Test
    public void shouldNotAllowYouToSelectVaccinesCommoditiesWhenLGASelected() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.spinnerSource = mock(Spinner.class);
        when(receiveActivity.spinnerSource.getSelectedItem()).thenReturn(application.getString(R.string.lga_for_receive));
        Commodity commodity = mock(Commodity.class);
        when(commodity.getCategory()).thenReturn(new Category("Vaccines"));
        assertFalse(receiveActivity.getCheckBoxVisibilityStrategy().allowClick(new BaseCommodityViewModel(commodity)));
    }

    @Test
    public void shouldNotAllowYouToSelectVaccinesCommoditiesWhenOthersSourcesSelected() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.spinnerSource = mock(Spinner.class);
        when(receiveActivity.spinnerSource.getSelectedItem()).thenReturn(application.getString(R.string.others));
        Commodity commodity = mock(Commodity.class);
        when(commodity.getCategory()).thenReturn(new Category("Vaccines"));
        assertFalse(receiveActivity.getCheckBoxVisibilityStrategy().allowClick(new BaseCommodityViewModel(commodity)));
    }

    @Test
    public void shouldNotAllowYouToSelectNonLGANonVaccinesCommoditiesWhenLGAIsSelected() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.spinnerSource = mock(Spinner.class);
        when(receiveActivity.spinnerSource.getSelectedItem()).thenReturn(application.getString(R.string.lga));
        Commodity commodity = mock(Commodity.class);
        when(commodity.isNonLGA()).thenReturn(true);
        when(commodity.getCategory()).thenReturn(new Category("NonVaccines"));
        assertFalse(receiveActivity.getCheckBoxVisibilityStrategy().allowClick(new BaseCommodityViewModel(commodity)));

    }

    @Test
    public void shouldClearAllLGACommoditiesWhenZonalSourceIsSelected() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        addCommodity(false, PANADOL);
        addCommodity(false, "sugar");
        addCommodity(true, "sugar12");
        assertThat(receiveActivity.selectedCommodities.size(), is(3));
        receiveActivity.spinnerSource.setSelection(1);
        assertThat(receiveActivity.spinnerSource.getSelectedItem().toString(), is(application.getString(R.string.zonal_store_for_receive)));
        assertThat(receiveActivity.selectedCommodities.size(), is(1));
    }

    @Test
    public void shouldClearAllNonLGACommoditiesWhenZonalSourceIsNotSelected() throws Exception {
        ReceiveActivity receiveActivity = getReceiveActivity();
        addCommodity(false, PANADOL);
        addCommodity(false, "sugar");
        addCommodity(true, "sugar12");
        assertThat(receiveActivity.selectedCommodities.size(), is(3));
        receiveActivity.spinnerSource.setSelection(0);
        assertThat(receiveActivity.spinnerSource.getSelectedItem().toString(), is(application.getString(R.string.others_for_receive)));
        assertThat(receiveActivity.selectedCommodities.size(), is(2));

    }

    private void addCommodity(boolean nonLGA, String name) {
        Commodity commodity = new Commodity(name);
        commodity.setNonLGA(nonLGA);
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(commodity);
        viewModel.setQuantityReceived(2);
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(viewModel);
        EventBus.getDefault().post(commodityToggledEvent);
    }

    private void performSubmitWithValidFields() {
        ReceiveActivity receiveActivity = getReceiveActivity();
        receiveActivity.textViewAllocationId.setText(VALID_ALLOCATION_ID);
        setupValidCommodity(receiveActivity);
        receiveActivity.getSubmitButton().performClick();
    }

    private void setupValidCommodity(ReceiveActivity receiveActivity) {
        ReceiveCommodityViewModel viewModel = new ReceiveCommodityViewModel(new Commodity(PANADOL));
        viewModel.setQuantityReceived(2);
        fireCommodityToggledEvent(receiveActivity, viewModel);
    }

    private CommodityToggledEventDetails fireCommodityToggledEvent(ReceiveActivity activity) {
        ReceiveCommodityViewModel commodityViewModel = new ReceiveCommodityViewModel(new Commodity("name"));
        return fireCommodityToggledEvent(activity, commodityViewModel);
    }

    private CommodityToggledEventDetails fireCommodityToggledEvent(ReceiveActivity activity, ReceiveCommodityViewModel commodityViewModel) {
        CommodityToggledEvent commodityToggledEvent = new CommodityToggledEvent(commodityViewModel);
        EventBus.getDefault().post(commodityToggledEvent);
        return new CommodityToggledEventDetails(activity, commodityToggledEvent);
    }


    private class CommodityToggledEventDetails {
        private ReceiveActivity activity;
        public CommodityToggledEvent commodityToggledEvent;

        public CommodityToggledEventDetails(ReceiveActivity activity, CommodityToggledEvent commodityToggledEvent) {
            this.activity = activity;
            this.commodityToggledEvent = commodityToggledEvent;
        }

        public BaseCommodityViewModel commodityViewModel() {
            return this.commodityToggledEvent.getCommodity();
        }
    }
}