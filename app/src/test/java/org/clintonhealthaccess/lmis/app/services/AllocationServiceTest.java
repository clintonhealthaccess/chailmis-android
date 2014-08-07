package org.clintonhealthaccess.lmis.app.services;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.models.Allocation;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(RobolectricGradleTestRunner.class)
public class AllocationServiceTest {

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
        Allocation firstAllocation = new Allocation();
        firstAllocation.setAllocationId("UG-2013");
        firstAllocation.setReceived(false);
        Allocation secondAllocation = new Allocation();
        secondAllocation.setAllocationId("UG-2012");
        secondAllocation.setReceived(true);
        allocationDao.create(firstAllocation);
        allocationDao.create(secondAllocation);
        assertThat(allocationService.getReceivedAllocationIds(), contains("UG-2012"));
    }

    @Test
    public void getYetToBeReceivedAllocationIdsShouldOnlyListIdsOfYetToBeReceivedAllocations() throws Exception {
        Allocation firstAllocation = new Allocation();
        firstAllocation.setAllocationId("UG-2013");
        firstAllocation.setReceived(false);
        Allocation secondAllocation = new Allocation();
        secondAllocation.setAllocationId("UG-2012");
        secondAllocation.setReceived(true);
        allocationDao.create(firstAllocation);
        allocationDao.create(secondAllocation);
        assertThat(allocationService.getYetToBeReceivedAllocationIds(), contains("UG-2013"));
    }

    @Test
    public void shouldGetAllocationByLmisId() throws Exception {
        String lmisId = "UG-2013";
        Allocation firstAllocation = new Allocation();
        firstAllocation.setAllocationId(lmisId);
        firstAllocation.setReceived(false);
        allocationDao.create(firstAllocation);
        assertThat(allocationService.getAllocationByLmisId(lmisId).getAllocationId(), is(lmisId));
    }
}