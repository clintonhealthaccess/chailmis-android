package org.clintonhealthaccess.lmis.app.services;

import android.content.Context;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.persistence.CommoditiesRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CommodityService {
    @Inject
    private Context context;

    @Inject
    private CommoditiesRepository commoditiesRepository;

    public void initialise() {
        // FIXME: should load from remote LMIS server
        String commoditiesJson;
        try {
            InputStream src = context.getAssets().open("default_commodities.json");
            commoditiesJson = CharStreams.toString(new InputStreamReader(src));
        } catch (IOException e) {
            throw new LmisException("Doesn't matter, we will change this anyway.", e);
        }

        commoditiesRepository.save(commoditiesJson);
    }
}
