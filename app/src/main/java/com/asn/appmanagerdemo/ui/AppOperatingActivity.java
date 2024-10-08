package com.asn.appmanagerdemo.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;


import com.asn.appmanagerdemo.R;
import com.asn.appmanagerdemo.model.AppInfo;
import com.asn.appmanagerdemo.util.AppUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AppOperatingActivity extends AppCompatActivity {

    private AppInfo appInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_operating);
        Intent intent = getIntent();
        appInfo = intent.getParcelableExtra("appInfo");
        Bitmap bm = getIntent().getParcelableExtra("appIcon");
        if (appInfo.getAppIcon() == null) {
            return;
        }
        setTitle(appInfo.getAppName());

        PackageManager pm = getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(appInfo.getPackageName(), PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            installed = false;
        }
        if (!installed) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    //進入應用
    public void enterApplication(View view) {
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(appInfo.getPackageName());

        if (intent != null) {
            startActivity(getPackageManager().getLaunchIntentForPackage(appInfo.getPackageName()));
        } else {
            Toast.makeText(this, "打開失敗", Toast.LENGTH_SHORT).show();
        }
    }

    //卸載應用
    public void uninstallApplication(View view) {
        Uri uri = Uri.parse("package:" + appInfo.getPackageName());
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        startActivity(intent);
//        finish();
//        MainActivity.Input.publishSubject1.onNext(true);
    }

    //應用詳情
    public void applicationDetails(View view) {
        String packageName = appInfo.getPackageName();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", packageName, null));
        startActivity(intent);
    }

    //提取圖標
    public void saveIcon(View view) {

//        //檢查權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(AppOperatingActivity.this, "沒有存儲權限", Toast.LENGTH_SHORT).show();

            } else {
                byte[] appIcon;
                Bitmap bm;
                appIcon = getIntent().getByteArrayExtra("appIcon");
                bm = BitmapFactory.decodeByteArray(appIcon, 0, appIcon.length);


//            Bitmap bm = getIntent().getParcelableExtra("appIcon");
                FileOutputStream fos;
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AppManager/Icon/";
//            File file = new File(path + appInfo.getAppName() + ".png");

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), appInfo.getAppName() + ".png");

                //判断文件夹是否存在
                if (!new File(path).exists()) {
                    new File(path).mkdirs();
                }
                Log.i("path", "saveIcon: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + appInfo.getAppName() + ".png");
                try {
                    fos = new FileOutputStream(file, false);
                    //壓縮bitmap寫進outputStream 參數：輸出格式  輸出質量  目標OutputStream
                    //格式可以為jpg,png,jpg不能存儲透明
                    bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    //關閉流
                    Toast.makeText(this, "文件以保存至:\n" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + appInfo.getAppName() + ".png", Toast.LENGTH_LONG).show();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    AppUtil.exceptionToast(AppOperatingActivity.this, e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    AppUtil.exceptionToast(AppOperatingActivity.this, e.getMessage());
                }

            }
        }


    }

    //提取apk
    public void saveAPK(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(AppOperatingActivity.this, "沒有存儲權限", Toast.LENGTH_SHORT).show();

            } else {
                final ProgressDialog dialog = ProgressDialog.show(this, "複製文件", "正在複製...");

                //長時間的工作不能在主線程做，得啟動分線程執行
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AppManager/APK/";

                            if (Build.VERSION.SDK_INT > 29) {
                                outputPath = getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/app/audio/";
                            } else {
                                outputPath = Environment.getExternalStorageDirectory().getPath() + "/app/audio/";
                            }

                            //判斷文件是否存在，不存在則創建
                            if (!new File(outputPath).exists()) {
                                new File(outputPath).mkdirs();
                            }

                            long startTime = System.currentTimeMillis();

                            File file = new File(appInfo.getApplicationInfo().sourceDir);
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputPath + appInfo.getAppName() + "_" + appInfo.getVersionName() + ".apk"));
                            byte[] bytes = new byte[1024];
                            int len;
                            while ((len = bis.read(bytes)) > 0) {
                                bos.write(bytes, 0, len);
                            }
                            bos.close();
                            bis.close();

                            long endTime = System.currentTimeMillis();
                            //耗時
                            final double usedTime = (endTime - startTime) / 1000.0;
                            //速度
                            final double speed = file.length() / usedTime / 1048576.0;
                            String finalOutputPath = outputPath;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {//在主線程進行
                                    Toast.makeText(AppOperatingActivity.this, "文件以保存至:\n" + finalOutputPath + appInfo.getAppName() + "_" + appInfo.getVersionName() + ".apk  \n用時:" + String.format("%.1f", usedTime) + "s  速度:" + String.format("%.1f", speed) + "MB/s", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            AppUtil.exceptionToast(AppOperatingActivity.this, e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            AppUtil.exceptionToast(AppOperatingActivity.this, e.getMessage());
                        }

                        //移除dialog
                        dialog.dismiss();//方法在分線程執行，但内部使用Handler實現主線程移除dialog
                    }
                }).start();

            }
        }

    }

    //分享應用
    public void shareApp(View view) {
        Toast.makeText(this, "正在生成臨時文件...", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(appInfo.getApplicationInfo().sourceDir);//安装包目錄文件名都是base.apk
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(getExternalCacheDir(), appInfo.getAppName() + "_" + appInfo.getVersionName() + ".apk")));
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = bis.read(bytes)) > 0) {
                        bos.write(bytes, 0, len);
                    }
                    bos.close();
                    bis.close();

                    //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    //StrictMode.setVmPolicy(builder.build());
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    //    builder.detectFileUriExposure();
                    //}

                    Uri uri;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(AppOperatingActivity.this, "com.zhou.appmanager.fileprovider", new File(getExternalCacheDir(), appInfo.getAppName() + "_" + appInfo.getVersionName() + ".apk"));
                    } else {
                        uri = Uri.fromFile(new File(getExternalCacheDir(), appInfo.getAppName() + "_" + appInfo.getVersionName() + ".apk"));
                    }
                    //uri= Uri.fromFile(new File(getExternalCacheDir(), appInfo.getAppName() + ".apk"));


                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    //intent.setData(uri);
                    //intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    intent.setType("application/vnd.android.package-archive");//此處可發送多種文件
                    //在Activity上下文之外啟動Activity需要给Intent設置FLAG_ACTIVITY_NEW_TASK標誌，不然會報異常。
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //臨時訪問讀權限  intent的接受者將被授予 INTENT 數據uri 或者 在ClipData 上的讀權限。
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "分享文件"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    AppUtil.exceptionToast(AppOperatingActivity.this, e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    AppUtil.exceptionToast(AppOperatingActivity.this, e.getMessage());
                }
            }
        }).start();

    }

    //activity銷毀時清除緩存
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteDirWihtFile(getExternalCacheDir());
    }

    //刪除一個目錄下所有文件包括目錄本身
    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 刪除所有文件}
            } else if (file.isDirectory()) {
                deleteDirWihtFile(file); // 遞規的方式刪除文件夾}
            }
        }
        dir.delete();// 刪除目錄本身
    }

    //權限訊息
    public void permissionInfo(View view) {
        Intent intent = new Intent(AppOperatingActivity.this, PermissionInfoActivity.class);
        intent.putExtra("appInfo", appInfo);
        startActivity(intent);
    }

    public void gotoAPPMarket(View view) {
        String str = "market://details?id=" + appInfo.getPackageName();
        Intent localIntent = new Intent(Intent.ACTION_VIEW);
        localIntent.setData(Uri.parse(str));
        startActivity(localIntent);
    }
}
