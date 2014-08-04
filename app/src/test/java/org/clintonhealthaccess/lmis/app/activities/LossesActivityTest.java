package org.clintonhealthaccess.lmis.app.activities;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.activities.viewmodels.LossesCommodityViewModel;
import org.clintonhealthaccess.lmis.app.adapters.strategies.CommodityDisplayStrategy;
import org.clintonhealthaccess.lmis.app.events.CommodityToggledEvent;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import de.greenrobot.event.EventBus;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.setupActivity;

@RunWith(RobolectricGradleTestRunner.class)
public class LossesActivityTest {

    private UserService userService;
    private LossesActivity lossesActivity;

    @Before
    public void setUp() throws Exception {
        lossesActivity = setupActivity(LossesActivity.class);
        userService = mock(UserService.class);
        when(userService.getRegisteredUser()).thenReturn(new User("", "", "place"));
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });

    }

    @Test
    public void testBuildActivity() throws Exception {
        assertThat(lossesActivity, not(nullValue()));
    }

    @Test
    public void shouldNotAllowSelectionOfOutOfStockCommoditiesOnOverlay() {
        assertThat(lossesActivity.getCheckBoxVisibilityStrategy(), is(CommodityDisplayStrategy.DISALLOW_CLICK_WHEN_OUT_OF_STOCK));
    }


    @Test
    public void shouldOpenConfirmDialogIfLossesAreValid() throws Exception {
        Commodity mockCommodity = mock(Commodity.class);
        when(mockCommodity.getStockOnHand()).thenReturn(10);
        LossesCommodityViewModel lossesCommodityViewModel = new LossesCommodityViewModel(mockCommodity);
        lossesCommodityViewModel.setMissing(1);

        EventBus.getDefault().post(new CommodityToggledEvent(lossesCommodityViewModel));
        Button submitButton = lossesActivity.getSubmitButton();
        submitButton.performClick();
        assertThat(submitButton.getVisibility(), is(View.VISIBLE));
        Dialog latestDialog = ShadowDialog.getLatestDialog();
        assertThat(latestDialog, is(notNullValue()));
    }

    @Test
    public void shouldNotOpenConfirmDialogIfLossesAreInvalid() {
        LossesCommodityViewModel lossesCommodityViewModel = new LossesCommodityViewModel(new Commodity("Panado"));
        EventBus.getDefault().post(new CommodityToggledEvent(lossesCommodityViewModel));
        Button submitButton = lossesActivity.getSubmitButton();
        submitButton.performClick();
        assertThat(ShadowToast.getTextOfLatestToast(), is(application.getResources().getString(R.string.fillInSomeLosses)));
        assertThat(ShadowDialog.getLatestDialog(), is(nullValue()));
    }

}
