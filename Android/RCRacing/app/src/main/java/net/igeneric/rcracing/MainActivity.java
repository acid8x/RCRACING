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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final int ACTION_REQUEST_PERMISSION = 0;
    private static final int ACTION_REQUEST_ENABLE = 1;
    private static final int ACTION_REQUEST_SETUP = 2;

    public static boolean hasFocus = true, broadcastUpdateRegistered = false, running = false, landscape = false, started = false, setup = false, connected = false;
    public static int permissionsGranted = 0, activityInfo, raceType = 0, raceLapsNumber = 2, raceGatesNumber = 4, raceKillsNumber = 10, raceLivesNumber = 0;
    public static List<Players> mPlayersList = new ArrayList<>();
    public static List<Integer> mWinnersList = new ArrayList<>();
    private Drawable dConnected, dDisconnected;
    private ListView listView, listView2 = null;
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
            case ACTION_REQUEST_SETUP:
                if (resultCode == Activity.RESULT_OK) {
                    TextView rt = (TextView) findViewById(R.id.tvRaceType);
                    String text = "";
                    if (raceType < 3) {
                        if (raceType == 2) {
                            text += "Race with guns, ";
                            if (raceLivesNumber > 0) text += raceLivesNumber + " lives each\n";
                            else text += "no lives limit\n";
                        }
                        else text += "Race without guns\n";
                        text += "Complete " + raceLapsNumber + " laps, with " + raceGatesNumber + " gates per lap";
                    } else if (raceType == 3) {
                        text += "Search & Destroy\nFirst to make " + raceKillsNumber + " kills";
                        if (raceLivesNumber > 0) text += ", with " + raceLivesNumber + " lives each";
                    }
                    rt.setText(text);
                    startService(new Intent(getBaseContext(), BTService.class));
                    updateUI();
                } else {
                    Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_SETUP);
                }
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
        started = true;
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
        if (started) {
            this.stopService(new Intent(this, BTService.class));
            this.unbindService(mServiceConnection);
        }
        if (Build.VERSION.SDK_INT >= 21) finishAndRemoveTask();
        finish();
        System.exit(0);
    }

    public void updateUI() {
        if (BTService.mConnected != connected) {
            connected = BTService.mConnected;
            ImageView connectionState = (ImageView) findViewById(R.id .connectionState);
            if (connected) connectionState.setImageDrawable(dConnected);
            else connectionState.setImageDrawable(dDisconnected);
        }
        Collections.sort(mPlayersList);
        if (landscape && mPlayersList.size() > 3) {
            List<Players> list1, list2;
            if (listView2 == null) listView2 = (ListView) findViewById(R.id.listView2);
            list1 = mPlayersList.subList(0, 3);
            list2 = mPlayersList.subList(3, mPlayersList.size());
            listView.setAdapter(new ConstructorListAdapter(getBaseContext(), R.layout.listview_row_item, list1));
            listView2.setAdapter(new ConstructorListAdapter(getBaseContext(), R.layout.listview_row_item, list2));
        } else {
            listView.setAdapter(new ConstructorListAdapter(getBaseContext(), R.layout.listview_row_item, mPlayersList));
        }
    }

    // ANDROID MAIN EVENTS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int orientation = getResources().getConfiguration().orientation; // 1 PORTRAIT, 2 LANDSCAPE
        int rotation = getWindowManager().getDefaultDisplay().getRotation(); // 0 0, 1 90, 2 180, 3 270
        if (orientation == 2) {
            landscape = true;
            if (rotation < 2) activityInfo = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            if (rotation > 1) activityInfo = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else if (orientation == 1) {
            landscape = false;
            if (rotation < 2) activityInfo = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            if (rotation > 1) activityInfo = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
        setRequestedOrientation(activityInfo);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkSelfPermissions();
        if (Build.VERSION.SDK_INT >= 21) {
            dConnected = getDrawable(R.drawable.connected);
            dDisconnected = getDrawable(R.drawable.disconnected);
        }
        else {
            dConnected = getResources().getDrawable(R.drawable.connected);
            dDisconnected = getResources().getDrawable(R.drawable.disconnected);
        }
        listView = (ListView) findViewById( R.id.listView);
        Collections.sort(mPlayersList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasFocus = true;
        Intent intent;
        if (BTService.mBluetoothAdapter == null || !BTService.mBluetoothAdapter.isEnabled()) {
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ACTION_REQUEST_ENABLE);
        }
        if (started) {
            if (mBTService == null) {
                if (Build.VERSION.SDK_INT >= 23 && permissionsGranted < 2) return;
                intent = new Intent(getApplicationContext(), BTService.class);
                bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            }
            registerReceiver(broadcastUpdate, new IntentFilter("ACTION_UPDATE_UI"));
            broadcastUpdateRegistered = true;
            if (running) updateUI();
            if (!setup) {
                setup = true;
                intent = new Intent(getApplicationContext(), SetupActivity.class);
                startActivityForResult(intent, ACTION_REQUEST_SETUP);
            }
        }
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
