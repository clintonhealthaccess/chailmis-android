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

package org.clintonhealthaccess.lmis.app.watchers;

import android.text.TextWatcher;
import android.widget.EditText;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.activities.viewmodels.BaseCommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import static android.text.Editable.Factory;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class QuantityTextWatcherTest {

    StockService mockStockService;
    private TextWatcher watcher;
    private EditText editText;

    @Before
    public void setUp() throws Exception {
        mockStockService = mock(StockService.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockService.class).toInstance(mockStockService);
            }
        });
        editText = spy(new EditText(application));
        watcher = new QuantityTextWatcher(editText, new BaseCommodityViewModel(new Commodity("")));
    }

    @Test
    public void shouldShowErrorIfQuantityIsGreaterThanStockAvailable() throws Exception {
        when(mockStockService.getStockLevelFor(Matchers.<Commodity>anyObject())).thenReturn(2);
        watcher.afterTextChanged(new Factory().newEditable("12"));
        verify(editText).setError(anyString());

    }

    @Test
    public void shouldShowErrorWithRemainingStockIfQuantityIsGreaterThanStockAvailable() throws Exception {
        when(mockStockService.getStockLevelFor(Matchers.<Commodity>anyObject())).thenReturn(2);
        watcher.afterTextChanged(new Factory().newEditable("12"));
        assertThat(editText.getError().toString(), containsString("(2)"));

    }

    @Test
    public void shouldNotShowErrorIfQuantityIsLessThanStockAvailable() throws Exception {
        when(mockStockService.getStockLevelFor(Matchers.<Commodity>anyObject())).thenReturn(77);
        watcher.afterTextChanged(new Factory().newEditable("4"));
        verify(editText, never()).setError(anyString());
    }

    @Test
    public void shouldNotShowErrorIfQuantityIsEqualToStockAvailable() throws Exception {
        when(mockStockService.getStockLevelFor(Matchers.<Commodity>anyObject())).thenReturn(4);
        watcher.afterTextChanged(new Factory().newEditable("4"));
        verify(editText, never()).setError(anyString());
    }


}