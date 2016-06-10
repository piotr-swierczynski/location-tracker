package nl.tudelft.exchange.student.locationtracker.data.receiver;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by Piotr on 2016-06-10.
 */
public interface RSSIScanResultHandler {
    void handleScanResults(List<ScanResult> scanResults);
}
