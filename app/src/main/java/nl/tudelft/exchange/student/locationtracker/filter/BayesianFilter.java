package nl.tudelft.exchange.student.locationtracker.filter;

import android.net.wifi.ScanResult;
import android.util.Pair;

import java.util.List;
import java.util.Map;

import nl.tudelft.exchange.student.locationtracker.filter.data.AccessPoint;

/**
 * Created by Piotr on 2016-05-25.
 */
public class BayesianFilter {

    public static final int NUMBER_OF_CELLS = 4;

    private Map<String, AccessPoint> accessPointMap;

    //private Map<String, Double[]> aposterioriProbabilities = new HashMap<>();

    private Double[] aposterioriMemory = new Double[NUMBER_OF_CELLS];

    public BayesianFilter(Map<String, AccessPoint> accessPointMap) {
        this.accessPointMap = accessPointMap;
//        for(Map.Entry<String, AccessPoint> entry : accessPointMap.entrySet()) {
//            Double[] cellProbabilitiesForCertainSignal = new Double[NUMBER_OF_CELLS];
//            for(int i = 0; i < NUMBER_OF_CELLS; ++i) {
//                cellProbabilitiesForCertainSignal[i] = null;
//            }
//            aposterioriProbabilities.put(entry.getKey(),cellProbabilitiesForCertainSignal);
//        }
        resetFilter();
    }

    public Map<String, AccessPoint> getAccessPointMap() {
        return accessPointMap;
    }

    public void setAccessPointMap(Map<String, AccessPoint> accessPointMap) {
        this.accessPointMap = accessPointMap;
    }

    public Double[] getAposterioriMemory() {
        return aposterioriMemory;
    }

    public void setAposterioriMemory(Double[] aposterioriMemory) {
        this.aposterioriMemory = aposterioriMemory;
    }

    public void resetFilter() {
        for(int i = 0; i < NUMBER_OF_CELLS; ++i) {
            aposterioriMemory[i] = null;
        }
    }

//    private Pair<Integer, Double> calculateOverallProbability(int initialBelieveCell, List<String> macAddresses) {
////        for(Map.Entry<String,Double[]> entry : aposterioriProbabilities.entrySet()) {
////            Log.d("LT", entry.getKey()+" -> "+entry.getValue()[0]+" "+entry.getValue()[1]);
////        }
//        double sum = 0.0;
//        double multipliedProbabilities;
//        for(int i = 0; i<NUMBER_OF_CELLS; ++i) {
//            multipliedProbabilities = 0.0;
//            for (String mac : macAddresses) {
//                multipliedProbabilities += aposterioriProbabilities.get(mac)[i] == null ? 0.0 : aposterioriProbabilities.get(mac)[i];
//            }
//            sum += multipliedProbabilities;
//        }
//        double bestCellProbability = 0.0, tmpProbability;
//        int cellID = -1;
//        for(int i = 0; i<NUMBER_OF_CELLS; ++i) {
//            tmpProbability = 0.0;
//            for (String mac : macAddresses) {
//                tmpProbability += aposterioriProbabilities.get(mac)[i] == null
//                        ? 0.0 : aposterioriProbabilities.get(mac)[i];
//            }
//            if(bestCellProbability < tmpProbability/sum) {
//                bestCellProbability = (tmpProbability/sum);
//                cellID = i;
//            }
//            Log.d("LT","CellID: "+i+" Multi: "+tmpProbability+" Sum: "+sum+" Probability: "+(tmpProbability/sum));
//        }
//        return Pair.create(cellID,bestCellProbability);
//    }
//
//    /* Updates the aposteriori probabilites matrix with regards to new signals data
//     */
//    public Pair<Integer, Double> updateBelieve(List<ScanResult> scanResults, int initialBelieveCell) {
//        Double[] probabilities = null;
//        double apriori = 1/(double)NUMBER_OF_CELLS;
//        List<String> macAddresses = new ArrayList<>();
//        for(int i = 0; i < NUMBER_OF_CELLS; ++i) {
//            for (ScanResult scanResult : scanResults) {
//                if (accessPointMap.containsKey(scanResult.BSSID) &&
//                        accessPointMap.get(scanResult.BSSID).getCellsCharacteristicMap().containsKey(i)) {
//                    probabilities = aposterioriProbabilities.get(scanResult.BSSID);
//                    macAddresses.add(scanResult.BSSID);
//                    if (probabilities[i] == null) {
//                        probabilities[i] = aposteriori(apriori, i,
//                                scanResult.level, scanResult.BSSID);
//                        Log.d("LT", "IF: " + probabilities[i]);
//                    } else {
//                        probabilities[i] = aposteriori(probabilities[i],
//                                i, scanResult.level, scanResult.BSSID);
//                        Log.d("LT", "ELSE: " + probabilities[i]);
//                    }
//                }
//            }
//        }
//        return calculateOverallProbability(initialBelieveCell, macAddresses);
//    }
//
//    // P(Si|Oj) = ( P(Oj|Si)*P(Si) ) / ( sum P(Oj|Si)*P(Si) |i )
//    private double aposteriori(double apriori, int initialBelieveCell, int signalStrength, String mac) {
//        double sum = 0.0;
//        double aposteriori = 0.0;
//        for(int i = 0; i<NUMBER_OF_CELLS; ++i) {
//            if(accessPointMap.get(mac).getCellsCharacteristicMap().containsKey(i)) {
////                if(i == initialBelieveCell) {
//                sum += probabilityOfObservationOnConditionThatState(i, signalStrength, mac) *
//                        (aposterioriProbabilities.get(mac)[i] == null ? apriori : aposterioriProbabilities.get(mac)[i]);
////                } else {
////                    sum += probabilityOfObservationOnConditionThatState(i, signalStrength, mac) * (1-apriori);
////                }
//            }
//            else {
//                sum += 1.0;
//            }
//        }
//        aposteriori = (probabilityOfObservationOnConditionThatState(initialBelieveCell,signalStrength,mac)*apriori)/sum;
//        return aposteriori;
//    }

