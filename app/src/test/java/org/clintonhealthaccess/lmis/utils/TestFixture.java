package org.clintonhealthaccess.lmis.utils;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;
import static com.google.common.io.ByteStreams.copy;
import static org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository.COMMODITIES_FILE;

public class TestFixture {
    public static void initialiseCommodities(Context context) throws IOException {
        InputStream src = context.getAssets().open("default_commodities.json");
        FileOutputStream dest = context.openFileOutput(COMMODITIES_FILE, MODE_PRIVATE);
        copy(src, dest);
        dest.close();
    }
}
