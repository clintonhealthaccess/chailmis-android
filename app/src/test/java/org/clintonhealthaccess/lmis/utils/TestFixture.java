package org.clintonhealthaccess.lmis.utils;

import android.content.Context;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import org.clintonhealthaccess.lmis.app.models.Category;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.google.common.io.ByteStreams.copy;
import static java.util.Arrays.asList;
import static org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository.COMMODITIES_FILE;

public class TestFixture {
    public static void initialiseDefaultCommodities(Context context) throws IOException {
        InputStream src = context.getAssets().open("default_commodities.json");
        FileOutputStream dest = context.openFileOutput(COMMODITIES_FILE, MODE_PRIVATE);
        copy(src, dest);
        dest.close();
    }

    public static List<Category> defaultCommodities(Context context) throws IOException {
        InputStream src = context.getAssets().open("default_commodities.json");
        String defaultCommoditiesAsJson = CharStreams.toString(new InputStreamReader(src));
        return asList(new Gson().fromJson(defaultCommoditiesAsJson, Category[].class));
    }
}
