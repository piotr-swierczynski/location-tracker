package nl.tudelft.exchange.student.locationtracker.offline;

import android.net.wifi.ScanResult;
import android.util.Pair;

import java.util.List;

import nl.tudelft.exchange.student.locationtracker.filter.BayesianFilter;

/**
 * Created by Piotr on 2016-06-18.
 */
public class OfflineBayesianFilterTest {

    public static void main(String[] args) {
        BayesianFilter bayesianFilter = new BayesianFilter(OfflineBayesianFilterDataLoader.loadData("C:\\Users\\Piotr\\Desktop\\Smart Phone Sensing\\EWIdata.txt"));
        List<List<ScanResult>> offlineScans = OfflineScansLoader.loadAllScanLists("C:\\Users\\Piotr\\Desktop\\Gathering Data\\C2.txt");
        Integer[] score = setArray(new Integer[BayesianFilter.NUMBER_OF_CELLS]);
        Integer[] tmpScore = setArray(new Integer[BayesianFilter.NUMBER_OF_CELLS]);
        int i = 0;
        for (List<ScanResult> singleScan : offlineScans) {
            Pair<Integer, Double> result = bayesianFilter.probability(singleScan);
            if(i%7 == 0 && i != 0) {
                int best = -1;
                int idx = -1;
                for(int j = 0; j < score.length; ++j) {
                    if(tmpScore[j] > best) {
                        best = tmpScore[j];
                        idx = j;
                    }
                }
                score[idx] = score[idx] + 1;
                tmpScore = setArray(new Integer[BayesianFilter.NUMBER_OF_CELLS]);
                bayesianFilter.resetFilter();
            }
            if(result.second > 0.9) {
                tmpScore[result.first] = tmpScore[result.first] + 1;
            }
            ++i;
        }
        for(i = 0; i < BayesianFilter.NUMBER_OF_CELLS; ++i)
            System.out.print(score[i]+" ");
        System.out.println();
    }

    private static Integer[] setArray(Integer[] array) {
        for(int i = 0; i < BayesianFilter.NUMBER_OF_CELLS; ++i) {
            array[i] = 0;
        }
        return array;
    }
}
