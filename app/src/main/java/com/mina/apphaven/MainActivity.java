package com.mina.apphaven;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;


public class MainActivity extends Activity {

    private final String[] appNames = {"test", "APP2", "APP3"};
    private final String[] appDownloadUrls = {
        "https://github.com/ruihq/AppHaven/releases/download/testapk/base.2.apk",
        "https://github.com/ruihq/AppHaven/raw/main/releases/app2.apk",
        "https://github.com/ruihq/AppHaven/raw/main/releases/app3.apk"
    };
    private final String[] appImages = {
            "https://github.com/ruihq/AppHaven/releases/download/testapk/app_icon.1.png",
            "https://github.com/username/repo/raw/main/releases/app2.png",
            "https://github.com/username/repo/raw/main/releases/app3.png"
    };

    private Spinner appSpinner;
    private Button downloadButton;
    private ProgressBar downloadProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appSpinner = findViewById(R.id.appSpinner);
        downloadButton = findViewById(R.id.downloadButton);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);

        String[] displayAppNames = new String[appNames.length];
        for (int i = 0; i < appNames.length; i++) {
            displayAppNames[i] = appNames[i] + " " + (i + 1);
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, displayAppNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appSpinner.setAdapter(spinnerAdapter);

        appSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAppDetails(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadSelectedApp();
            }
        });
    }

    private void updateAppDetails(int position) {
        String appName = appNames[position];
        String appDownloadUrl = appDownloadUrls[position];
        String appImageUrl = appImages[position];

        TextView appDetailsTextView = findViewById(R.id.appDetailsTextView);
        appDetailsTextView.setText("App Name: " + appName + "\nDownload URL: " + appDownloadUrl);

        ImageView appImageView = findViewById(R.id.appImageView);
        Glide.with(this).load(appImageUrl).into(appImageView);
    }

    private void downloadSelectedApp() {
        String appName = appNames[appSpinner.getSelectedItemPosition()];
        String appDownloadUrl = appDownloadUrls[appSpinner.getSelectedItemPosition()];

        Uri uri = Uri.parse(appDownloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(appName + " Download");

        String apkFileName = appName + ".apk";
        File apkFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), apkFileName);
        request.setDestinationUri(Uri.fromFile(apkFile));

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        // Show the progress bar and start listening for download completion
        downloadProgressBar.setVisibility(View.VISIBLE);
        updateDownloadProgress(downloadId);
    }

    private void updateDownloadProgress(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;

                while (downloading) {
                    Cursor cursor = downloadManager.query(query);
                    if (cursor != null && cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        int downloadedBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        final int progress = (int) ((downloadedBytes * 100L) / totalBytes);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downloadProgressBar.setProgress(progress);
                            }
                        });

                        if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                            downloading = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    downloadProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Download " + (status == DownloadManager.STATUS_SUCCESSFUL ? "completed" : "failed"), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        cursor.close();
                    }
                }
            }
        }).start();
    }
}
