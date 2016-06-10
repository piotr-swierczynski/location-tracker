package nl.tudelft.exchange.student.locationtracker.filter;

import android.net.wifi.ScanResult;
import android.util.Pair;

import java.util.List;

import nl.tudelft.exchange.student.locationtracker.filter.data.loader.BayesianFilterDataLoader;

/**
 * Created by Piotr on 2016-06-10.
 */
public class VotingLocalizer {

    private BayesianFilter bayesianFilter;
    public static final int VOTE_SIZE = 7;
    private int[] votes = new int[BayesianFilter.NUMBER_OF_CELLS];
    private int votesCounter;

    public VotingLocalizer(String radioMapsFile) {
        bayesianFilter = new BayesianFilter(BayesianFilterDataLoader.loadData(radioMapsFile));
    }

    public int localize(List<List<ScanResult>> localizationSignalsBuffer) {
        resetVoting();
        bayesianFilter.resetFilter();
        for (List<ScanResult> scanResults : localizationSignalsBuffer) {
            Pair<Integer, Double> iterationResultsFromBayesianFilter = bayesianFilter.probability(scanResults);
            ++votes[iterationResultsFromBayesianFilter.first];
            ++votesCounter;
            if (votesCounter == VOTE_SIZE) {
                return finalizeLocalizationProcess();
            }
        }
        return -1;
    }

    private void resetVoting() {
        votesCounter = 0;
        for(int i = 0; i < votes.length; ++i) {
            votes[i] = 0;
        }
    }

    private int finalizeLocalizationProcess() {
        int highestNumberOfVotes = -1;
        int cellIndexWithHighestNumberOfVotes = -1;
        for(int i = 0; i < votes.length; ++i) {
            if(votes[i] > highestNumberOfVotes) {
                highestNumberOfVotes = votes[i];
                cellIndexWithHighestNumberOfVotes = i;
            } else if (votes[i] > highestNumberOfVotes) {
                if(bayesianFilter.getAposterioriMemory()[i] >
                        bayesianFilter.getAposterioriMemory()[cellIndexWithHighestNumberOfVotes]) {
                    highestNumberOfVotes = votes[i];
                    cellIndexWithHighestNumberOfVotes = i;
                }
            }
        }
        return cellIndexWithHighestNumberOfVotes;
    }
}
