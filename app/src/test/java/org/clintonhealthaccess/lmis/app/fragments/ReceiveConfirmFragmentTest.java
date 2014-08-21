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

package org.clintonhealthaccess.lmis.app.fragments;

import android.app.Dialog;
import android.widget.Button;
import android.widget.Toast;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Receive;
import org.clintonhealthaccess.lmis.app.services.ReceiveService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;


import static junit.framework.Assert.assertFalse;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(RobolectricGradleTestRunner.class)
public class ReceiveConfirmFragmentTest {

    private ReceiveService mockReceiveService;

    @Before
    public void setUp() throws Exception {
        mockReceiveService = mock(ReceiveService.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ReceiveService.class).toInstance(mockReceiveService);
            }
        });

        ReceiveConfirmFragment receiveConfirmFragment = ReceiveConfirmFragment.newInstance(new Receive());
        startFragment(receiveConfirmFragment);
    }

    @Test
    public void shouldCloseFragmentWhenBackButtonIsClicked() {
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        Button closeButton = (Button) dialog.findViewById(R.id.button_receive_go_back);
        closeButton.performClick();
        assertFalse(dialog.isShowing());
    }

    @Test
    public void shouldSaveReceiveWhenConfirmButtonIsClicked() {
        Dialog dialog = ShadowDialog.getLatestDialog();
        Button confirmButton = (Button) dialog.findViewById(R.id.button_receive_confirm);
        confirmButton.performClick();

        verify(mockReceiveService).saveReceive(Matchers.<Receive> anyObject());
        assertThat(ShadowToast.getTextOfLatestToast(), is(application.getResources().getString(R.string.receive_successful)));
        assertFalse(dialog.isShowing());
    }
}