package com.mina.apphaven;

import android.app.Activity;
import android.content.Intent;
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

        promptInstall(apkFile);
    }

    private void promptInstall(File apkFile) {
        if (apkFile.exists()) {
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (installIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(installIntent);
            } else {
                Toast.makeText(MainActivity.this, "Error: Unable to find an app to handle the installation.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Error: APK file not found.", Toast.LENGTH_SHORT).show();
        }
    }
}
