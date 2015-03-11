/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.CommodityActionValue;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.LmisServer;
import org.clintonhealthaccess.lmis.utils.LMISTestCase;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestFixture.defaultCategories;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjectionWithMockLmisServer;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.testActionValues;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class DataSetServiceTest extends LMISTestCase {

    @Inject
    private DataSetService dataSetService;

    private List<CommodityActionValue> mockStockLevels;
    private LmisServer mockLmisServer;
    @Inject
    private CommodityService commodityService;

    @Before
    public void setUp() throws Exception {
        mockLmisServer = mock(LmisServer.class);
        mockStockLevels = testActionValues(application);
        when(mockLmisServer.fetchCategories((User) anyObject())).thenReturn(defaultCategories(application));
        when(mockLmisServer.fetchCommodityActionValues((User) anyObject())).thenReturn(mockStockLevels);

        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LmisServer.class).toInstance(mockLmisServer);
            }
        });

        commodityService.initialise(new User("test", "pass"));
    }

    @Ignore
    @Test
    public void shouldAll() throws Exception {
        List<DataSet> dataSets = dataSetService.all();
        assertThat(dataSets, is(notNullValue()));
        assertThat(dataSets.size(), is(3));
    }

    @Test
    public void shouldGetByName() throws Exception {
        DataSet dataSet = dataSetService.getByName("lmis dataset Daily");
        assertThat(dataSet, is(notNullValue()));
        assertThat(dataSet.getId(), is("lmis_dataset_daily"));
    }
}