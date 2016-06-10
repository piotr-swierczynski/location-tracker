package nl.tudelft.exchange.student.locationtracker.data.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

/**
 * Created by Piotr on 2016-06-10.
 */
public class RSSIBroadcastReceiver extends BroadcastReceiver {

    private RSSIScanResultHandler rssiScanResultHandler;

    public RSSIBroadcastReceiver(RSSIScanResultHandler rssiScanResultHandler) {
        this.rssiScanResultHandler = rssiScanResultHandler;
    }

    @Override
    public void onReceive(Context c, Intent i) {
        // Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
        WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        rssiScanResultHandler.handleScanResults(wifiManager.getScanResults());
        wifiManager.startScan(); // relaunch scan immediately
    }
}
