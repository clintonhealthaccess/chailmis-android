package org.clintonhealthaccess.lmis.app.models;

import org.clintonhealthaccess.lmis.app.models.api.OrganisationUnit;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private String name;
    private String created;
    private String lastUpdated;
    private String surname;
    private String firstName;
    private String email;
    private String phoneNumber;
    private UserCredentials userCredentials;
    private List<OrganisationUnit> organisationUnits = new ArrayList<OrganisationUnit>();
    private String id;

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

    public List<OrganisationUnit> getOrganisationUnits() {
        return organisationUnits;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
