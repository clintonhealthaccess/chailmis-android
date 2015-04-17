package org.clintonhealthaccess.lmis.app.events;

import org.clintonhealthaccess.lmis.app.models.Allocation;

public class AllocationCreateEvent {
    public  Allocation allocation;

    public AllocationCreateEvent(Allocation allocation){
        this.allocation = allocation;
    }
}
