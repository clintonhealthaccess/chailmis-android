package org.clintonhealthaccess.lmis.app.remote.endpoints;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.UserProfile;
import org.clintonhealthaccess.lmis.app.models.Aggregation;
import org.clintonhealthaccess.lmis.app.models.api.DataElement;
import org.clintonhealthaccess.lmis.app.models.api.DataElementGroup;
import org.clintonhealthaccess.lmis.app.models.api.DataElementGroupSet;
import org.clintonhealthaccess.lmis.app.models.DataSet;

import java.util.List;
import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Path;

public interface Dhis2Endpoint {

    @GET("/api/systemSettings/reasons_for_order")
    Map<String, List<String>> getReasonsForOrder();

    @GET("/api/systemSettings/data_element_group_set_id")
    String getDateElementGroupSetId();

    @GET("/api/me")
    UserProfile validateLogin() throws LmisException;

    @GET("/api/dataSets/{id}")
    DataSet getDataSet(@Path("id") String id);

    @GET("/api/dataElements/{id}")
    DataElement getDataElement(@Path("id") String id);

    @GET("/api/categoryCombos/{id}")
    Aggregation getCategoryCombo(@Path("id") String id);

    @GET("/api/dataElementGroupSets/{id}")
    DataElementGroupSet getDataElementGroupSet(@Path("id") String id);

    @GET("/api/dataElementGroups/{id}")
    DataElementGroup getDataElementGroup(@Path("id") String id);
}
  