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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private static final int ACTION_REQUEST_PERMISSION = 0;
    private static final int ACTION_REQUEST_ENABLE = 1;
    private static final int ACTION_REQUEST_RACETYPE = 2;
    private static final int ACTION_REQUEST_LAPS = 3;
    private static final int ACTION_REQUEST_GATES = 4;
    private static final int ACTION_REQUEST_KILLS = 5;
    private static final int ACTION_REQUEST_LIVES = 6;

    public static boolean hasFocus = true, broadcastUpdateRegistered = false, running = false, landscape = false, started = false, setup = false;
    public static int permissionsGranted = 0;
    public static int raceType = 1, raceLapsNumber = 1, raceGatesNumber = 1, raceKillsNumber = 1, raceLivesNumber = 0;
    public static List<Players> mPlayersList = new ArrayList<>();
    public static List<Integer> mWinnersList = new ArrayList<>();
    public static int activityInfo;

    private ListView listView, listView2 = null;
    private List<Players> list1, list2;

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
            case ACTION_REQUEST_RACETYPE:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent;
                    if (MainActivity.raceType < 3) {
                        intent = new Intent(getApplicationContext(), LapsActivity.class);
                        startActivityForResult(intent, ACTION_REQUEST_LAPS);
                    } else {
                        intent = new Intent(getApplicationContext(), KillsActivity.class);
                        startActivityForResult(intent, ACTION_REQUEST_KILLS);
                    }
                }  else {
                    setup = false;
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
                break;
            case ACTION_REQUEST_LAPS:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), GatesActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_GATES);
                } else {
                    Intent intent = new Intent(getApplicationContext(), RaceTypeActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_RACETYPE);
                }
                break;
            case ACTION_REQUEST_GATES:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), LivesActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_LIVES);
                } else {
                    Intent intent = new Intent(getApplicationContext(), LapsActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_LAPS);
                }
                break;
            case ACTION_REQUEST_KILLS:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), LivesActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_LIVES);
                } else {
                    Intent intent = new Intent(getApplicationContext(), RaceTypeActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_RACETYPE);
                }
                break;
            case ACTION_REQUEST_LIVES:
                if (resultCode == Activity.RESULT_OK) {
                    TextView rt = (TextView) findViewById(R.id.tvRaceType);
                    if (raceType < 3) {
                        boolean gun = false;
                        if (raceType == 2) gun = true;
                        rt.setText("(RACE) GUNS: " + gun + ", LAPS: " + raceLapsNumber + ", GATES: " + raceGatesNumber + ", LIVES: " + raceLivesNumber);
                    } else if (raceType == 3) {
                        rt.setText("(BATTLE) KILLS: " + raceKillsNumber + ", LIVES: " + raceLivesNumber);
                    }
                    startService(new Intent(getBaseContext(), BTService.class));
                } else {
                    Intent intent;
                    if (MainActivity.raceType < 3) {
                        intent = new Intent(getApplicationContext(), GatesActivity.class);
                        startActivityForResult(intent, ACTION_REQUEST_GATES);
                    } else {
                        intent = new Intent(getApplicationContext(), KillsActivity.class);
                        startActivityForResult(intent, ACTION_REQUEST_KILLS);
                    }
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
        else finish();
    }

    public void updateUI() {
        Collections.sort(mPlayersList);
        if (landscape && mPlayersList.size() > 3) {
            if (listView2 == null) {
                listView2 = (ListView) findViewById(R.id.listView2);
                list1 = new ArrayList<>();
                list2 = new ArrayList<>();
            }
            list1 = mPlayersList.subList(0, 3);
            list2 = mPlayersList.subList(3, list1.size());
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
        listView = (ListView) findViewById( R.id.listView);
        Collections.sort(mPlayersList);
        if (landscape && mPlayersList.size() > 3) {
            if (listView2 == null) {
                listView2 = (ListView) findViewById(R.id.listView2);
                list1 = new ArrayList<>();
                list2 = new ArrayList<>();
            }
            list1 = mPlayersList.subList(0, 3);
            list2 = mPlayersList.subList(3, list1.size());
            listView.setAdapter( new ConstructorListAdapter(getBaseContext(), R.layout.listview_row_item, list1 ) );
            listView2.setAdapter( new ConstructorListAdapter(getBaseContext(), R.layout.listview_row_item, list2 ) );
        } else {
            listView.setAdapter( new ConstructorListAdapter(getBaseContext(), R.layout.listview_row_item, mPlayersList ) );
        }
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
                intent = new Intent(getApplicationContext(), RaceTypeActivity.class);
                startActivityForResult(intent, ACTION_REQUEST_RACETYPE);
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
