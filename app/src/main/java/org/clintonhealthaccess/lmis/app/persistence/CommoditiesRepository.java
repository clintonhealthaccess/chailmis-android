package org.clintonhealthaccess.lmis.app.persistence;

import android.content.Context;

import com.google.gson.Gson;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.models.Category;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.google.common.io.ByteStreams.copy;
import static java.util.Arrays.asList;

public class CommoditiesRepository {
    @Inject
    private Context context;

    public static final String COMMODITIES_FILE = "commodities.json";

    public List<Category> allCategories() {
        InputStreamReader jsonReader;
        try {
            FileInputStream fileInput = context.openFileInput(COMMODITIES_FILE);
            jsonReader = new InputStreamReader(fileInput);
        } catch (IOException e) {
            throw new LmisException("Cannot read file: " + COMMODITIES_FILE, e);
        }

        Category[] categories = new Gson().fromJson(jsonReader, Category[].class);
        if (categories == null) {
            throw new LmisException("Invalid commodity categories in " + COMMODITIES_FILE);
        }
        return asList(categories);
    }

    public void save(String jsonString) {
        InputStream src = new ByteArrayInputStream(jsonString.getBytes());
        try {
            FileOutputStream dest = context.openFileOutput(COMMODITIES_FILE, MODE_PRIVATE);
            copy(src, dest);
        } catch (IOException e) {
            throw new LmisException("Cannot write file: " + COMMODITIES_FILE, e);
        }
    }
}
