package org.clintonhealthaccess.lmis.app.remote.endpoints;

import org.clintonhealthaccess.lmis.app.LmisException;

import java.util.List;

import retrofit.http.GET;

public interface Dhis2Endpoint {

    @GET("/api/systemSettings/reasons_for_order")
    List<String> getReasonsForOrder();

    @GET("/api/users")
    Object getUsers() throws LmisException;
}
  