package org.clintonhealthaccess.lmis.app.backgroundServices;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

public class DownloadBroadcastReceiver extends BroadcastReceiver {

    private String latestVersion;

    @Override
    public void onReceive(Context context, Intent intent) {
        Long fileId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (!validDownloadFile(context, fileId)) {
            return;
        }
        Uri downloadedFile = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).getUriForDownloadedFile(fileId);
        install(context, downloadedFile);
    }

    private boolean validDownloadFile(Context context, Long fileId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor c= downloadManager.query(new DownloadManager.Query().setFilterById(fileId));

        if(c.moveToFirst()){
            String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if(status == DownloadManager.STATUS_SUCCESSFUL && title.contains(latestVersion)){
                return true;
            }else{
                return false;
            }
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
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(i);
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
}
