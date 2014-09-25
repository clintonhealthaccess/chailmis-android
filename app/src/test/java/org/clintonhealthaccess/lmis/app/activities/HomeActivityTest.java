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
import android.os.Bundle;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
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
        when(mockUserService.getRegisteredUser()).thenReturn(new User("", "", "place"));
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
    public void testListViewNotificationsExists() throws Exception {
        HomeActivity homeActivity = getHomeActivity();
        assertThat(homeActivity.listViewNotifications, not(nullValue()));
    }

    @Test
    public void testClickDispenseButtonNavigatesToDispenseActivity() {

        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonDispense).performClick();

        Intent intent = new Intent(homeActivity, DispenseActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public void testClickReceiveButtonNavigatesToReceiveActivity() {
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonReceive).performClick();

        Intent intent = new Intent(homeActivity, ReceiveActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public void testClickOrderButtonNavigatesToReceiveActivity() {
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonOrder).performClick();

        Intent intent = new Intent(homeActivity, OrderActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public void testClickLossesButtonNavigatesToReceiveActivity() {
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonLosses).performClick();

        Intent intent = new Intent(homeActivity, LossesActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public void testClickReportsButtonNavigatesToReceiveActivity() {
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonReports).performClick();

        Intent intent = new Intent(homeActivity, ReportsActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public void testClickMessagesButtonNavigatesToReceiveActivity() {
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonMessages).performClick();

        Intent intent = new Intent(homeActivity, MessagesActivity.class);

        assertThat(shadowOf(homeActivity).getNextStartedActivity(), equalTo(intent));
    }

    @Test
    public void testClickAdjustmentsButtonNavigatesToDispenseActivity() {
        setRegistrationStatus(true);

        HomeActivity homeActivity = getHomeActivity();

        homeActivity.findViewById(R.id.buttonAdjustments).performClick();

        Intent intent = new Intent(homeActivity, DispenseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(HomeActivity.IS_ADJUSTMENT, true);
        intent.putExtras(bundle);
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
