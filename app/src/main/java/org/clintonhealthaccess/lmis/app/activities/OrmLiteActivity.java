package org.clintonhealthaccess.lmis.app.activities;

import android.content.Context;
import android.os.Bundle;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.clintonhealthaccess.lmis.app.persistence.LmisSqliteOpenHelper;

import roboguice.activity.RoboActionBarActivity;

public class OrmLiteActivity extends RoboActionBarActivity {
    protected volatile LmisSqliteOpenHelper helper;
    protected volatile boolean created = false;
    private volatile boolean destroyed = false;


    public LmisSqliteOpenHelper getHelper() {
        if (helper == null) {
            if (!created) {
                throw new IllegalStateException("A call has not been made to onCreate() yet so the helper is null");
            } else if (destroyed) {
                throw new IllegalStateException(
                        "A call to onDestroy has already been made and the helper cannot be used after that point");
            } else {
                throw new IllegalStateException("Helper is null for some unknown reason");
            }
        } else {
            return helper;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (helper == null) {
            helper = getHelperInternal(this);
            created = true;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseHelper(helper);
        destroyed = true;
    }

    protected LmisSqliteOpenHelper getHelperInternal(Context context) {
        @SuppressWarnings({"unchecked", "deprecation"})
        LmisSqliteOpenHelper newHelper = OpenHelperManager.getHelper(context, LmisSqliteOpenHelper.class);
        return newHelper;
    }

    protected void releaseHelper(LmisSqliteOpenHelper helper) {
        OpenHelperManager.releaseHelper();
        this.helper = null;
    }
}
