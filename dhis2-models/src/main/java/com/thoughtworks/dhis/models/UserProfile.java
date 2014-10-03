package com.thoughtworks.dhis.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserProfile {
    private String name;
    private String created;
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
