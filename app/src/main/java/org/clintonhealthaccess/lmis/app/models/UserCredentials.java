package org.clintonhealthaccess.lmis.app.models;

import java.util.ArrayList;
import java.util.List;

public class UserCredentials {

    private String code;
    private String name;
    private String created;
    private String username;
    private String passwordLastUpdated;
    private List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>();
    private String lastLogin;
    private Boolean selfRegistered;
    private Boolean disabled;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordLastUpdated() {
        return passwordLastUpdated;
    }

    public void setPasswordLastUpdated(String passwordLastUpdated) {
        this.passwordLastUpdated = passwordLastUpdated;
    }

    public List<UserAuthorityGroup> getUserAuthorityGroups() {
        return userAuthorityGroups;
    }

    public void setUserAuthorityGroups(List<UserAuthorityGroup> userAuthorityGroups) {
        this.userAuthorityGroups = userAuthorityGroups;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Boolean getSelfRegistered() {
        return selfRegistered;
    }

    public void setSelfRegistered(Boolean selfRegistered) {
        this.selfRegistered = selfRegistered;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }


}
