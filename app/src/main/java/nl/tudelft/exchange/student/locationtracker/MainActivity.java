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
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.exchange.student.locationtracker.data.AccData;
import nl.tudelft.exchange.student.locationtracker.data.RssData;
import nl.tudelft.exchange.student.locationtracker.data.saver.AccDataSaver;
import nl.tudelft.exchange.student.locationtracker.data.saver.RssDataSaver;
import nl.tudelft.exchange.student.locationtracker.filter.BayesianFilter;
import nl.tudelft.exchange.student.locationtracker.filter.data.loader.BayesianFilterDataLoader;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

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
    private boolean enableLocalization = false;
    private BayesianFilter bayesianFilter = null;
    private int counter = 0;
    private int initialBelieveCell = 0;

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
                Toast.makeText(MainActivity.this, "RSS results have been saved!", Toast.LENGTH_LONG).show();
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

    public void onSwitchClicked(View view) {
        enableLocalization = !enableLocalization;
        if(enableLocalization) {
            bayesianFilter = new BayesianFilter(BayesianFilterDataLoader.loadData("PDF.txt"));
            counter = 0;
        }
    }

    public void onRadioButtonClicked(View view) {
        switch(view.getId()) {
            case R.id.radioButton1:
                initialBelieveCell = 0;
                Toast.makeText(MainActivity.this, "0", Toast.LENGTH_LONG).show();
                break;
            case R.id.radioButton2:
                initialBelieveCell = 1;
                Toast.makeText(MainActivity.this, "1", Toast.LENGTH_LONG).show();
                break;
            case R.id.radioButton3:
                initialBelieveCell = 2;
                Toast.makeText(MainActivity.this, "2", Toast.LENGTH_LONG).show();
                break;
            case R.id.radioButton4:
                initialBelieveCell = 3;
                Toast.makeText(MainActivity.this, "3", Toast.LENGTH_LONG).show();
                break;
        }
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {    }

    private class RSSIBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent i) {
            // Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
            WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            rssiScanResultHandler(wifiManager.getScanResults());
            wifiManager.startScan(); // relaunch scan immediately
        }
    }

    private void rssiScanResultHandler(List<ScanResult> scanResults) {
        if(enableLocalization) {
            Pair<Integer, Double> probability = bayesianFilter.probability(scanResults);
            Toast.makeText(MainActivity.this, "Cell id: " + probability.first + " P(Si|O) = "+probability.second+"! Po: " + ++counter + " iteracjach!", Toast.LENGTH_SHORT).show();
            if (probability.second > 0.9) {
                Toast.makeText(MainActivity.this, "Jestes w pokoju o id: " + probability.first, Toast.LENGTH_SHORT).show();
            }
        }
        if(enableRssScan) {
            rssDataSet.add(new RssData(System.currentTimeMillis(), scanResults));
            Toast.makeText(MainActivity.this, "Liczba skanów: "+rssDataSet.size(), Toast.LENGTH_SHORT).show();
        }
    }
}
