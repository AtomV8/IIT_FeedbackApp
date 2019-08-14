package ch.fhnw.ip6_feedbackapp;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DeviceData extends Application {

    final private String MEM_FILE = "/proc/meminfo";
    final private String SOC_FILE = "/proc/cpuinfo";

    String deviceManufacturer;
    String deviceModel;
    String soc;
    String androidSDK;
    String androidVersion;
    long ramTotal;
    long ramFree;
    String provider;
    String mobileDataConnectivity;
    String bluetoothState;
    String wifiState;
    String gpsState;
    int batteryPercentage;
    /* TODO: GPS COORDINATES */


    public void getEverything() {

        deviceManufacturer = getDeviceManufacturer();
        deviceModel = getDeviceModel();

        soc = getSOC();

        androidSDK = getSDK();
        androidVersion = getAndroidVersion();

        ramTotal = getRAM("free");
        ramFree = getRAM("total");

        provider = getProvider();

        mobileDataConnectivity = getConnectivity();

        bluetoothState = getBluetoothState();

        wifiState = getWifiState();

        gpsState = getGPSState();

        batteryPercentage = getBatteryPercentage();

    }

    // --------------- GETTERS -------------------

    public String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getDeviceModel() {
        return Build.MODEL;
    }

    public String getSDK() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public long getRAM(String param){
        String[] segs;
        FileReader fstream;
        long memTotal = -1;
        long memFree = -1;
        try {
            fstream = new FileReader(MEM_FILE);
        } catch (FileNotFoundException e) {
            Log.e("getRAM", "Could not read " + MEM_FILE);
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
            Log.e("getRAM", e.toString());
        }
        if(param == "total"){
            return memTotal;
        }else if(param == "free"){
            return memFree;
        }
        else return -1;
    }

    public String getSOC(){
        String[] segs;
        FileReader fstream;
        String soc = "Unknown";
        try {
            fstream = new FileReader(SOC_FILE);
        } catch (FileNotFoundException e) {
            Log.e("getSOC", "Could not read " + SOC_FILE);
            return soc;
        }
        BufferedReader in = new BufferedReader(fstream, 500);
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (line.indexOf("Hardware") >= 0) {
                    segs = line.trim().split(" ");
                    soc = segs[segs.length -1];
                }
            }
        } catch (IOException e) {
            Log.e("getSOC", e.toString());
        }
        return soc;
    }

    public String getProvider(){
        TelephonyManager telephonyManager = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
        return telephonyManager.getSimOperatorName();
    }

    public String getConnectivity(){
        TelephonyManager telephonyManager = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD: return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN: return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "Unknown";
        }
        throw new RuntimeException("New type of network");
    }

    public String getBluetoothState(){
        if(BluetoothAdapter.getDefaultAdapter().isEnabled()) return "On";
        return "Off";
    }

    public String getWifiState() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                return "On"; // Not connected to an access point
            }
            return "Connected"; // Connected to an access point
        }
        else {
            return "Off"; // Wi-Fi adapter is OFF
        }
    }

    public String getGPSState(){
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return "On";
        return "Off";
    }

    public int getBatteryPercentage(){
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int batteryPercentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return batteryPercentage;
    }
}

