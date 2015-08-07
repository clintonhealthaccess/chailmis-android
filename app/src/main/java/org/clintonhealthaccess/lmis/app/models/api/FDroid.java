package org.clintonhealthaccess.lmis.app.models.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class FDroid {

    @Path("application/package")
    @Element
    private String version;

    public String getVersion() {
        return version;
    }

    public static boolean checkVersion(String remoteVersion, String currentVersion) {
        if (remoteVersion.compareTo(currentVersion) > 0) {
            return true;
        }
        return false;
    }

    public static class FDroidRepo {

        @Element
        private String description;
    }
}
