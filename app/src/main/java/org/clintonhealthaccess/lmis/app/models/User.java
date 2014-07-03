package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {
    @DatabaseField(id = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String username;

    @DatabaseField(canBeNull = false)
    private String password;

    public User() {
        // ormlite likes it
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
