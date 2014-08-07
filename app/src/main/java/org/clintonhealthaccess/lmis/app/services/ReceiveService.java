package org.clintonhealthaccess.lmis.app.services;

import org.clintonhealthaccess.lmis.app.models.Receive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReceiveService {

    public List<String> getReadyAllocationIds() {
        return new ArrayList<>(Arrays.asList("UG-2004", "UG-2005"));
    }

    public List<String> getCompletedIds() {
        return new ArrayList<>();
    }

    public void saveReceive(Receive receive) {

    }
}
