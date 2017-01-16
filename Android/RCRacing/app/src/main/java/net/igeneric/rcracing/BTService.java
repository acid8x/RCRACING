package net.igeneric.rcracing;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BTService extends Service {
    private BluetoothManager mBluetoothManager;
    public static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeScanner mBluetoothLeScanner;
    private static BluetoothGatt mBluetoothGatt;
    private static ScanSettings settings;
    private static List<ScanFilter> filters = new ArrayList<>();
    private static boolean mConnected = false;

    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static UUID UUID_BLE_HM10_RX_TX = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_BLE_HM10_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnected = true;
                sendBroadcast(new Intent(ACTION_GATT_CONNECTED));
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnected = false;
                sendBroadcast(new Intent(ACTION_GATT_DISCONNECTED));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGatt.getService(UUID_BLE_HM10_SERVICE).getCharacteristic(UUID_BLE_HM10_RX_TX);
                mBluetoothGatt.setCharacteristicNotification(mBluetoothGattCharacteristic, true);
                mBluetoothGatt.readCharacteristic(mBluetoothGattCharacteristic);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (!MainActivity.running) {
                sendMessage("&1C0&2C0&3C0&4C0&5C0");
                MainActivity.running = true;
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            final String data = new String(characteristic.getValue());
            Log.i("TEST", "Data Received: "+data);
            final char[] dataArray = data.toCharArray();
            final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(EXTRA_DATA, dataArray);
            sendBroadcast(intent);
        }
    };

    // SERVICE BIND
    class LocalBinder extends Binder {
        BTService getService() {
            return BTService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public static void sendMessage(String str) {
        boolean sent = false;
        int retry = 0;
        if (mConnected) {
            while(!sent) {
                BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGatt.getService(UUID_BLE_HM10_SERVICE).getCharacteristic(UUID_BLE_HM10_RX_TX);
                mBluetoothGattCharacteristic.setValue(str.getBytes());
                sent = mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
                if (retry > 4) break;
                else retry++;
            }
        }
    }

    // BLUETOOTH CONNECTION
    public boolean initialize() {
        if (settings == null) if (Build.VERSION.SDK_INT >= 21) settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) return false;
        }
        if (mBluetoothAdapter == null) mBluetoothAdapter = mBluetoothManager.getAdapter();
        return mBluetoothAdapter != null;
    }

    public boolean connectTo(String address) {
        if (mBluetoothAdapter == null) return false;
        if (mBluetoothGatt != null) return mBluetoothGatt.connect();
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) return false;
        mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattCallback);
        return true;
    }

    public void scanLeDevice() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (mBluetoothLeScanner == null) mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (mBluetoothAdapter.isDiscovering()) mBluetoothAdapter.cancelDiscovery();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mConnected) {
                        if (Build.VERSION.SDK_INT >= 21) mBluetoothLeScanner.stopScan(mScanCallback);
                        else mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        scanLeDevice();
                    }
                }
            }, 10000);
            if (Build.VERSION.SDK_INT >= 21) mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
            else mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (device != null) {
                try {
                    if (device.getName().equals("RC_RACING")) {
                        connectTo(device.getAddress());
                    }
                } catch (Exception e) {
                    //
                }
            }
        }
    };

    @SuppressLint("newApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            if (result.getDevice() != null) {
                try {
                    if (result.getDevice().getName().equals("RC_RACING")) {
                        connectTo(result.getDevice().getAddress());
                    }
                } catch (Exception e) {
                    //
                }
            }
        }

    };

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) return;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}
