package ch.fhnw.ip6_feedbackapp;

import android.graphics.drawable.Drawable;

public class AppDetails implements Comparable< AppDetails > {
    private String appVersion;
    private String packageName;
    private String appName;
    Drawable appIcon;

    public AppDetails(String packageName, String name, Drawable icon, String appVersion) {
        this.appVersion = appVersion;
        this.packageName = packageName;
        this.appName = name;
        this.appIcon = icon;
    }


    public String getAppVersion() { return appVersion; }

    public String getPackageName() {
        return  packageName;
    }

    public String getName() {
        return appName;
    }

    public Drawable getIcon() {
        return appIcon;
    }


    @Override
    public int compareTo(AppDetails appDetails) {
        return this.getName().compareTo(appDetails.getName());
    }
}
