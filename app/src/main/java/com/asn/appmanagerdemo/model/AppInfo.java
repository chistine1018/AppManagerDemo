package com.asn.appmanagerdemo.model;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class AppInfo implements Parcelable {
    private String appName; //應用名
    private String appNamePinyin; //應名拼音名稱

    private String packageName; //應用的包名
    private String versionName; //應用的版本名稱
    private Drawable appIcon; // 應用的圖標
    private String[] permissionInfos; //應用的權限訊息
    private ApplicationInfo applicationInfo;


    private String apkSize; //apk大小
    private long allSize; //總大小
    private long appSize; //app大小
    private long dataSize; //數據大小
    private long cacheSize; //緩存大小

    public AppInfo() {
    }

    protected AppInfo(Parcel in) {
        appName = in.readString();
        packageName = in.readString();
        versionName = in.readString();
        //appIcon = in.readParcelable(Bitmap.class.getClassLoader());
        permissionInfos = in.createStringArray();
        applicationInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeString(versionName);
        //dest.writeParcelable(appIcon,flags);
        dest.writeStringArray(permissionInfos);
        dest.writeParcelable(applicationInfo, flags);
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", appNamePinyin='" + appNamePinyin + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", appIcon=" + appIcon +
                ", permissionInfos=" + Arrays.toString(permissionInfos) +
                ", applicationInfo=" + applicationInfo +
                ", apkSize='" + apkSize + '\'' +
                ", allSize=" + allSize +
                ", appSize=" + appSize +
                ", dataSize=" + dataSize +
                ", cacheSize=" + cacheSize +
                '}';
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getApkSize() {
        return apkSize;
    }

    public void setApkSize(String apkSize) {
        this.apkSize = apkSize;
    }

    public long getAllSize() {
        return allSize;
    }

    public void setAllSize(long allSize) {
        this.allSize = allSize;
    }

    public long getAppSize() {
        return appSize;
    }

    public void setAppSize(long appSize) {
        this.appSize = appSize;
    }

    public long getDataSize() {
        return dataSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getAppNamePinyin() {
        return appNamePinyin;
    }

    public void setAppNamePinyin(String appNamePinyin) {
        this.appNamePinyin = appNamePinyin;
    }

    public String[] getPermissionInfos() {
        return permissionInfos;
    }

    public void setPermissionInfos(String[] permissionInfos) {
        this.permissionInfos = permissionInfos;
    }

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

}
