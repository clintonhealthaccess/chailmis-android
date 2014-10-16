/*
 * Copyright (c) 2014, ThoughtWorks
 *
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

package org.clintonhealthaccess.lmis.utils;

import com.google.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.Adjustment;
import org.clintonhealthaccess.lmis.app.models.AdjustmentReason;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.AdjustmentService;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import roboguice.inject.InjectResource;

public class LMISTestCase {
    @InjectResource(R.string.dhis2_base_url)
    protected String dhis2BaseUrl;

    protected void setUpSuccessHttpGetRequest(int i, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        Robolectric.addPendingHttpResponse(200, rootDataSetJson);
    }

    protected void setUpSuccessHttpPostRequest(int i, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        Robolectric.addPendingHttpResponse(200, rootDataSetJson);
    }

    protected void setUpSuccessHttpGetRequest(String uri, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        Robolectric.addHttpResponseRule("GET", String.format("%s%s", dhis2BaseUrl, uri), new TestHttpResponse(200, rootDataSetJson));
    }

    protected void setUpSuccessHttpPostRequest(String uri, String fixtureFile) throws IOException {
        String rootDataSetJson = readFixtureFile(fixtureFile);
        Robolectric.addHttpResponseRule("POST", String.format("%s%s", dhis2BaseUrl, uri), new TestHttpResponse(200, rootDataSetJson));
    }

    private String readFixtureFile(String fileName) throws IOException {
        URL url = this.getClass().getClassLoader().getResource("fixtures/" + fileName);
        InputStream src = url.openStream();
        String content = IOUtils.toString(src);
        src.close();
        return content;
    }

    public static void adjust(Commodity commodity, int quantity, boolean positive, AdjustmentReason reason, AdjustmentService adjustmentService) {
        Adjustment adjustment = new Adjustment(commodity, quantity, positive, reason.getName());
        adjustmentService.save(Arrays.asList(adjustment));
    }
}
