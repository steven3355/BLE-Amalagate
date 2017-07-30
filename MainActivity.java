package test.research.sjsu.bleamalagate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    ParcelUuid mServiceUUID = ParcelUuid.fromString("00001830-0000-1000-8000-00805F9B34FB");
    ParcelUuid mServiceDataUUID = ParcelUuid.fromString("00009208-0000-1000-8000-00805F9B34FB");

    //Scanner
    BluetoothLeScanner mBluetoothLeScanner;
    ScanFilter mScanFilter;
    ScanFilter.Builder mScanFilterBuilder = new ScanFilter.Builder();
    ScanSettings mScanSettings;
    ScanSettings.Builder mScanSettingBuilder = new ScanSettings.Builder();
    List<ScanFilter> FilterList = new ArrayList<>();
    Button startScanningButton;
    Button stopScanningButton;
    TextView BeaconsReceived;
    long time = Long.MAX_VALUE;

    //Advertiser
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    AdvertiseData mAdvertiseData;
    AdvertiseData.Builder mAdvertiseDataBuilder;
    AdvertiseSettings mAdvertiseSettings;
    AdvertiseSettings.Builder mAdvertiseSettingBuilder = new AdvertiseSettings.Builder();
    Button BroadcastButton;
    Button StopBroadcastButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BLESetUp();
        PrepareAdvertiseSettings();
        PrepareAdvertiseData("Moshi,Moshi");
        PrepareScanFilter();
        PrepareScanSetting();
        PreparePeripherals();
    }
    public void BLESetUp(){
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }
    public void PrepareAdvertiseData(String str){
        mAdvertiseDataBuilder = new AdvertiseData.Builder();
        mAdvertiseDataBuilder.addServiceData(mServiceDataUUID,str.getBytes());
        mAdvertiseDataBuilder.setIncludeDeviceName(true);
        mAdvertiseDataBuilder.setIncludeTxPowerLevel(true);
        mAdvertiseDataBuilder.addServiceUuid(mServiceUUID);
        mAdvertiseData= mAdvertiseDataBuilder.build();
    }
    public void PrepareAdvertiseSettings(){
        mAdvertiseSettingBuilder.setAdvertiseMode(1);
        mAdvertiseSettingBuilder.setTimeout(0);
        mAdvertiseSettingBuilder.setTxPowerLevel(3);
        mAdvertiseSettingBuilder.setConnectable(true);
        mAdvertiseSettings = mAdvertiseSettingBuilder.build();
    }
    public void PrepareScanFilter(){
        mServiceUUID = ParcelUuid.fromString("00001830-0000-1000-8000-00805F9B34FB");
        mScanFilterBuilder.setServiceUuid(mServiceUUID);
        mScanFilter = mScanFilterBuilder.build();
        FilterList.add(mScanFilter);
    }
    public void PrepareScanSetting(){
        mScanSettingBuilder.setScanMode(1);
        mScanSettings = mScanSettingBuilder.build();
    }
    public void PreparePeripherals(){
        BeaconsReceived= (TextView) findViewById(R.id.BeaconsReceived);
        BroadcastButton = (Button) findViewById(R.id.BroadcastButton);
        BroadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAdvertise();
            }
        });
        StopBroadcastButton = (Button) findViewById(R.id.StopBroadcastButton);
        StopBroadcastButton.setVisibility(View.INVISIBLE);
        StopBroadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAdvertise();
            }
        });

        startScanningButton = (Button) findViewById(R.id.startScanningButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.stopScanningButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);
    }

    public void startAdvertise(){
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings,mAdvertiseData,mAdvertiseCallback);
        BroadcastButton.setVisibility(View.INVISIBLE);
        StopBroadcastButton.setVisibility(View.VISIBLE);
    }
    public void stopAdvertise(){
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        BroadcastButton.setVisibility(View.VISIBLE);
        StopBroadcastButton.setVisibility(View.INVISIBLE);
    }
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);

        }
    };
    public void startScanning() {
        System.out.println("start scanning");
        BeaconsReceived.setText("");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.startScan(FilterList, mScanSettings, leScanCallback);
            }
        });
    }

    public void stopScanning() {
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(leScanCallback);
            }
        });
    }
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String name=result.getDevice().getName();
            time = Math.min(time, result.getTimestampNanos());
            String data= new String(result.getScanRecord().getServiceData(mServiceDataUUID));
            BeaconsReceived.setText(
                    "Device Name = " + name +
                    "\nrssi = " + result.getRssi() +
                    "\nAddress = " + result.getDevice().getAddress() +
                    "\nTime Stamp = " + result.getTimestampNanos() +
                    "\nTime Elapsed  = "+ (result.getTimestampNanos()-time)/1000000000 +
                    "\nService Data = " + data );
        }
    };
}
