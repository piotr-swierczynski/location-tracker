package nl.tudelft.exchange.student.locationtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.exchange.student.locationtracker.data.AccData;
import nl.tudelft.exchange.student.locationtracker.data.RssData;
import nl.tudelft.exchange.student.locationtracker.data.saver.AccDataSaver;
import nl.tudelft.exchange.student.locationtracker.data.saver.RssDataSaver;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //TEST COMMIT
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private WifiManager wifiManager;
    private RSSIBroadcastReceiver rssiBroadcastReceiver;
    private IntentFilter intentFilter = new IntentFilter();
    private List<RssData> rssDataSet = new ArrayList<>();
    private List<AccData> accDataSet = new ArrayList<>();
    private Button saveAcc;
    private Button saveRss;
    private Button startScanAcc;
    private Button startScanRss;
    private boolean enableAccScan = false;
    private boolean enableRssScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        saveAcc = (Button) findViewById(R.id.button1);
        saveRss = (Button) findViewById(R.id.button2);
        startScanAcc = (Button) findViewById(R.id.button3);
        startScanRss = (Button) findViewById(R.id.button4);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d("LT", "No accelerometer!!!");
        }
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        rssiBroadcastReceiver = new RSSIBroadcastReceiver();
        registerReceiver(rssiBroadcastReceiver, intentFilter);
        wifiManager.startScan();

        saveAcc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
                try {
                    enableRssScan = false;
                    new RssDataSaver().save(rssDataSet, MainActivity.this);
                    rssDataSet = new ArrayList<>();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        startScanAcc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enableAccScan = true;
            }
        });

        startScanRss.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
        registerReceiver(rssiBroadcastReceiver, intentFilter);
    }

    // onPause() unregister the accelerometer for stop listening the events
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        unregisterReceiver(rssiBroadcastReceiver);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(enableAccScan)
            accDataSet.add(new AccData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class RSSIBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent i) {
            // Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
            WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            rssiScanResultHandler(wifiManager.getScanResults()); // your method to handle Scan results
            wifiManager.startScan(); // relaunch scan immediately
        }
    }

    private void rssiScanResultHandler(List<ScanResult> scanResults) {
        Log.d("LT",1+"");
        if(enableRssScan) {
            Log.d("LT",2+"");
            rssDataSet.add(new RssData(System.currentTimeMillis(), scanResults));
        }
    }
}
