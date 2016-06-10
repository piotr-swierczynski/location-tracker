package nl.tudelft.exchange.student.locationtracker;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

import nl.tudelft.exchange.student.locationtracker.filter.BayesianFilter;
import nl.tudelft.exchange.student.locationtracker.filter.data.ContinuousLocalizer;
import nl.tudelft.exchange.student.locationtracker.filter.data.loader.BayesianFilterDataLoader;
import nl.tudelft.exchange.student.locationtracker.movement.ArrowManager;

public class LocalizationActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private RSSIBroadcastReceiver rssiBroadcastReceiver;
    private IntentFilter intentFilter = new IntentFilter();
    private BayesianFilter bayesianFilter = new BayesianFilter(BayesianFilterDataLoader.loadData("PDF.txt"));
    private boolean enabledLocalization = false;
    private boolean enabledContinuousLocalization = false;
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
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        rssiBroadcastReceiver = new RSSIBroadcastReceiver();
        registerReceiver(rssiBroadcastReceiver, intentFilter);
        wifiManager.setFrequencyBand(WifiManager.WIFI_FREQUENCY_BAND_2GHZ, false);
        wifiManager.startScan();
        initializeImageButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(rssiBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(rssiBroadcastReceiver);
    }

    public void onLocalizationClick(View v) {
        bayesianFilter.resetFilter();
        clearTheInDoorMap();
        resetVotes();
        votesCounter = 0;
        enabledLocalization = true;
        //progressDialog = ProgressDialog.show(this, "Localization process", "It may take a few seconds", true);
    }

    public void onContinuousLocalizationClick(View v) {
        if(!enabledContinuousLocalization) {
            continuousLocalizer.reset();
        }
        enabledContinuousLocalization = !enabledContinuousLocalization;
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
        //progressDialog.dismiss();
    }

    private class RSSIBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent i) {
            WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            rssiScanResultHandler(wifiManager.getScanResults());
            wifiManager.startScan();
        }

    }

    //FIXME
    private void rssiScanResultHandler(List<ScanResult> scanResults) {
        if(enabledLocalization) {
            clearTheInDoorMap();
            bayesianFilter.resetFilter();
            Pair<Integer, Double> iterationResultsFromBayesianFilter = bayesianFilter.probability(scanResults);
            updateDisplayedProbabilities();

            int localizedCellID = getResources().getIdentifier("c"+(iterationResultsFromBayesianFilter.first + 1), "id", getPackageName());
            ImageButton localizedCell = (ImageButton)findViewById(localizedCellID);
            localizedCell.setColorFilter(Color.argb(110, 255, 0, 0));
            /*
            if(iterationResultsFromBayesianFilter.second > 0.95) {
                ++votes[iterationResultsFromBayesianFilter.first];
                ++votesCounter;
                if (votesCounter == VOTE_SIZE) {
                    finalizeLocalizationProcess();
                }
            }
            */
        }
        else if(enabledContinuousLocalization) {
            clearTheInDoorMap();
            int cellIndex = continuousLocalizer.localize(scanResults);
            if(cellIndex != -1) {
                int localizedCellID = getResources().getIdentifier("c" + (cellIndex + 1), "id", getPackageName());
                ImageButton localizedCell = (ImageButton) findViewById(localizedCellID);
                localizedCell.setColorFilter(Color.argb(110, 255, 0, 0));
                if(currentCell == null) {
                    currentCell = localizedCell;
                } else if (currentCell != localizedCell) {
                    previousCell = currentCell;
                    currentCell = localizedCell;
                    ArrowManager.setArrow(this, previousCell.getTag(), currentCell.getTag());
                    //previousCell.setColorFilter(Color.argb(65, 255, 0, 0));
                }
                if(previousCell != null) {
                    //previousCell.setColorFilter(Color.argb(65, 255, 0, 0));
                }
                //Toast.makeText(LocalizationActivity.this, "Jestes w pokoju o id: C" + (cellIndex + 1), Toast.LENGTH_SHORT).show();
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
