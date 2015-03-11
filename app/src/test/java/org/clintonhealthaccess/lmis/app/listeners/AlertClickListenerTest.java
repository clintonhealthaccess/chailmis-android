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

package org.clintonhealthaccess.lmis.app.listeners;

import android.content.Intent;
import android.view.View;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.activities.MessagesActivity;
import org.clintonhealthaccess.lmis.app.activities.OrderActivity;
import org.clintonhealthaccess.lmis.app.adapters.AlertsAdapter;
import org.clintonhealthaccess.lmis.app.models.OrderType;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.models.alerts.LowStockAlert;
import org.clintonhealthaccess.lmis.app.services.AlertsService;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
public class AlertClickListenerTest {

    private UserService mockUserService;
    private MessagesActivity activity;
    private AlertsService alertsService;

    @Before
    public void setUp() throws Exception {
        User user = mock(User.class);
        mockUserService = mock(UserService.class);
        when(mockUserService.userRegistered()).thenReturn(true);
        when(mockUserService.getRegisteredUser()).thenReturn(
                new User("Tw Office", "pass","place", "Tw Kla Office"));
        when(mockUserService.userRegistered()).thenReturn(true);
        when(mockUserService.getRegisteredUser()).thenReturn(user);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
            }
        });
        activity = buildActivity(MessagesActivity.class).create().get();
        alertsService = mock(AlertsService.class);
    }

    @Test
    public void shouldOnlyStartIntentForEnabledLowStockAlerts() throws Exception {
        AlertClickListener listener = new AlertClickListener(setupAdapter(false), activity);
        listener.onItemClick(null, mock(View.class), 1, 1);
        Intent startedIntent = shadowOf(activity).getNextStartedActivity();
        assertThat(startedIntent, is(notNullValue()));
        assertThat(startedIntent.getComponent().getClassName(), equalTo(OrderActivity.class.getName()));
    }

    @Test
    public void shouldNotStartIntentForDisabledLowStockAlerts() throws Exception {
        AlertClickListener listener = new AlertClickListener(setupAdapter(true), activity);
        listener.onItemClick(null, mock(View.class), 1, 1);
        Intent startedIntent = shadowOf(activity).getNextStartedActivity();
        assertThat(startedIntent, is(nullValue()));
    }

    @Test
    public void shouldSetOrderTypeToEmergency() throws Exception {
        AlertClickListener listener = new AlertClickListener(setupAdapter(false), activity);
        listener.onItemClick(null, mock(View.class), 1, 1);
        Intent startedIntent = shadowOf(activity).getNextStartedActivity();
        assertThat(startedIntent, is(notNullValue()));
        assertThat(startedIntent.getStringExtra(AlertClickListener.ORDER_TYPE), is(OrderType.EMERGENCY));

    }


    private AlertsAdapter setupAdapter(boolean value) {
        AlertsAdapter alertsAdapter = mock(AlertsAdapter.class);
        LowStockAlert mockLowStockAlert = mock(LowStockAlert.class);
        when(mockLowStockAlert.isDisabled()).thenReturn(value);
        when(alertsAdapter.getItem(anyInt())).thenReturn(mockLowStockAlert);
        return alertsAdapter;
    }
}