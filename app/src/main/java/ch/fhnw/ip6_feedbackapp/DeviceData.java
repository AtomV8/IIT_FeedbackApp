package ch.fhnw.ip6_feedbackapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DeviceData {

    final private String MEM_FILE = "/proc/meminfo";
    final private String SOC_FILE = "/proc/cpuinfo";

    private Context context;

    String deviceManufacturer = "not provided";
    String deviceModel = "not provided";
    String soc = "not provided";
    String androidSDK = "not provided";
    String androidVersion = "not provided";
    long ramTotal = -1;
    long ramFree = -1;
    String provider = "not provided";
    String mobileDataConnectivity = "not provided";
    String bluetoothState = "not provided";
    String wifiState = "not provided";
    String gpsState = "not provided";
    int batteryPercentage = -1;

    double latitude = -1;
    double longitude = -1;

    public DeviceData(Context context) {
        this.context = context;
        getData();
    }


    public void getData() {

        /* TODO: Check shared preferences and only get the according device data */

        deviceManufacturer = pullDeviceManufacturer();
        deviceModel = pullDeviceModel();

        soc = pullSOC();

        androidSDK = pullSDK();
        androidVersion = pullAndroidVersion();

        ramTotal = pullRAM("free");
        ramFree = pullRAM("total");

        provider = pullProvider();

        mobileDataConnectivity = pullConnectivity();

        bluetoothState = pullBluetoothState();

        wifiState = pullWifiState();

        gpsState = pullGPSState();

        batteryPercentage = pullBatteryPercentage();

    }

    public String pullDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public String pullDeviceModel() {
        return Build.MODEL;
    }

    public String pullSDK() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public String pullAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public long pullRAM(String param) {
        String[] segs;
        FileReader fstream;
        long memTotal = -1;
        long memFree = -1;
        try {
            fstream = new FileReader(MEM_FILE);
        } catch (FileNotFoundException e) {
            return memFree;
        }
        BufferedReader in = new BufferedReader(fstream, 500);
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (line.indexOf("MemTotal:") >= 0) {
                    segs = line.trim().split("[ ]+");
                    memTotal = Long.parseLong(segs[1]);
                }
                if (line.indexOf("MemAvailable:") >= 0) {
                    segs = line.trim().split("[ ]+");
                    memFree = Long.parseLong(segs[1]);
                }
            }
        } catch (IOException e) {
        }
        if (param == "total") {
            return memTotal;
        } else if (param == "free") {
            return memFree;
        } else return -1;
    }

    public String pullSOC() {
        String[] segs;
        FileReader fstream;
        String soc = "Unknown";
        try {
            fstream = new FileReader(SOC_FILE);
        } catch (FileNotFoundException e) {
            return soc;
        }
        BufferedReader in = new BufferedReader(fstream, 500);
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (line.indexOf("Hardware") >= 0) {
                    segs = line.trim().split(" ");
                    soc = segs[segs.length - 1];
                }
            }
        } catch (IOException e) {
        }
        return soc;
    }

    public String pullProvider() {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        return telephonyManager.getSimOperatorName();
    }

    public String pullConnectivity() {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
        }
        throw new RuntimeException("New type of network");
    }

    public String pullBluetoothState() {
        if(BluetoothAdapter.getDefaultAdapter().isEnabled()) return "On";
        return "Off";
    }

    public String pullWifiState() {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                return "On"; // Not connected to an access point
            }
            return "Connected"; // Connected to an access point
        } else {
            return "Off"; // Wi-Fi adapter is OFF
        }
    }

    public String pullGPSState() {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return "On";
        return "Off";
    }

    public int pullBatteryPercentage() {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryPercentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return batteryPercentage;
    }

    // --------------- GETTERS -------------------

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getSoc() {
        return soc;
    }

    public String getAndroidSDK() {
        return androidSDK;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public long getRamTotal() {
        return ramTotal;
    }

    public long getRamFree() {
        return ramFree;
    }

    public String getProvider() {
        return provider;
    }

    public String getMobileDataConnectivity() {
        return mobileDataConnectivity;
    }

    public String getBluetoothState() {
        return bluetoothState;
    }

    public String getWifiState() {
        return wifiState;
    }

    public String getGpsState() {
        return gpsState;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    // --------------- SETTERS -------------------

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public void setSoc(String soc) {
        this.soc = soc;
    }

    public void setAndroidSDK(String androidSDK) {
        this.androidSDK = androidSDK;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public void setRamTotal(long ramTotal) {
        this.ramTotal = ramTotal;
    }

    public void setRamFree(long ramFree) {
        this.ramFree = ramFree;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setMobileDataConnectivity(String mobileDataConnectivity) {
        this.mobileDataConnectivity = mobileDataConnectivity;
    }

    public void setBluetoothState(String bluetoothState) {
        this.bluetoothState = bluetoothState;
    }

    public void setWifiState(String wifiState) {
        this.wifiState = wifiState;
    }

    public void setGpsState(String gpsState) {
        this.gpsState = gpsState;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }
}

