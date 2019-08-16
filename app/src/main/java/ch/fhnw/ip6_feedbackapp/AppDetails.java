package ch.fhnw.ip6_feedbackapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class AppDetails implements Comparable< AppDetails > {
    private String appVersion;
    private String packageName;
    private String appName;
    private Drawable appIcon;


    public AppDetails() { }

    public AppDetails(String packageName, String name, Drawable icon, String appVersion) {
        this.appVersion = appVersion;
        this.packageName = packageName;
        this.appName = name;
        this.appIcon = icon;
    }

    // Constructor that gets all data from a package name
    public AppDetails(String pkgName, Context context){
        this.packageName = pkgName;
        Log.d("APPDETAILSCONSTRUCTOR", packageName);

        // Get app name
        PackageManager packageManager = context.getPackageManager();
        try {
            appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("APPDETAILSCONSTRUCTOR", appName);

        // Get app icon
        try
        {
            appIcon = packageManager.getApplicationIcon(packageName);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        // Get app version
        try {
            PackageInfo pInfo  = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("APPDETAILSCONSTRUCTOR", appVersion);
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    @Override
    public int compareTo(AppDetails appDetails) {
        return this.getAppName().compareTo(appDetails.getAppName());
    }
}
