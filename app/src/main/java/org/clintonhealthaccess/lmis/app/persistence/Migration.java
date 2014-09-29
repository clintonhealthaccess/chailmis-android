package org.clintonhealthaccess.lmis.app.persistence;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

public interface Migration {
    void up(SQLiteDatabase db, ConnectionSource connectionSource);
    void down(SQLiteDatabase db, ConnectionSource connectionSource);
}
