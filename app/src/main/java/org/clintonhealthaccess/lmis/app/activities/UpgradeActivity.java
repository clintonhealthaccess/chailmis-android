package org.clintonhealthaccess.lmis.app.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import org.clintonhealthaccess.lmis.app.BuildConfig;
import org.clintonhealthaccess.lmis.app.R;

import java.io.File;

public class UpgradeActivity extends ActionBarActivity {

    public static final String LATEST_VERSION_CODE = "latest_code";
    private String latestVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        latestVersion = getIntent().getStringExtra(LATEST_VERSION_CODE);
    }

    public void later(View view) {
        finish();
    }

    public void download(View view) {
        Uri apkUri = Uri.parse(getString(R.string.app_market_base_url) + "/fdroid/" + BuildConfig.FLAVOR + "/repo/LMIS.apk");
        DownloadManager.Request request = new DownloadManager.Request(apkUri);
        String fileName = "LMIS-" + latestVersion + ".apk";
        request.setTitle(fileName);
        request.setDescription(latestVersion);
        request.setDestinationUri(Uri.fromFile(new File(getExternalFilesDir(null).getPath() + "/" + fileName)));
        ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
        finish();
    }
}
