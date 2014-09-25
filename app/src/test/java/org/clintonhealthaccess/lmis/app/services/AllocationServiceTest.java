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

package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.app.models.AllocationItem;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.app.remote.Dhis2;
import org.clintonhealthaccess.lmis.utils.LMISTestCase;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.io.IOException;
import java.util.List;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(RobolectricGradleTestRunner.class)
public class AllocationServiceTest extends LMISTestCase {
    @Inject
    private Dhis2 dhis2;

    @Inject
    private CommodityService commodityService;

    @Inject
    private CategoryService categoryService;

    @Inject
    private AllocationService allocationService;
    private GenericDao<Allocation> allocationDao;

    @Before
    public void setUp() throws Exception {
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        allocationDao = new GenericDao<Allocation>(Allocation.class, Robolectric.application);

    }

    @Test
    public void getReceivedAllocationIdsShouldGetIdsOfReceivedAllocations() throws Exception {
        Allocation firstAllocation = new Allocation("UG-2013", "20140901");
        firstAllocation.setReceived(false);
        Allocation secondAllocation = new Allocation("UG-2012", "20140901");
        secondAllocation.setReceived(true);
        allocationDao.create(firstAllocation);
        allocationDao.create(secondAllocation);
        assertThat(allocationService.getReceivedAllocationIds(), contains("UG-2012"));
    }

    @Test
    public void getYetToBeReceivedAllocationIdsShouldOnlyListIdsOfYetToBeReceivedAllocations() throws Exception {
        Allocation firstAllocation = new Allocation("UG-2013", "20140901");
        firstAllocation.setReceived(false);
        Allocation secondAllocation = new Allocation("UG-2012", "20140901");
        secondAllocation.setReceived(true);
        allocationDao.create(firstAllocation);
        allocationDao.create(secondAllocation);
        assertThat(allocationService.getYetToBeReceivedAllocationIds(), contains("UG-2013"));
    }

    @Test
    public void shouldGetAllocationByLmisId() throws Exception {
        String lmisId = "UG-2013";
        Allocation firstAllocation = new Allocation(lmisId, "20140901");
        firstAllocation.setAllocationId(lmisId);
        firstAllocation.setReceived(false);
        allocationDao.create(firstAllocation);
        assertThat(allocationService.getAllocationByLmisId(lmisId).getAllocationId(), is(lmisId));
    }

    @Test
    public void shouldSyncAndSaveAllocations() throws Exception {
        // given
        User user = setupForAllocations();

        setUpSuccessHttpGetRequest(200, "allocations.json");

        assertThat(allocationDao.countOf(), is(0l));

        // when
        allocationService.syncAllocations(user);

        // then
        assertThat(allocationDao.countOf(), is(2l));
        List<Allocation> allocations = allocationDao.queryForAll();
        Allocation firstAllocation = allocations.get(0);
        assertThat(firstAllocation.getAllocationId(), is("12345"));

        List<AllocationItem> allocationItems = firstAllocation.getAllocationItems();
        assertThat(allocationItems.size(), is(1));
        assertThat(allocationItems.get(0).getCommodity().getName(), is("Albendazole_200mg tablet "));
        assertThat(allocationItems.get(0).getQuantity(), is(10));
    }


    @Test
    public void shouldnotSyncAndSaveDuplicateAllocations() throws Exception {
        // given
        User user = setupForAllocations();

        assertThat(allocationDao.countOf(), is(0l));

        // when
        setUpSuccessHttpGetRequest(200, "allocations.json");
        allocationService.syncAllocations(user);

        // then
        assertThat(allocationDao.countOf(), is(2l));

        setUpSuccessHttpGetRequest(200, "allocations.json");
        allocationService.syncAllocations(user);

        assertThat(allocationDao.countOf(), is(2l));

    }

    private User setupForAllocations() throws IOException {
        // FIXME: can we mock all this?
        String orgUnit = "orgnunit";
        User user = new User("test", "pass");
        user.setFacilityCode(orgUnit);

        setUpSuccessHttpGetRequest(200, "dataSets.json");

        commodityService.saveToDatabase(dhis2.fetchCommodities(user));
        categoryService.clearCache();
        return user;
    }
}