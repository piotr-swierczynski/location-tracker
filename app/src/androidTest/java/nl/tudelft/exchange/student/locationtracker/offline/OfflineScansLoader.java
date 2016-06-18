package nl.tudelft.exchange.student.locationtracker.offline;

import android.net.wifi.ScanResult;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Piotr on 2016-05-29.
 */
public class OfflineScansLoader {

    public static List<List<ScanResult>> loadAllScanLists(String filename) {
        List<List<ScanResult>> scanResults = new ArrayList<>();
        File file;
        FileInputStream inputStream;
        String currentLine;
        ScanResult currentlyProcessScan = null;
        try {
            file = new File(filename);

            inputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            int i = 0;
            String[] bssids = null;
            String[] levels = null;
            List<ScanResult> oneScan = null;
            while  ( ( currentLine = bufferedReader.readLine() ) != null ) {
                if(i % 3 == 1) {
                    bssids = currentLine.split("\\t");
                } else if(i % 3 == 2) {
                    levels = currentLine.split("\\t");
                } else {
                    if(bssids!=null && levels!=null) {
                        oneScan = new ArrayList<>();
                        for(int j = 0; j < bssids.length; j++) {
                            currentlyProcessScan = new ScanResult();
                            currentlyProcessScan.BSSID = bssids[j];
                            currentlyProcessScan.level = Integer.parseInt(levels[j]);
                            oneScan.add(currentlyProcessScan);
                        }
                        scanResults.add(oneScan);
                    }
                    bssids = null;
                    levels = null;
                }
                ++i;
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scanResults;
    }

}
