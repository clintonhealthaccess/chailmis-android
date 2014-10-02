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

package org.clintonhealthaccess.lmis.utils;

import android.content.Context;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.CommodityAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.clintonhealthaccess.lmis.app.models.LossReason.EXPIRED;
import static org.clintonhealthaccess.lmis.app.models.LossReason.MISSING;
import static org.clintonhealthaccess.lmis.app.models.LossReason.WASTED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFixture {
    public static Commodity buildMockCommodity() {
        Commodity mockCommodity = mock(Commodity.class);
        CommodityAction wasted = new CommodityAction(mockCommodity, "fake_id_1", "commodity WASTED", WASTED.name());
        CommodityAction missing = new CommodityAction(mockCommodity, "fake_id_2", "commodity MISSING", MISSING.name());
        CommodityAction expiries = new CommodityAction(mockCommodity, "fake_id_3", "commodity EXPIRED", EXPIRED.name());
        when(mockCommodity.getCommodityActionsSaved()).thenReturn(newArrayList(wasted, missing, expiries));
        return mockCommodity;
    }

    public static List<Category> defaultCategories(Context context) throws IOException {
        InputStream src = context.getAssets().open("default_commodities.json");
        String defaultCommoditiesAsJson = CharStreams.toString(new InputStreamReader(src));
        return asList(new Gson().fromJson(defaultCommoditiesAsJson, Category[].class));
    }

    public static List<Commodity> getDefaultCommodities(Context context) throws IOException {
        List<Commodity> defaultCommodities = new ArrayList<>();
        for(Category category : defaultCategories(context)) {
            defaultCommodities.addAll(category.getCommodities());
        }
        return defaultCommodities;
    }
}
