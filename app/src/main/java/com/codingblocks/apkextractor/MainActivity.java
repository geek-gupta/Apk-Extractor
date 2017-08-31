package com.codingblocks.apkextractor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DrawableUtils;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity
        implements
        ApkListAdapter.OnItemClickListener {
    
    RecyclerView recyclerView;
    ArrayList<ApkListModel> apksList;
    ApkListAdapter adapter;
    ProgressBar progressBar;
    LoadDataTask loadDataTask;
    DownloadTask downloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        apksList = new ArrayList<>();

        adapter = new ApkListAdapter(this, apksList, this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        recyclerView.setAdapter(adapter);

        requestPermission();

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions
                (this, new String[]{android.Manifest.permission.GET_TASKS},
                        234);
    }

    void fillList2() {
        ArrayList<PackageInfo> res = new ArrayList<PackageInfo>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packs = pm.getInstalledPackages(0);

        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            String description = (String) p.applicationInfo.loadDescription(pm);
            String label = p.applicationInfo.loadLabel(pm).toString();
            String packageName = p.packageName;
            String versionName = p.versionName;
            int versionCode = p.versionCode;
            Drawable icon = p.applicationInfo.loadIcon(pm);
            String filePath = p.applicationInfo.publicSourceDir.toString();
            ApkListModel apkModel = new ApkListModel(label, filePath, icon,
                    packageName, versionName, String.valueOf(versionCode));
            apksList.add(apkModel);
//Continue to extract other info about the app...
        }
    }

    void fillList() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo p : apps) {
            String packageName3 = p.activityInfo.applicationInfo.packageName;
            String packageName2 = p.activityInfo.packageName;
            int packageUID = p.activityInfo.applicationInfo.uid;

            String description = (String) p.activityInfo.applicationInfo.loadDescription(getPackageManager());
            String label = p.activityInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            String packageName = p.activityInfo.packageName;
            //  String versionName = p.activityInfo.applicationInfo.versionName;
            //int versionCode = p.activityInfo.versionCode;
            Drawable icon = p.activityInfo.applicationInfo.loadIcon(getPackageManager());

            //getPackageManager().getApplicationLabel(this.getPackageManager());

//            ApkListModel model = new ApkListModel(info.geta)
            //    String appname = packageManager.getApplicationLabel(pk).toString()

        }
    }

    String extract(String clickedPackageName) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(mainIntent, 0);

        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File savepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        InputStream in;
        OutputStream outputStream;
        //FileOutputStream outputStream;

        for (ResolveInfo info : apps) {
            String packageName3 = info.activityInfo.packageName;

            if (packageName3.equals(clickedPackageName)) {
                try {
                    String label = info.activityInfo.applicationInfo.
                            loadLabel(getPackageManager()).toString();

                    File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
                    in = new FileInputStream(file);
                    String fileOutPutPath = savepath.toString() + "/" + label + ".apk";
                    outputStream = new FileOutputStream
                            (fileOutPutPath);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    in.close();
                    outputStream.close();
                    return fileOutPutPath;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    static String currentPackageName = "";

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        downloadTask = new DownloadTask();
                        downloadTask.execute(currentPackageName);
                    } else {
                        Toast.makeText
                                (this, "Allow Permission to continue", Toast.LENGTH_LONG).show();
                    }
                }

            }
        } else if (requestCode == 234) {
            loadDataTask = new LoadDataTask();
            loadDataTask.execute();
        }
    }

    @Override
    protected void onDestroy() {
        if (loadDataTask != null)
            loadDataTask.cancel(true);
        if (downloadTask != null)
            downloadTask.cancel(true);
        super.onDestroy();
    }

    void writeToFile() {

    }

    @Override
    public void onItemClick(int position, ApkListModel model) {
        currentPackageName = model.getPackageName();
        ActivityCompat.requestPermissions
                (this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        123);
    }

    public class LoadDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            fillList2();
            Collections.sort(apksList, new Comparator<ApkListModel>() {
                @Override
                public int compare(ApkListModel lhs, ApkListModel rhs) {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    public class DownloadTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Starting for " +
                    currentPackageName, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            publishProgress(2, 3, 4);
            return extract(params[0]);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
            String message = "Unable to save";
            if (path != null) {
                message = "saved " + path;
            }
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