    // P(Oj|Si)
    private double probabilityOfObservationOnConditionThatState(int cellID, int signalStrength, String mac) {
        return Gaussian.pdf(signalStrength,
                accessPointMap.get(mac).getCellsCharacteristicMap().get(cellID).getMeanSignalValue(),
                accessPointMap.get(mac).getCellsCharacteristicMap().get(cellID).getStandardDeviationOfSignalValue());
    }

    //P(O|Si) = pi( P(Oj|Si) )
    private double probabilityOfOverallObservationOnConditionThatState(List<ScanResult> scanResults, int cellID) {
        double probability = 1.0;
        for (ScanResult scanResult : scanResults) {
            if(accessPointMap.containsKey(scanResult.BSSID)) {
                probability *= accessPointMap.get(scanResult.BSSID).getCellsCharacteristicMap().containsKey(cellID)
                        ? probabilityOfObservationOnConditionThatState(cellID, scanResult.level, scanResult.BSSID) : 0.001;
            }
        }
        return probability;
    }

    //gives the pair of the room and its probability P(Si|O) - the best of all
    public Pair<Integer, Double> probability(List<ScanResult> scanResults) {
        double sum = 0.0;
        double apriori = 1/(double)NUMBER_OF_CELLS;
        for(int i = 0; i < NUMBER_OF_CELLS; ++i) {
            sum += probabilityOfOverallObservationOnConditionThatState(scanResults,i) *
                    (aposterioriMemory[i] != null ? aposterioriMemory[i] : apriori);
        }
        double bestCellProbability = 0.0, tmpProbability;
        int bestCellID = -1;
        for(int i = 0; i<NUMBER_OF_CELLS; ++i) {
            tmpProbability = probabilityOfOverallObservationOnConditionThatState(scanResults,i) *
                    (aposterioriMemory[i] != null ? aposterioriMemory[i] : apriori) / sum;
            aposterioriMemory[i] = tmpProbability;
            if(bestCellProbability < tmpProbability) {
                bestCellProbability = tmpProbability;
                bestCellID = i;
            }
        }
        return Pair.create(bestCellID,bestCellProbability);
    }

}
