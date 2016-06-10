package nl.tudelft.exchange.student.locationtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.exchange.student.locationtracker.data.AccData;
import nl.tudelft.exchange.student.locationtracker.data.RssData;
import nl.tudelft.exchange.student.locationtracker.data.receiver.RSSIBroadcastReceiver;
import nl.tudelft.exchange.student.locationtracker.data.receiver.RSSIBroadcastReceiverInitializer;
import nl.tudelft.exchange.student.locationtracker.data.receiver.RSSIScanResultHandler;
import nl.tudelft.exchange.student.locationtracker.data.saver.AccDataSaver;
import nl.tudelft.exchange.student.locationtracker.data.saver.RssDataSaver;

public class MainActivity extends AppCompatActivity implements SensorEventListener, RSSIScanResultHandler{

    private Pair<RSSIBroadcastReceiver, IntentFilter> broadcastReceiverPair;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private List<RssData> rssDataSet = new ArrayList<>();
    private List<AccData> accDataSet = new ArrayList<>();
    private Button saveAcc;
    private Button saveRss;
    private Button startScanAcc;
    private Button startScanRss;
    private Button localizeBtn;
    private boolean enableAccScan = false;
    private boolean enableRssScan = false;
    private String filename = "filename";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        saveAcc = (Button) findViewById(R.id.button1);
        saveRss = (Button) findViewById(R.id.button2);
        startScanAcc = (Button) findViewById(R.id.button3);
        startScanRss = (Button) findViewById(R.id.button4);
        localizeBtn = (Button) findViewById(R.id.localize);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d("LT", "No accelerometer!!!");
        }

        broadcastReceiverPair = RSSIBroadcastReceiverInitializer.initialize(this);
        localizeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LocalizationActivity.class));
            }
        });

        saveAcc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "ACC results have been saved!", Toast.LENGTH_LONG).show();
                try {
                    enableAccScan = false;
                    new AccDataSaver().save(accDataSet, MainActivity.this);
                    accDataSet = new ArrayList<>();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        saveRss.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enableRssScan = false;
                saveRSSDataWithDialogPrompt();
            }
        });

        startScanAcc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "ACC Scaning begins!", Toast.LENGTH_LONG).show();
                enableAccScan = true;
            }
        });

        startScanRss.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "RSS Scaning begins!", Toast.LENGTH_LONG).show();
                enableRssScan = true;
            }
        });

    }

    // onResume() register the accelerometer for listening the events
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        registerReceiver(broadcastReceiverPair.first, broadcastReceiverPair.second);
    }

    // onPause() unregister the accelerometer for stop listening the events
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        unregisterReceiver(broadcastReceiverPair.first);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(enableAccScan)
            accDataSet.add(new AccData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {    }

    @Override
    public void handleScanResults(List<ScanResult> scanResults) {
        if(enableRssScan) {
            rssDataSet.add(new RssData(System.currentTimeMillis(), scanResults));
            Toast.makeText(MainActivity.this, "Number of preformed scans: "+rssDataSet.size(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRSSDataWithDialogPrompt() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptsView = layoutInflater.inflate(R.layout.filename_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        filename = userInput.getText().toString();
                        Toast.makeText(MainActivity.this, "RSS results have been saved! --> " + filename + ".txt", Toast.LENGTH_LONG).show();
                        try {
                            new RssDataSaver().save(rssDataSet, filename, MainActivity.this);
                            rssDataSet = new ArrayList<>();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        alertDialogBuilder.create().show();
    }
}
