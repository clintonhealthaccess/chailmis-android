package org.clintonhealthaccess.lmis.app.services;

import java.util.List;

import retrofit.http.GET;

public interface DHIS2ApiService {

    @GET("/api/systemSettings/reasons_for_order")
    List<String> getReasonsForOrder();
}
