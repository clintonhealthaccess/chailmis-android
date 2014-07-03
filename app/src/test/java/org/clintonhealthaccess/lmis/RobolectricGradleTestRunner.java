package org.clintonhealthaccess.lmis;

import org.junit.runners.model.InitializationError;
import org.mockito.MockitoAnnotations;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;

import static java.lang.System.getProperty;
import static org.robolectric.annotation.Config.DEFAULT;
import static org.robolectric.res.Fs.fileFromPath;

public class RobolectricGradleTestRunner extends RobolectricTestRunner {

    public RobolectricGradleTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String manifestProperty = getProperty("android.manifest");
        if (config.manifest().equals(DEFAULT) && manifestProperty != null) {
            String resProperty = getProperty("android.resources");
            String assetsProperty = getProperty("android.assets");
            return new AndroidManifest(fileFromPath(manifestProperty), fileFromPath(resProperty), fileFromPath(assetsProperty));
        }
        return super.getAppManifest(config);
    }
}