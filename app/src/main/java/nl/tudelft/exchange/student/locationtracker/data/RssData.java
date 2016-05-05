package nl.tudelft.exchange.student.locationtracker.data;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by Piotr on 2016-05-03.
 */
public class RssData extends Data{

    private List<ScanResult> scanResults;

    public RssData(long timestamp, List<ScanResult> scanResults) {
        this.timestamp = timestamp;
        this.scanResults = scanResults;
    }

    public List<ScanResult> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }

}
