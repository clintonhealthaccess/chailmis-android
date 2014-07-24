package org.clintonhealthaccess.lmis.app.remote.endpoints;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.CategoryCombo;
import org.clintonhealthaccess.lmis.app.models.DataElement;
import org.clintonhealthaccess.lmis.app.models.DataSet;
import org.clintonhealthaccess.lmis.app.models.UserProfile;

import java.util.List;
import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Path;

public interface Dhis2Endpoint {

    @GET("/api/systemSettings/reasons_for_order")
    Map<String, List<String>> getReasonsForOrder();

    @GET("/api/me")
    UserProfile validateLogin() throws LmisException;

    @GET("/api/dataSets/{id}")
    DataSet getDataSet(@Path("id") String id);

    @GET("/api/dataElements/{id}")
    DataElement getDataElement(@Path("id") String id);

    @GET("/api/categoryCombos/{id}")
    CategoryCombo getCategoryCombo(@Path("id") String id);
}
  