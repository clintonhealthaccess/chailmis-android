package org.clintonhealthaccess.lmis.app.remote.endpoints;

import org.clintonhealthaccess.lmis.app.models.api.FDroid;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;

public interface FDroidEndPoint {

    @Headers({
            "Content-Type:application/xml"
    })
    @GET("/fdroid/{flavor}/repo")
    FDroid getVersionInfo(@Path("flavor") String flavor);
}
