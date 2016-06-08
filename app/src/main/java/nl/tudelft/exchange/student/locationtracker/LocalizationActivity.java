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
import nl.tudelft.exchange.student.locationtracker.filter.data.loader.BayesianFilterDataLoader;

public class LocalizationActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private RSSIBroadcastReceiver rssiBroadcastReceiver;
    private IntentFilter intentFilter = new IntentFilter();
    private BayesianFilter bayesianFilter = new BayesianFilter(BayesianFilterDataLoader.loadData("PDF.txt"));
    private boolean enabledLocalization = false;
    private ProgressDialog progressDialog;
    private static final int VOTE_SIZE = 7;
    private int[] votes = new int[BayesianFilter.NUMBER_OF_CELLS];
    private int votesCounter;

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
        //FIXME possible changes here - no reset during localization process
        bayesianFilter.resetFilter();
        clearTheInDoorMap();
        resetVotes();
        votesCounter = 0;
        enabledLocalization = true;
        //progressDialog = ProgressDialog.show(this, "Localization process", "It may take a few seconds", true);
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
        Log.d("LT",""+votes[0]+" "+votes[1]+" "+votes[2]+" "+votes[3]);
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
            Pair<Integer, Double> iterationResultsFromBayesianFilter = bayesianFilter.probability(scanResults);
            updateDisplayedProbabilities();
            if(iterationResultsFromBayesianFilter.second > 0.95) {
                ++votes[iterationResultsFromBayesianFilter.first];
                ++votesCounter;
                if (votesCounter == VOTE_SIZE) {
                    finalizeLocalizationProcess();
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
