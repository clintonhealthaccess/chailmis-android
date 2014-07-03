package org.clintonhealthaccess.lmis.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

public class MySqliteOpenHelper extends OrmLiteSqliteOpenHelper {
    public MySqliteOpenHelper(Context context) {
        super(context, "lmis_test", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }
}
