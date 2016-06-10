package nl.tudelft.exchange.student.locationtracker.data.receiver;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Pair;

/**
 * Created by Piotr on 2016-06-10.
 */
public class RSSIBroadcastReceiverInitializer {

    public static Pair<RSSIBroadcastReceiver,IntentFilter> initialize(RSSIScanResultHandler activity) {
        WifiManager wifiManager = (WifiManager) ((Activity)activity).getSystemService(Context.WIFI_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        RSSIBroadcastReceiver rssiBroadcastReceiver = new RSSIBroadcastReceiver(activity);
        ((Activity)activity).registerReceiver(rssiBroadcastReceiver, intentFilter);
        wifiManager.setFrequencyBand(WifiManager.WIFI_FREQUENCY_BAND_2GHZ, false);
        wifiManager.startScan();
        return Pair.create(rssiBroadcastReceiver,intentFilter);
    }
}
