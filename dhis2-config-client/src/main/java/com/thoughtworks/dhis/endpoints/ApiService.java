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

    @GET("/dataSets/{id}")
    DataSet getDataSetWithDetails(@Path("id") String id,@Query("fields") String fields);

    @POST("/dataValueSets")
    Object submitValueSet(@Body DataValueSet set);

    @GET("/me")
    UserProfile getProfile();


}
