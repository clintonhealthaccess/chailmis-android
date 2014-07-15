package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.app.sync.SyncManager;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class HomeActivityTest {
    private UserService mockUserService;

    @Before
    public void setUp() {
        mockUserService = mock(UserService.class);
        final SyncManager mockSyncManager = mock(SyncManager.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
                bind(SyncManager.class).toInstance(mockSyncManager);
            }
        });
    }

    private HomeActivity getHomeActivity() {
        return buildActivity(HomeActivity.class).create().get();
    }

    private void setRegistrationStatus(boolean registered) {
        when(mockUserService.userRegistered()).thenReturn(registered);
    }

    @Test
    public void testBuildActivity() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity, not(nullValue()));
    }

    @Test
    public void testDispenseButtonIsConnectedToView() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity.buttonDispense, not(nullValue()));
    }

    @Test
    public void testReceiveButtonIsConnectedToView() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity.buttonReceive, not(nullValue()));
    }

    @Test
    public void testLossesButtonIsConnectedToView() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity.buttonLosses, not(nullValue()));
    }

    @Test
    public void testOrderButtonIsConnectedToView() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity.buttonOrder, not(nullValue()));
    }

    @Test
    public void testReportsButtonIsConnectedToView() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity.buttonReports, not(nullValue()));
    }

    @Test
    public void testMessagesButtonIsConnectedToView() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity.buttonMessages, not(nullValue()));
    }

    @Test
    public void testTextViewFacilityNameIsConnectedToView() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity.textFacilityName, not(nullValue()));
    }

    @Test
    public void testClickDispenseButtonNavigatesToDispenseActivity() {

        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonDispense).callOnClick();

        Intent intent = new Intent(homeActivity, DispenseActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public  void testClickReceiveButtonNavigatesToReceiveActivity(){
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonReceive).callOnClick();

        Intent intent = new Intent(homeActivity, ReceiveActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public  void testClickOrderButtonNavigatesToReceiveActivity(){
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonOrder).callOnClick();

        Intent intent = new Intent(homeActivity, OrderActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public  void testClickLossesButtonNavigatesToReceiveActivity(){
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonLosses).callOnClick();

        Intent intent = new Intent(homeActivity, LossesActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public  void testClickReportsButtonNavigatesToReceiveActivity(){
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonReports).callOnClick();

        Intent intent = new Intent(homeActivity, ReportsActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public  void testClickMessagesButtonNavigatesToReceiveActivity(){
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonMessages).callOnClick();

        Intent intent = new Intent(homeActivity, MessagesActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public void testShouldRenderRegisterActivityIfThereIsNoUserRegistered() throws Exception {

        setRegistrationStatus(false);

        HomeActivity homeActivity = getHomeActivity();

        Intent registerIntent = new Intent(homeActivity, RegisterActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(registerIntent));
    }
}
