package org.clintonhealthaccess.lmis.app.backgroundServices;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

public class DownloadBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!validIntent(context, intent)) {
            return;
        }

        Long fileId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (!validDownloadFile(context, fileId)) {
            return;
        }
        Uri downloadedFile = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).getUriForDownloadedFile(fileId);
        install(context, downloadedFile);
    }

    private boolean validIntent(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            if (context.getPackageName().equals(intent.getPackage())) {
                return true;
            }
        }

        return false;
    }

    private boolean validDownloadFile(Context context, Long fileId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor c= downloadManager.query(new DownloadManager.Query().setFilterById(fileId));

        try {
            if(c.moveToFirst()){
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status != DownloadManager.STATUS_SUCCESSFUL) {
                    return false;
                }
                try {
                    String versionCode = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
                    String currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                    if(versionCode.compareTo(currentVersion) > 0){
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        } finally {
            c.close();
        }
        return false;
    }



    private void install(Context context, final Uri apk) {
        Intent i;

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            i=new Intent(Intent.ACTION_INSTALL_PACKAGE);
            i.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
        }
        else {
            i=new Intent(Intent.ACTION_VIEW);
        }

        i.setDataAndType(apk, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(i);
    }

}
