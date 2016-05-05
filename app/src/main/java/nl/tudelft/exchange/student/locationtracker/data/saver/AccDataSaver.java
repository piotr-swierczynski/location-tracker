package nl.tudelft.exchange.student.locationtracker.data.saver;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import nl.tudelft.exchange.student.locationtracker.data.AccData;

/**
 * Created by Piotr on 2016-05-03.
 */
public class AccDataSaver {

    public void save(List<AccData> data, Context context) throws IOException{
        File file = null;
        FileOutputStream outputStream;
        try {
            file = new File(Environment.getExternalStorageDirectory(), "accdata.txt");

            outputStream = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(outputStream);
            for (AccData row: data) {
                pw.println(row.getTimestamp() + "\t" + row.getX() + "\t" + row.getY() + "\t" + row.getZ());
            }
            pw.flush();
            pw.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(context, new String[] {file.toString()}, null, null);
    }
}
