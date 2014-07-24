package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;

@DatabaseTable(tableName = "users")
public class User {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String username;

    @DatabaseField(canBeNull = false)
    private String password;

    @DatabaseField()
    private String facilityCode;

    @DatabaseField()
    private String facilityName;

    public User() {
        // ormlite likes it
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String facilityCode) {
        this.username = username;
        this.password = password;
        this.facilityCode = facilityCode;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;

        return id == user.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public String encodeCredentialsForBasicAuthorization() {
        String credentials = username + ":" + password;
        String base64EncodedCredentials = encodeToString(credentials.getBytes(), NO_WRAP);
        return "Basic " + base64EncodedCredentials;
    }

    public String getFacilityCode() {
        return facilityCode;
    }

    public void setFacilityCode(String facilityCode) {
        this.facilityCode = facilityCode;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }
}
