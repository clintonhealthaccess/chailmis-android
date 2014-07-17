package org.clintonhealthaccess.lmis.app.remote.endpoints;

import org.clintonhealthaccess.lmis.app.LmisException;

import java.util.List;
import java.util.Map;

import retrofit.http.GET;

public interface Dhis2Endpoint {

    @GET("/api/systemSettings/reasons_for_order")
    Map<String,List<String>> getReasonsForOrder();

    @GET("/api/dataSets") // login is valid once it can access data without exception
    Object validateLogin() throws LmisException;
}
  