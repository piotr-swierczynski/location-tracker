package nl.tudelft.exchange.student.locationtracker.data.saver;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.wifi.ScanResult;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import nl.tudelft.exchange.student.locationtracker.data.AccData;
import nl.tudelft.exchange.student.locationtracker.data.RssData;

/**
 * Created by Piotr on 2016-05-03.
 */
public class RssDataSaver {

    public void save(List<RssData> data, String filename, Context context) throws IOException {
        File file = null;
        FileOutputStream outputStream;
        try {
            file = new File(Environment.getExternalStorageDirectory(), filename+".txt");

            outputStream = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(outputStream);
            for (RssData row: data) {
                pw.println(row.getTimestamp());
                for(ScanResult result : row.getScanResults()) {
                    pw.print(result.BSSID+"\t");
                }
                pw.println();
                for(ScanResult result : row.getScanResults()) {
                    pw.print(result.level+"\t");
                }
                pw.println();
            }
            pw.flush();
            pw.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null, null);
    }
}
