package nl.tudelft.exchange.student.locationtracker.filter.data.loader;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.exchange.student.locationtracker.filter.data.AccessPoint;
import nl.tudelft.exchange.student.locationtracker.filter.data.SignalInCellCharacteristic;

/**
 * Created by Piotr on 2016-05-25.
 */
public class BayesianFilterDataLoader {

    public static Map<String, AccessPoint> loadData(String filename) {
        Map<String,AccessPoint> result = new HashMap<>();
        File file;
        FileInputStream inputStream;
        String currentLine;
        Integer cellID = null;
        try {
            file = new File(Environment.getExternalStorageDirectory(), filename);

            inputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while  ( ( currentLine = bufferedReader.readLine() ) != null ) {
                if(currentLine.contains("\t")) {
                    readSignalCharacterization(currentLine, result, cellID);
                } else {
                    cellID = Integer.parseInt(currentLine);
                }
            }

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void readSignalCharacterization(String currentLine, Map<String,AccessPoint> result, Integer cellID) {
        String[] signalCharacteristicParts = currentLine.split("\\t");
        AccessPoint currentAccessPoint = null;
        if(result.containsKey(signalCharacteristicParts[0])) {
            currentAccessPoint = result.get(signalCharacteristicParts[0]);
        } else {
            currentAccessPoint = new AccessPoint(signalCharacteristicParts[0]);
            result.put(signalCharacteristicParts[0], currentAccessPoint);
        }
        currentAccessPoint.getCellsCharacteristicMap().put(cellID,
                new SignalInCellCharacteristic(cellID,
                        Double.parseDouble(signalCharacteristicParts[1]),
                        Double.parseDouble(signalCharacteristicParts[2])));
    }
}
