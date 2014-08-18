/*
 * Copyright (c) 2014, Clinton Health Access Initiative
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
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Category;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.j256.ormlite.android.apptools.OpenHelperManager.getHelper;
import static com.j256.ormlite.android.apptools.OpenHelperManager.releaseHelper;
import static com.j256.ormlite.dao.DaoManager.createDao;
import static java.util.Arrays.asList;

public class TestFixture {
    public static void initialiseDefaultCommodities(Context context) throws IOException {
        SQLiteOpenHelper openHelper = getHelper(context, LmisSqliteOpenHelper.class);
        try {
            for (Category category : defaultCategories(context)) {
                initialiseCategoryDao(openHelper).create(category);
                for (Commodity commodity : category.getNotSavedCommodities()) {
                    commodity.setCategory(category);
                    initialiseCommodityDao(openHelper).create(commodity);
                }
            }
        } catch (SQLException e) {
            throw new LmisException(e);
        } finally {
            releaseHelper();
        }
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

    private static Dao<Category, String> initialiseCategoryDao(SQLiteOpenHelper openHelper) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, Category.class);
    }

    private static Dao<Commodity, String> initialiseCommodityDao(SQLiteOpenHelper openHelper) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(openHelper);
        return createDao(connectionSource, Commodity.class);
    }

}
