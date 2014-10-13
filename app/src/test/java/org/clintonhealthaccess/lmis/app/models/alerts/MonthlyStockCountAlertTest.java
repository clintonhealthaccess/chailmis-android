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

package org.clintonhealthaccess.lmis.app.models.alerts;

import android.content.Intent;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.activities.AdjustmentsActivity;
import org.clintonhealthaccess.lmis.app.activities.MessagesActivity;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.services.UserService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Calendar;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;


@RunWith(RobolectricGradleTestRunner.class)
public class MonthlyStockCountAlertTest {
    private UserService mockUserService;

    @Before
    public void setUp() throws Exception {
        User user = mock(User.class);
        mockUserService = mock(UserService.class);
        when(mockUserService.userRegistered()).thenReturn(true);
        when(mockUserService.getRegisteredUser()).thenReturn(user);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(mockUserService);
            }
        });
    }

    @Test
    public void shouldNavigateToAdjusmentPageOnClick() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JULY, 27);
        MonthlyStockCountAlert alert = new MonthlyStockCountAlert(calendar.getTime());
        alert.onClick(Robolectric.application);
        MessagesActivity activity = buildActivity(MessagesActivity.class).create().get();
        Intent startedIntent = shadowOf(activity).getNextStartedActivity();
        Assert.assertThat(startedIntent, is(notNullValue()));
        assertThat(AdjustmentsActivity.class.getName(), is(startedIntent.getComponent().getClassName()));
    }

    @Test
    public void shouldHaveCorrectMessage() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2025, Calendar.JULY, 27);
        MonthlyStockCountAlert alert = new MonthlyStockCountAlert(calendar.getTime());

        assertThat(alert.getMessage(), containsString("July"));

        assertThat(alert.getMessage(), containsString("Monthly Stock Count for July"));

    }
}