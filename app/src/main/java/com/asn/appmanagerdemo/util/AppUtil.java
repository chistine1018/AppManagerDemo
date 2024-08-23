package com.asn.appmanagerdemo.util;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;


import com.asn.appmanagerdemo.model.AppInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AppUtil {
    public final static int USER_APP = 1;
    public final static int SYSTEM_APP = 2;

    //獲取已安装的app信息
    public static List<AppInfo> getAppInfo(int tag, Context context) {
        PackageManager pm = context.getPackageManager();
        //獲取所有的app信息
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        List<AppInfo> userApp = new ArrayList<>();
        List<AppInfo> systemApp = new ArrayList<>();

        for (PackageInfo packageInfo : packages) {
            // 判斷系統/非系統應用
            AppInfo appInfo = new AppInfo();
            try {
                appInfo.setAppName(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
//                appInfo.setAppNamePinyin(PinyinTool.getPinyinString(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString()));
                appInfo.setAppNamePinyin(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
                appInfo.setPackageName(packageInfo.packageName);
                appInfo.setVersionName(packageInfo.versionName);
                appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(context.getPackageManager()));
                appInfo.setApplicationInfo(packageInfo.applicationInfo);
                String[] permissions = pm.getPackageInfo(appInfo.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
                if (permissions == null) {
                    permissions = new String[]{};
                }
                appInfo.setPermissionInfos(permissions);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {// 非系統應用
                userApp.add(appInfo);
            } else {  // 系統應用
                systemApp.add(appInfo);
            }
        }

        if (tag == SYSTEM_APP) {
            return systemApp;
        } else if (tag == USER_APP) {
            return userApp;
        } else {
            return null;
        }
    }

    //獲取大小,需要有訪問使用紀錄的權限
    public static void getSize(Context context, List<AppInfo> appInfos) {
        try {
            //僅在8.0及以上才執行
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
                StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();

                for (int i = 0; i < appInfos.size(); i++) {
                    AppInfo appInfo = appInfos.get(i);
                    for (StorageVolume storageVolume : storageVolumes) {
                        String uuidStr = storageVolume.getUuid();
                        String description = storageVolume.getDescription(context);
                        //Log.e("description:", "description:"+description);
                        if (description.equals("內部存儲")) {
                            UUID uuid = uuidStr == null ? StorageManager.UUID_DEFAULT : UUID.fromString(uuidStr);
                            int uid = AppUtil.getAppUid(context, appInfo.getPackageName());
                            StorageStats storageStats = storageStatsManager.queryStatsForUid(uuid, uid);
                            //總大小=應用大小+數據大小
                            long allSize = storageStats.getAppBytes() + storageStats.getDataBytes();
                            appInfo.setAllSize(allSize);
                            appInfo.setAppSize(storageStats.getAppBytes());//應用大小
                            appInfo.setDataSize(storageStats.getDataBytes());//數據大小
                            appInfo.setCacheSize(storageStats.getCacheBytes());//緩存大小
                        }
                    }
                    appInfos.set(i, appInfo);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //獲取應用的uid
    public static int getAppUid(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return info.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //檢測是否擁有訪問使用紀錄的權限
    public static boolean hasUsageStatsPermission(Context context) {
        //http://stackoverflow.com/a/42390614/878126
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        final int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        boolean granted;
        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    //拋出異常時打toast
    public static void exceptionToast(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    //申請權限
    public static void requestPermissions(Activity activity) {
        //動態申請權限  WRITE_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            //如果應用程式之前請求過此權限但使用者拒絕了請求，此方法將傳回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //這裡可以寫個對話框之類的項目向使用者解釋為什麼要申請權限，並在對話框的確認鍵後續再次申請權限
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                //申請權限，字串陣列內是一個或多個要申請的權限，1是申請權限結果的回傳參數，在onRequestPermissionsResult可以得知申請結果
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
            }
        }

        //動態申請權限 READ_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //如果應用程式之前請求過此權限但使用者拒絕了請求，此方法將傳回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //這裡可以寫個對話框之類的項目向使用者解釋為什麼要申請權限，並在對話框的確認鍵後續再次申請權限
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                //申請權限，字串陣列內是一個或多個要申請的權限，1是申請權限結果的回傳參數，在onRequestPermissionsResult可以得知申請結果
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, 1);
            }
        }
    }

    //根據名稱排序
    public static void sortByName(int sortByName, List<AppInfo> userAppInfos, List<AppInfo> systemAppInfos) {
        if (sortByName % 2 == 0) {//變成升序排序(sortByName預設=1)
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o1.getAppName(), o2.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    //return o1.getAppName().compareTo(o2.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o1.getAppName(), o2.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o1.getAppName(), o2.getAppName());
                    //return o1.getAppName().compareTo(o2.getAppName());
                }
            });
        } else { //變成降序排序
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o2.getAppName(), o1.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o2.getAppName(), o1.getAppName());
                    //return o2.getAppName().compareTo(o1.getAppName());
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    Comparator<Object> com = java.text.Collator.getInstance(java.util.Locale.CHINA);
                    return com.compare(o2.getAppName(), o1.getAppName());
                    //Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
                    //return com.compare(o2.getAppName(), o1.getAppName());
                    //return o2.getAppName().compareTo(o1.getAppName());
                }
            });
        }
    }

    //根據權限數量排序
    public static void sortByPermissions(int sortByPermissions, List<AppInfo> userAppInfos, List<AppInfo> systemAppInfos) {
        if (sortByPermissions % 2 == 0) {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = o1.getPermissionInfos().length - o2.getPermissionInfos().length;
                    //if (i > 0) {
                    //    return 1;
                    //} else if (i == 0) {
                    //    return 0;
                    //} else {
                    //    return -1;
                    //}
                    //return (i < 0) ? -1 : ((i == 0) ? 0 : 1);
                    return Integer.compare(i, 0);
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = o1.getPermissionInfos().length - o2.getPermissionInfos().length;
                    return Integer.compare(i, 0);
                }
            });
        } else {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = o1.getPermissionInfos().length - o2.getPermissionInfos().length;
                    if (i > 0) {
                        return -1;
                    } else if (i == 0) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = o1.getPermissionInfos().length - o2.getPermissionInfos().length;
                    return Integer.compare(0, i);
                }
            });
        }
    }

    //根據apk大小排序
    public static void sortByAPKSize(int sortBySize, List<AppInfo> userAppInfos, List<AppInfo> systemAppInfos) {
        if (sortBySize % 2 == 0) {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int) new File(o1.getApplicationInfo().sourceDir).length() - (int) new File(o2.getApplicationInfo().sourceDir).length();
                    return Integer.compare(i, 0);
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int) new File(o1.getApplicationInfo().sourceDir).length() - (int) new File(o2.getApplicationInfo().sourceDir).length();
                    return Integer.compare(i, 0);
                }
            });
        } else {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int) new File(o1.getApplicationInfo().sourceDir).length() - (int) new File(o2.getApplicationInfo().sourceDir).length();
                    return Integer.compare(0, i);
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int) new File(o1.getApplicationInfo().sourceDir).length() - (int) new File(o2.getApplicationInfo().sourceDir).length();
                    return Integer.compare(0, i);
                }
            });
        }
    }

    //根據apk大小排序
    public static void sortByALLSize(int sortByALLSize, List<AppInfo> userAppInfos, List<AppInfo> systemAppInfos) {
        if (sortByALLSize % 2 == 0) {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int) o1.getAllSize() - (int) o2.getAllSize();
                    return Integer.compare(i, 0);
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int) o1.getAllSize() - (int) o2.getAllSize();
                    return Integer.compare(i, 0);
                }
            });
        } else {
            Collections.sort(userAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int) o1.getAllSize() - (int) o2.getAllSize();
                    return Integer.compare(0, i);
                }
            });
            Collections.sort(systemAppInfos, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo o1, AppInfo o2) {
                    int i = (int) o1.getAllSize() - (int) o2.getAllSize();
                    return Integer.compare(0, i);
                }
            });
        }
    }
}
