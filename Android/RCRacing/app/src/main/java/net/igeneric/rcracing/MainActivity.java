package net.igeneric.rcracing;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final int ACTION_REQUEST_PERMISSION = 0;
    private static final int ACTION_REQUEST_ENABLE = 1;

    public static boolean hasFocus = true, broadcastUpdateRegistered = false, running = false;
    public static int permissionsGranted = 0;
    public static int raceType = 1, raceLapsNumber = 2, raceGatesNumber = 4, raceKillsNumber = 10;
    public static List<Players> mPlayersList = new ArrayList<>();
    public static List<Integer> mWinnersList = new ArrayList<>();

    public ListView listView;

    private BTService mBTService = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBTService = null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBTService = ((BTService.LocalBinder) iBinder).getService();
            if (!mBTService.initialize()) exit("Unable to initialize Bluetooth LE");
            mBTService.scanLeDevice();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_REQUEST_ENABLE:
                if (resultCode == Activity.RESULT_CANCELED) exit("Bluetooth must be enable");
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // PERMISSIONS CHECK
    private void checkSelfPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            requestPermissions(permissions, ACTION_REQUEST_PERMISSION);
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) exit("Device does not support Bluetooth LE");
        startService(new Intent(getBaseContext(), BTService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case ACTION_REQUEST_PERMISSION:
                    for (int results : grantResults) {
                        if (results != PackageManager.PERMISSION_GRANTED) exit("Permission is needed");
                        else permissionsGranted++;
                    }
                    break;
            }
        }
    }

    // CUSTOM FUNCTIONS
    private void exit(String msg) {
        if (msg != null) Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
        if (mBTService != null) {
            mBTService.disconnect();
            mBTService.close();
        }
        this.stopService(new Intent(this, BTService.class));
        this.unbindService(mServiceConnection);
        finish();
    }

    public void updateUI() {
        Collections.sort(mPlayersList);
        listView.setAdapter( new ConstructorListAdapter(getBaseContext(), R.layout.listview_row_item, mPlayersList ) );
    }

    // ANDROID MAIN EVENTS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int orientation = getResources().getConfiguration().orientation; // 1 PORTRAIT, 2 LANDSCAPE
        int rotation = getWindowManager().getDefaultDisplay().getRotation(); // 0 0, 1 90, 2 180, 3 270
        if (orientation == 2 && rotation < 2) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else if (orientation == 1 && rotation < 2) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else if (orientation == 2 && rotation > 1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        else if (orientation == 1 && rotation > 1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkSelfPermissions();
        listView = (ListView) findViewById( R.id.listView);
        Collections.sort(mPlayersList);
        listView.setAdapter( new ConstructorListAdapter(getBaseContext(), R.layout.listview_row_item, mPlayersList ) );
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasFocus = true;
        if (BTService.mBluetoothAdapter == null || !BTService.mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ACTION_REQUEST_ENABLE);
        }
        if (mBTService == null) {
            if (Build.VERSION.SDK_INT >= 23 && permissionsGranted < 2) return;
            Intent intent = new Intent(getApplicationContext(), BTService.class);
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }
        registerReceiver(broadcastUpdate, new IntentFilter("ACTION_UPDATE_UI"));
        broadcastUpdateRegistered = true;
        if (running) updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hasFocus = false;
        if (broadcastUpdateRegistered) {
            unregisterReceiver(broadcastUpdate);
            broadcastUpdateRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        exit(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    private BroadcastReceiver broadcastUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case "ACTION_UPDATE_UI":
                    updateUI();
                    break;
            }
        }
    };
}
