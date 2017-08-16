package test.research.sjsu.bleamalagate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
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
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //API
    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    ParcelUuid mServiceUUID = ParcelUuid.fromString("00001830-0000-1000-8000-00805F9B34FB");
    ParcelUuid mServiceDataUUID = ParcelUuid.fromString("00009208-0000-1000-8000-00805F9B34FB");
    UUID mServiceUUID2 = UUID.fromString("00001830-0000-1000-8000-00805F9B34FB");
    UUID mCharUUID = UUID.fromString("00003000-0000-1000-8000-00805F9B34FB");

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
    BluetoothDevice mBluetoothDevice;
    String address;

    //Advertiser
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    AdvertiseData mAdvertiseData;
    AdvertiseData.Builder mAdvertiseDataBuilder;
    AdvertiseSettings mAdvertiseSettings;
    AdvertiseSettings.Builder mAdvertiseSettingBuilder = new AdvertiseSettings.Builder();
    Button BroadcastButton;
    Button StopBroadcastButton;

    //Gatt
    Boolean Connected = false;
    BluetoothGatt mBluetoothGatt;
    BluetoothGattServer mBluetoothGattServer;
    BluetoothGattService mBluetoothGattService;
    BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    Button Connect;
    Button Disconnect;
    Button Read;
    //BluetoothDevice ConnectDevice;
    TextView ConnectionState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BLESetUp();
        PrepareAdvertiseSettings();
        PrepareAdvertiseData("World!");
        PrepareScanFilter();
        PrepareScanSetting();
        PreparePeripherals();
        startServer();
    }
    public void startServer(){
        mBluetoothGattServer = mBluetoothManager.openGattServer(this, mBluetoothGattServerCallback);
        mBluetoothGattService = new BluetoothGattService(mServiceUUID2, 0);
        mBluetoothGattCharacteristic = new BluetoothGattCharacteristic(mCharUUID,BluetoothGattCharacteristic.PROPERTY_READ,BluetoothGattCharacteristic.PERMISSION_READ);
        //mBluetoothGattCharacteristic.setValue("I'MSOMECHARACTERISTIC".getBytes());
        mBluetoothGattService.addCharacteristic(mBluetoothGattCharacteristic);
        mBluetoothGattServer.addService(mBluetoothGattService);
    }
    private BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            char CharDataArr[] = new char[500];
            Arrays.fill(CharDataArr, 'A');
            String CharData = new String("Do not go gentle into that good night,\n" +
                    "Old age should burn and rave at close of day;\n" +
                    "Rage, rage against the dying of the light.\n" +
                    "Though wise men at their end know dark is right,\n" +
                    "Because their words had forked no lightning they\n" +
                    "Do not go gentle into that good night.\n");
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0 , CharData.getBytes());
        }
    };
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

        ConnectionState = (TextView) findViewById(R.id.ConnectionState);
        ConnectionState.setMovementMethod(new ScrollingMovementMethod());
        Connect = (Button) findViewById(R.id.Connect);
        Connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                EstablishConnection();
                Connect.setVisibility(View.INVISIBLE);
                Disconnect.setVisibility(View.VISIBLE);
                Read.setVisibility(View.VISIBLE);
            }
        });

        Disconnect = (Button) findViewById(R.id.Disconnect);
        Disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                DisableConnection();
                Connect.setVisibility(View.VISIBLE);
                Disconnect.setVisibility(View.INVISIBLE);
            }
        });
        Disconnect.setVisibility(View.INVISIBLE);

        Read = (Button) findViewById(R.id.Read);
        Read.setVisibility(View.INVISIBLE);
        Read.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                if(mBluetoothGatt.getService(mServiceUUID2)!=null);
                    mBluetoothGatt.readCharacteristic(mBluetoothGatt.getService(mServiceUUID2).getCharacteristic(mCharUUID));
                    ConnectionState.setText("Getting Characteristic");
            }
        });
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(newState == BluetoothGatt.STATE_CONNECTED){
                Connected = true;
                mBluetoothGatt.discoverServices();
                ConnectionState.setText("Connected and Discovering Services");
            }
            else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                Connected = false;
                ConnectionState.setText("Disconnected");
                Connect.setVisibility(View.VISIBLE);
                Disconnect.setVisibility(View.INVISIBLE);
            }

        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String Char = new String(characteristic.getValue());
            System.out.println(characteristic.getStringValue(0));
            ConnectionState.setText(Char);
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //mBluetoothGatt.getServices();
            gatt.requestMtu(500);
            System.out.println(status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                ConnectionState.setText("Serivce Discovered and Reading Characteristic");
            }
        }
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGatt.readCharacteristic(mBluetoothGatt.getService(mServiceUUID2).getCharacteristic(mCharUUID));
                System.out.println("MTU is changed");
            }
        }
    };

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
            String data = "No Data";
            mBluetoothDevice = result.getDevice();
            if(result.getScanRecord().getServiceData(mServiceDataUUID)!=null) {
                data = new String(result.getScanRecord().getServiceData(mServiceDataUUID));
            }
            BeaconsReceived.setText(
                    "Device Name = " + name +
                    "\nrssi = " + result.getRssi() +
                    "\nAddress = " + result.getDevice().getAddress() +
                    "\nTime Stamp = " + result.getTimestampNanos() +
                    "\nTime Elapsed  = "+ (result.getTimestampNanos()-time)/1000000000 +
                    "\nService Data = " + data);
            address = result.getDevice().getAddress();
            if(!Connected)
                EstablishConnection();
        }
    };
    private void EstablishConnection(){
        final BluetoothDevice ConnectDevice = mBluetoothAdapter.getRemoteDevice(address);
        ConnectionState.setText("Connecting to " + ConnectDevice.getName());
        mBluetoothGatt = ConnectDevice.connectGatt(this, false, mGattCallback);
    }
    private void DisableConnection(){
        mBluetoothGatt.disconnect();
    }
}