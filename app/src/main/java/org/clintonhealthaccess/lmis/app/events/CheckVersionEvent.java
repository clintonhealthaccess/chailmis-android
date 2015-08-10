package org.clintonhealthaccess.lmis.app.events;

public class CheckVersionEvent {
    private String latestVersion;

    public CheckVersionEvent(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
