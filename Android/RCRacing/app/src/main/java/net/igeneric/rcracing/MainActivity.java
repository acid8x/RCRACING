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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.viewanimator.ViewAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final int ACTION_REQUEST_PERMISSION = 0;
    private static final int ACTION_REQUEST_ENABLE = 1;
    private static final int ACTION_REQUEST_WELCOME = 2;
    private static final int ACTION_REQUEST_SETUP = 3;
    private static final int MY_DATA_CHECK_CODE = 4;

    public static boolean hasFocus = true, broadcastUpdateRegistered = false, running = false, landscape = false, started = false, connected = false, raceStarting = false, setupOpened = true, isTextToSpeech = false;
    public static int permissionsGranted = 0, activityInfo, raceType = 0, raceLapsNumber = 2, raceGatesNumber = 4, raceKillsNumber = 10, raceLivesNumber = 0;
    public static List<Players> mPlayersList = new ArrayList<>();
    public static List<Integer> mWinnersList = new ArrayList<>();
    private Drawable dConnected, dDisconnected;
    private ListView listView, listView2 = null;
    private BTService mBTService = null;
    public static TextToSpeech tts = null;
    private Button countdown;

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
            case ACTION_REQUEST_WELCOME:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_SETUP);
                } else {
                    exit("Quit RC Racing successfully!");
                }
                break;
            case ACTION_REQUEST_SETUP:
                setupOpened = false;
                if (resultCode == Activity.RESULT_OK) {
                    BTService.stopAll();
                    raceStarting = true;
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
                        if (raceLivesNumber > -1) text += ", with " + raceLivesNumber + " lives each";
                    }
                    rt.setText(text);
                    startService(new Intent(getBaseContext(), BTService.class));
                    updateUI();
                } else {
                    setupOpened = true;
                    BTService.restoreAll();
                    raceStarting = false;
                    Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_WELCOME);
                }
                break;
            case MY_DATA_CHECK_CODE:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    tts = new TextToSpeech(this, this);
                } else {
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (BTService.mConnected != connected) {
                    connected = BTService.mConnected;
                    ImageView connectionState = (ImageView) findViewById(R.id.connectionState);
                    if (connected) connectionState.setImageDrawable(dConnected);
                    else connectionState.setImageDrawable(dDisconnected);
                    ViewAnimator.animate(connectionState).scale(1,0,1,2,1,0,1,2,1).duration(2000).start();
                }
                if (connected && countdown.isClickable() && !setupOpened) countdown.setAlpha(1);
                if (!setupOpened) {
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
            }
        });
    }

    public static void say(String s) {
        if (isTextToSpeech) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) tts.speak(s, TextToSpeech.QUEUE_ADD, null, s);
            else tts.speak(s, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            isTextToSpeech = true;
            tts.setLanguage(Locale.US);
            say("Welcome to R C Racing");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                    startActivityForResult(intent, ACTION_REQUEST_WELCOME);
                }
            },2000);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
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
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        final TextView tvCountdown = (TextView) findViewById(R.id.textViewCountdown);
        checkSelfPermissions();
        if (Build.VERSION.SDK_INT >= 21) {
            dConnected = getDrawable(R.drawable.connected);
            dDisconnected = getDrawable(R.drawable.disconnected);
        }
        else {
            dConnected = getResources().getDrawable(R.drawable.connected);
            dDisconnected = getResources().getDrawable(R.drawable.disconnected);
        }
        countdown = (Button) findViewById(R.id.countdownButton);
        countdown.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 countdown.setClickable(false);
                 countdown.setAlpha(0);
                 say("Ladies and gentleman. Start your engine!");
                 new CountDownTimer(11000, 1000) {
                     public void onTick(long millisUntilFinished) {
                         int i = (int) (millisUntilFinished / 1000);
                         i--;
                         String s = "";
                         if (i != 0) s += i;
                         else s = "GO";
                         if (i < 6) say(s);
                         tvCountdown.setText(s);
                         if (i != 0) ViewAnimator.animate(tvCountdown).scale(0, 3).alpha(0, 1).decelerate().duration(333).thenAnimate(tvCountdown).scale(3, 0).alpha(1, 0).accelerate().duration(333).start();
                         else ViewAnimator.animate(tvCountdown).scale(0, 3).alpha(0, 1).duration(100).thenAnimate(tvCountdown).scale(3, 0).alpha(1, 0).duration(3000).start();
                     }

                     public void onFinish() {
                         raceStarting = false;
                         BTService.restoreAll();
                     }
                 }.start();
             }
        });
        Button start = (Button) findViewById(R.id.menuIcon);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raceType = 0;
                raceStarting = false;
                setupOpened = true;
                BTService.clearRaceType();
                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                startActivityForResult(intent, ACTION_REQUEST_WELCOME);
            }
        });
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
