package com.thoughtworks.dhis.endpoints;

import com.thoughtworks.dhis.models.CategoryCombo;
import com.thoughtworks.dhis.models.CategoryComboSearchResponse;
import com.thoughtworks.dhis.models.DataSet;
import com.thoughtworks.dhis.models.DataSetSearchResponse;
import com.thoughtworks.dhis.models.DataValueSet;
import com.thoughtworks.dhis.models.UserProfile;

import java.util.Map;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;


public interface ApiService {

    @POST("/metaData/?strategy=CREATE_AND_UPDATE")
    Map<String, Object> updateMetaData(@Body Map<String, Object> data);

    @GET("/categoryCombos")
    CategoryComboSearchResponse searchCategoryCombos(@Query("query") String query);

    @GET("/categoryCombos/{id}")
    CategoryCombo getCombo(@Path("id") String id);

    @GET("/dataSets")
    DataSetSearchResponse searchDataSets(@Query("query") String query, @Query("fields") String fields);

    @GET("/dataSets/{id}")
    DataSet getDataSet(@Path("id") String id);

    @POST("/dataValueSets")
    Object submitValueSet(@Body DataValueSet set);

    @GET("/me")
    UserProfile getProfile();


}
