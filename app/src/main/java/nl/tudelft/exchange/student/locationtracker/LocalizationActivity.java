package nl.tudelft.exchange.student.locationtracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import nl.tudelft.exchange.student.locationtracker.data.receiver.RSSIBroadcastReceiver;
import nl.tudelft.exchange.student.locationtracker.data.receiver.RSSIBroadcastReceiverInitializer;
import nl.tudelft.exchange.student.locationtracker.data.receiver.RSSIScanResultHandler;
import nl.tudelft.exchange.student.locationtracker.filter.BayesianFilter;
import nl.tudelft.exchange.student.locationtracker.filter.ContinuousLocalizer;
import nl.tudelft.exchange.student.locationtracker.filter.data.loader.BayesianFilterDataLoader;
import nl.tudelft.exchange.student.locationtracker.movement.ArrowManager;

public class LocalizationActivity extends AppCompatActivity implements RSSIScanResultHandler{

    private Pair<RSSIBroadcastReceiver, IntentFilter> broadcastReceiverPair;
    private BayesianFilter bayesianFilter = new BayesianFilter(BayesianFilterDataLoader.loadData("PDF.txt"));
    private boolean enabledLocalization = false;
    private boolean enabledContinuousLocalizationAccurate = false;
    private boolean enabledContinuousLocalizationFast = false;
    private boolean userInitialBelief = false;
    private ProgressDialog progressDialog;
    private static final int VOTE_SIZE = 9;
    private int[] votes = new int[BayesianFilter.NUMBER_OF_CELLS];
    private int votesCounter;
    private ContinuousLocalizer continuousLocalizer = new ContinuousLocalizer("PDF.txt");
    private ImageButton currentCell = null;
    private ImageButton previousCell = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        broadcastReceiverPair = RSSIBroadcastReceiverInitializer.initialize(this);
        initializeImageButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiverPair.first, broadcastReceiverPair.second);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiverPair.first);
    }

    public void onLocalizationClick(View v) {
        if(!userInitialBelief)
            bayesianFilter.resetFilter();
        clearTheInDoorMap();
        resetVotes();
        votesCounter = 0;
        updateDisplayedProbabilities();
        enabledLocalization = true;
        //progressDialog = ProgressDialog.show(this, "Localization process", "It may take a few seconds", true);
    }

    public void onInitialBeliefClick(View v) {
        ImageButton initialBeliefCell = null;
        for(View imBtn : LocalizationActivity.this.findViewById(R.id.local_layout).getTouchables()) {
            if(imBtn instanceof ImageButton && ((ImageButton) imBtn).getColorFilter() != null) {
                initialBeliefCell = ((ImageButton) imBtn);
                break;
            }
        }
        if(initialBeliefCell == null) {
            Toast.makeText(LocalizationActivity.this, "Choose one of the cells as initial belief!", Toast.LENGTH_SHORT).show();
        } else {
            int chosenCellID = Integer.parseInt(initialBeliefCell.getTag().toString()) - 1;
            int numberOfNeighbours = 0;
            int[] neighboursIdx = new int[] {-1,-1,-1};
            Double[] apostiriorMemory = new Double[BayesianFilter.NUMBER_OF_CELLS];
            apostiriorMemory[chosenCellID] = 0.3;
            for(int i = 0; i < BayesianFilter.NUMBER_OF_CELLS; ++i) {
                if(ArrowManager.arrow[chosenCellID][i] > 0) {
                    ++numberOfNeighbours;
                    neighboursIdx[numberOfNeighbours-1] = i;
                }
            }
            for(int i = 0; i < BayesianFilter.NUMBER_OF_CELLS; ++i) {
                if(i == chosenCellID) {
                    apostiriorMemory[i] = 0.3;
                } else if(neighboursIdx[0] == i || neighboursIdx[1] == i || neighboursIdx[2] == i) {
                    apostiriorMemory[i] = 0.1;
                } else {
                    apostiriorMemory[i] = (1-0.3-(0.1*numberOfNeighbours))/(BayesianFilter.NUMBER_OF_CELLS-numberOfNeighbours-1);
                }
            }
            bayesianFilter.setAposterioriMemory(apostiriorMemory);
            userInitialBelief = true;
        }
    }

    public void onContinuousLocalizationClick(View v) {
        if(!enabledContinuousLocalizationAccurate) {
            continuousLocalizer.reset();
        }
        enabledContinuousLocalizationAccurate = !enabledContinuousLocalizationAccurate;
    }

    public void onContinuousLocalizationFastClick(View v) {
        enabledContinuousLocalizationFast = !enabledContinuousLocalizationFast;
    }

    public void finalizeLocalizationProcess() {
        enabledLocalization = false;
        int best = -1;
        int idx = -1;
        for(int i = 0; i < votes.length; ++i) {
            if(votes[i] > best) {
                best = votes[i];
                idx = i;
            } else if (votes[i] > best) {
                if(bayesianFilter.getAposterioriMemory()[i] > bayesianFilter.getAposterioriMemory()[idx]) {
                    best = votes[i];
                    idx = i;
                }
            }
        }
        int localizedCellID = getResources().getIdentifier("c"+(idx + 1), "id", getPackageName());
        ImageButton localizedCell = (ImageButton)findViewById(localizedCellID);
        localizedCell.setColorFilter(Color.argb(110, 255, 0, 0));
        Toast.makeText(LocalizationActivity.this, "Jestes w pokoju o id: C" + (idx + 1), Toast.LENGTH_SHORT).show();
        userInitialBelief = false;
        //progressDialog.dismiss();
    }

    @Override
    public void handleScanResults(List<ScanResult> scanResults) {
        if(enabledLocalization) {
            Pair<Integer, Double> iterationResultsFromBayesianFilter = bayesianFilter.probability(scanResults);
            updateDisplayedProbabilities();
            if(iterationResultsFromBayesianFilter.second > 0.95) {
                ++votes[iterationResultsFromBayesianFilter.first];
                ++votesCounter;
                if (votesCounter == VOTE_SIZE) {
                    finalizeLocalizationProcess();
                }
            }
        } else if(enabledContinuousLocalizationFast) {
            clearTheInDoorMap();
            bayesianFilter.resetFilter();
            Pair<Integer, Double> iterationResultsFromBayesianFilter = bayesianFilter.probability(scanResults);
            updateDisplayedProbabilities();

            int localizedCellID = getResources().getIdentifier("c"+(iterationResultsFromBayesianFilter.first + 1), "id", getPackageName());
            ImageButton localizedCell = (ImageButton)findViewById(localizedCellID);
            localizedCell.setColorFilter(Color.argb(110, 255, 0, 0));
            if (currentCell == null) {
                currentCell = localizedCell;
            } else if (currentCell != localizedCell) {
                previousCell = currentCell;
                currentCell = localizedCell;
                ArrowManager.setArrow(this, previousCell.getTag(), currentCell.getTag());
                //previousCell.setColorFilter(Color.argb(65, 255, 0, 0));
            }
        } else if(enabledContinuousLocalizationAccurate) {
            clearTheInDoorMap();
            int cellIndex = continuousLocalizer.localize(scanResults);
            if (cellIndex != -1) {
                int localizedCellID = getResources().getIdentifier("c" + (cellIndex + 1), "id", getPackageName());
                ImageButton localizedCell = (ImageButton) findViewById(localizedCellID);
                localizedCell.setColorFilter(Color.argb(110, 255, 0, 0));
                if (currentCell == null) {
                    currentCell = localizedCell;
                } else if (currentCell != localizedCell) {
                    previousCell = currentCell;
                    currentCell = localizedCell;
                    ArrowManager.setArrow(this, previousCell.getTag(), currentCell.getTag());
                    //previousCell.setColorFilter(Color.argb(65, 255, 0, 0));
                }
                if (previousCell != null) {
                    //previousCell.setColorFilter(Color.argb(65, 255, 0, 0));
                }
            }
        }
    }

    private void updateDisplayedProbabilities() {
        TextView tmpTextView;
        for(int i = 0; i < BayesianFilter.NUMBER_OF_CELLS; ++i) {
            int textViewID = getResources().getIdentifier("p_c"+(i + 1), "id", getPackageName());
            tmpTextView = (TextView) findViewById(textViewID);
            tmpTextView.setTextSize(10);
            tmpTextView.setText(new DecimalFormat("#.####").format(bayesianFilter.getAposterioriMemory()[i]));
        }
    }

    private void resetVotes() {
        for(int i = 0; i < votes.length; ++i) {
            votes[i] = 0;
        }
    }

    private void clearTheInDoorMap() {
        for(View imBtn : LocalizationActivity.this.findViewById(R.id.local_layout).getTouchables()) {
            if(imBtn instanceof ImageButton) {
                ((ImageButton) imBtn).clearColorFilter();
            }
        }
    }

    private void initializeImageButtons() {
        for(final View view : findViewById(R.id.local_layout).getTouchables()) {
            if(view instanceof ImageButton) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if(((ImageButton) view).getColorFilter() == null) {
                                    clearTheInDoorMap();
                                    ((ImageButton) view).setColorFilter(Color.argb(110, 255, 255, 0));
                                } else {
                                    ((ImageButton)view).clearColorFilter();
                                }
                                return true;
                            case MotionEvent.ACTION_UP:
                                return true;
                        }
                        return false;
                    }
                });
            }
        }
    }
}
