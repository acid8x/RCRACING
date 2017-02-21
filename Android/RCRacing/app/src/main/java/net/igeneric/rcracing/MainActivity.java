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
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
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

public class MainActivity extends Activity implements TextToSpeech.OnInitListener, View.OnClickListener {

    public static String debugString = "";
    public static boolean hasFocus = true, broadcastUpdateRegistered = false, running = false, landscape = false, connected = false, ttsInit = false, isTextToSpeech = false;
    public static int permissionsGranted = 0, activityInfo, raceType = 0, raceLapsNumber = 2, raceGatesNumber = 4, raceKillsNumber = 10, raceLivesNumber = 0;
    public static int[] layouts = {R.layout.lv_race_without_guns,R.layout.lv_race_with_guns,R.layout.lv_battle,R.layout.lv_hunting};
    public static List<Players> mPlayersList = new ArrayList<>();
    public static List<Integer> mWinnersList = new ArrayList<>();
    public static TextToSpeech tts = null;
    private Drawable dConnected, dDisconnected;
    private ListView listView, listView2 = null;
    private ImageView connectionState;
    private Button buttonDEBUG;
    private BTService mBTService = null;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBTService = null;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (!Constants.DEV_MODE) {
                mBTService = ((BTService.LocalBinder) iBinder).getService();
                if (!mBTService.initialize()) onDestroy();
                mBTService.scanLeDevice();
            }
        }
    };
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

    public static void say(String s) {
        if (isTextToSpeech) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) tts.speak(s, TextToSpeech.QUEUE_ADD, null, s);
            else tts.speak(s, TextToSpeech.QUEUE_ADD, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ACTION_REQUEST_ENABLE:
                if (resultCode == Activity.RESULT_CANCELED) onDestroy();
                break;
            case Constants.ACTION_REQUEST_SETUP:
                if (resultCode == Activity.RESULT_OK) {
                    TextView rt = (TextView) findViewById(R.id.tvRaceType);
                    String text = "";
                    if (raceType < 3) {
                        if (raceType == 2) {
                            text += "Race with guns, ";
                            if (raceLivesNumber > 0) text += raceLivesNumber + " lives each\n";
                            else text += "no lives limit\n";
                        } else text += "Race without guns\n";
                        text += "Complete " + raceLapsNumber + " laps, with " + raceGatesNumber + " gates per lap";
                    } else if (raceType == 3) {
                        text += "Search & Destroy\nFirst to make " + raceKillsNumber + " kills";
                        if (raceLivesNumber > -1) text += ", with " + raceLivesNumber + " lives each";
                    } else if (raceType == 4) {
                        text += "Hunting\nFirst to make " + raceKillsNumber + " kills";
                    }
                    rt.setText(text);
                    startService(new Intent(getBaseContext(), BTService.class));
                    updateUI();
                } else onDestroy();
                break;
            case Constants.ACTION_REQUEST_DEBUG:
                final char[] dataArray = debugString.toCharArray();
                if (dataArray.length == 3) {
                    final Intent intent = new Intent(Constants.ACTION_DATA_AVAILABLE);
                    intent.putExtra(Constants.EXTRA_DATA, dataArray);
                    sendBroadcast(intent);
                }
                break;
            case Constants.MY_DATA_CHECK_CODE:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    tts = new TextToSpeech(this, this);
                } else {
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
                ttsInit = true;
                Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                startActivityForResult(intent, Constants.ACTION_REQUEST_SETUP);
                break;
            case Constants.ACTION_REQUEST_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    int getId = data.getExtras().getInt("ID");
                    Players p = getPlayers(getId);
                    String name = "" + data.getExtras().getString("NAME");
                    if (p != null && !name.equals("")) p.setName(name);
                    updateUI();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkSelfPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            requestPermissions(permissions, Constants.ACTION_REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            switch (requestCode) {
                case Constants.ACTION_REQUEST_PERMISSION:
                    for (int results : grantResults) {
                        if (results != PackageManager.PERMISSION_GRANTED) onDestroy();
                        else permissionsGranted++;
                    }
                    break;
            }
        }
    }

    public void updateUI() {
        if (raceType == 0) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Constants.DEV_MODE) if (buttonDEBUG.getVisibility() == View.INVISIBLE) buttonDEBUG.setVisibility(View.VISIBLE);
                if (BTService.mConnected != connected) {
                    connected = BTService.mConnected;
                    if (connected) connectionState.setImageDrawable(dConnected);
                    else connectionState.setImageDrawable(dDisconnected);
                    ViewAnimator.animate(connectionState).rubber().duration(1000).repeatCount(4).start();
                }
                if (landscape && mPlayersList.size() > 3) {
                    List<Players> list1, list2;
                    if (listView2 == null) listView2 = (ListView) findViewById(R.id.listView2);
                    list1 = mPlayersList.subList(0, 3);
                    list2 = mPlayersList.subList(3, mPlayersList.size());
                    listView.setAdapter(new ConstructorListAdapter(getBaseContext(), layouts[raceType-1], list1));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Players o = (Players) parent.getItemAtPosition(position);
                            changeName(o);
                        }
                    });
                    listView2.setAdapter(new ConstructorListAdapter(getBaseContext(), layouts[raceType-1], list2));
                    listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Players o = (Players) parent.getItemAtPosition(position);
                            changeName(o);
                        }
                    });
                } else {
                    listView.setAdapter(new ConstructorListAdapter(getBaseContext(), layouts[raceType-1], mPlayersList));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Players o = (Players) parent.getItemAtPosition(position);
                            changeName(o);
                        }
                    });
                }
            }
        });
    }

    public void changeName(Players o) {
        Intent serverIntent = new Intent(MainActivity.this, DialogActivity.class);
        serverIntent.putExtra("ID", o.getId());
        serverIntent.putExtra("TYPE", 1);
        serverIntent.putExtra("TITLE", "ENTER NEW NAME");
        serverIntent.putExtra("NAME", o.getName());
        startActivityForResult(serverIntent, Constants.ACTION_REQUEST_DIALOG);
    }

    private Players getPlayers(int id) {
        for (Players players : MainActivity.mPlayersList) {
            if (players.checkId(id)) return players;
        }
        return null;
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            isTextToSpeech = true;
            tts.setLanguage(Locale.US);
            say("Welcome to R C Racing");
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int orientation = getResources().getConfiguration().orientation;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
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
        if (Build.VERSION.SDK_INT >= 21) {
            dConnected = getDrawable(R.drawable.connected);
            dDisconnected = getDrawable(R.drawable.disconnected);
        } else {
            dConnected = getResources().getDrawable(R.drawable.connected);
            dDisconnected = getResources().getDrawable(R.drawable.disconnected);
        }
        listView = (ListView) findViewById(R.id.listView);
        Collections.sort(mPlayersList);
        if (Constants.DEV_MODE) {
            buttonDEBUG = (Button) findViewById(R.id.buttonDEBUG);
            buttonDEBUG.setOnClickListener(this);
        }
        ImageView ivLogo = (ImageView) findViewById(R.id.ivLogo);
        connectionState = (ImageView) findViewById(R.id.ivBT);
        ViewAnimator.animate(ivLogo).waitForHeight().translationX(-2000, 0).alpha(0, 1).duration(1000).decelerate()
                .thenAnimate(connectionState).scale(5,1).alpha(0,1).duration(500).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasFocus = true;
        Intent intent;
        if (!Constants.DEV_MODE) if (BTService.mBluetoothAdapter == null || !BTService.mBluetoothAdapter.isEnabled()) {
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, Constants.ACTION_REQUEST_ENABLE);
        }
        if (!Constants.DEV_MODE) if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            onDestroy();
        }
        if (Build.VERSION.SDK_INT >= 23 && permissionsGranted < 2) {
            checkSelfPermissions();
        }
        if (mBTService == null) {
            intent = new Intent(getApplicationContext(), BTService.class);
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }
        if (!broadcastUpdateRegistered) {
            registerReceiver(broadcastUpdate, new IntentFilter("ACTION_UPDATE_UI"));
            broadcastUpdateRegistered = true;
        }
        if (!ttsInit) {
            intent = new Intent();
            intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(intent, Constants.MY_DATA_CHECK_CODE);
        }
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
        if (mBTService != null) {
            mBTService.disconnect();
            mBTService.close();
            this.stopService(new Intent(this, BTService.class));
            this.unbindService(mServiceConnection);
        }
        if (Build.VERSION.SDK_INT >= 21) finishAndRemoveTask();
        finish();
        System.exit(0);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonDEBUG) {
            Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
            startActivityForResult(intent, Constants.ACTION_REQUEST_DEBUG);
        }
    }
}
