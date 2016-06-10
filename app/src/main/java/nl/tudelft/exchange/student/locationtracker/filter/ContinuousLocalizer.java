package nl.tudelft.exchange.student.locationtracker.filter;

import android.net.wifi.ScanResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Piotr on 2016-06-10.
 */
public class ContinuousLocalizer {

    private VotingLocalizer[] votingLocalizersGroup;
    private Queue<List<ScanResult>> localizationSignalsBuffer = new LinkedList<>();
    private int epochCounter = 0;

    public ContinuousLocalizer(String radioMapsFile) {
        votingLocalizersGroup = new VotingLocalizer[VotingLocalizer.VOTE_SIZE];
        for(int i = 0; i < VotingLocalizer.VOTE_SIZE; ++i) {
            votingLocalizersGroup[i] = new VotingLocalizer(radioMapsFile);
        }
    }

    public int localize(List<ScanResult> scanResults) {
        int localizedCell = -1;
        localizationSignalsBuffer.add(scanResults);
        if(localizationSignalsBuffer.size() == VotingLocalizer.VOTE_SIZE) {
            localizedCell = votingLocalizersGroup[epochCounter % VotingLocalizer.VOTE_SIZE].localize((List)localizationSignalsBuffer);
            ++epochCounter;
            localizationSignalsBuffer.remove();
        }
        return localizedCell;
    }

    public void reset() {
        localizationSignalsBuffer = new LinkedList<>();
        epochCounter = 0;
    }
}
