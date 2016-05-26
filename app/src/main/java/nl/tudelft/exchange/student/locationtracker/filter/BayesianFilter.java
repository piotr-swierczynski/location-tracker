package nl.tudelft.exchange.student.locationtracker.filter;

import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tudelft.exchange.student.locationtracker.filter.data.AccessPoint;

/**
 * Created by Piotr on 2016-05-25.
 */
public class BayesianFilter {

    private static final int NUMBER_OF_CELLS = 2;

    private Map<String, AccessPoint> accessPointMap;

    private Map<String, Double[]> aposterioriProbabilities = new HashMap<>();

    public BayesianFilter(Map<String, AccessPoint> accessPointMap) {
        this.accessPointMap = accessPointMap;
        for(Map.Entry<String, AccessPoint> entry : accessPointMap.entrySet()) {
            Double[] cellProbabilitiesForCertainSignal = new Double[NUMBER_OF_CELLS];
            for(int i = 0; i < NUMBER_OF_CELLS; ++i) {
                cellProbabilitiesForCertainSignal[i] = null;
            }
            aposterioriProbabilities.put(entry.getKey(),cellProbabilitiesForCertainSignal);
        }
    }

    public Map<String, AccessPoint> getAccessPointMap() {
        return accessPointMap;
    }

    public void setAccessPointMap(Map<String, AccessPoint> accessPointMap) {
        this.accessPointMap = accessPointMap;
    }

    private double calculateOverallProbability(int initialBelieveCell, List<String> macAddresses) {
//        for(Map.Entry<String,Double[]> entry : aposterioriProbabilities.entrySet()) {
//            Log.d("LT", entry.getKey()+" -> "+entry.getValue()[0]+" "+entry.getValue()[1]);
//        }
        double sum = 0.0;
        double multipliedProbabilities;
        for(int i = 0; i<NUMBER_OF_CELLS; ++i) {
            multipliedProbabilities = 0.0;
            for (String mac : macAddresses) {
                multipliedProbabilities += aposterioriProbabilities.get(mac)[i] == null ? 0.0 : aposterioriProbabilities.get(mac)[i];
            }
            sum += multipliedProbabilities;
        }
        multipliedProbabilities = 0.0;
        for (String mac : macAddresses) {
            multipliedProbabilities += aposterioriProbabilities.get(mac)[initialBelieveCell] == null
                    ? 0.0 : aposterioriProbabilities.get(mac)[initialBelieveCell];
        }
        Log.d("LT","Multi: "+multipliedProbabilities+" Sum: "+sum);
        return multipliedProbabilities/sum;
    }

    /* Updates the aposteriori probabilites matrix with regards to new signals data
     */
    public double updateBelieve(List<ScanResult> scanResults, int initialBelieveCell) {
        Double[] probabilities = null;
        double apriori = 1/(double)NUMBER_OF_CELLS;
        List<String> macAddresses = new ArrayList<>();
        for(int i = 0; i < NUMBER_OF_CELLS; ++i) {
            for (ScanResult scanResult : scanResults) {
                if (accessPointMap.containsKey(scanResult.BSSID) &&
                        accessPointMap.get(scanResult.BSSID).getCellsCharacteristicMap().containsKey(i)) {
                    probabilities = aposterioriProbabilities.get(scanResult.BSSID);
                    macAddresses.add(scanResult.BSSID);
                    if (probabilities[i] == null) {
                        probabilities[i] = aposteriori(apriori, i,
                                scanResult.level, scanResult.BSSID);
                        Log.d("LT", "IF: " + probabilities[i]);
                    } else {
                        probabilities[i] = aposteriori(probabilities[i],
                                i, scanResult.level, scanResult.BSSID);
                        Log.d("LT", "ELSE: " + probabilities[i]);
                    }
                }
            }
        }
        return calculateOverallProbability(initialBelieveCell, macAddresses);
    }

    // P(Si|Oj) = ( P(Oj|Si)*P(Si) ) / ( sum P(Oj|Si)*P(Si) |i )
    private double aposteriori(double apriori, int initialBelieveCell, int signalStrength, String mac) {
        double sum = 0.0;
        double aposteriori = 0.0;
        for(int i = 0; i<NUMBER_OF_CELLS; ++i) {
            if(accessPointMap.get(mac).getCellsCharacteristicMap().containsKey(i)) {
                if(i == initialBelieveCell) {
                    sum += probabilityOfObservationOnConditionThatState(i, signalStrength, mac) * apriori;
                } else {
                    sum += probabilityOfObservationOnConditionThatState(i, signalStrength, mac) * (1-apriori);
                }
            }
            else {
                sum += 1.0;
            }
        }
        aposteriori = (probabilityOfObservationOnConditionThatState(initialBelieveCell,signalStrength,mac)*apriori)/sum;
        return aposteriori;
    }

    // P(Oj|Si)
    private double probabilityOfObservationOnConditionThatState(int cellID, int signalStrength, String mac) {
        return Gaussian.pdf(signalStrength,
                accessPointMap.get(mac).getCellsCharacteristicMap().get(cellID).getMeanSignalValue(),
                accessPointMap.get(mac).getCellsCharacteristicMap().get(cellID).getStandardDeviationOfSignalValue());
    }

}
