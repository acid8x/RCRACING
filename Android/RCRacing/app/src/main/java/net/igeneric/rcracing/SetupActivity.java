package net.igeneric.rcracing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.florent37.viewanimator.ViewAnimator;

public class SetupActivity extends Activity {

    private Spinner spinner;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private String[] list = {"Race only, no weapons", "Race, weapons enabled", "Krush, Kill \'N' Destroy","Select Race Type"};
    private boolean spinnerOpened = false;
    private static int state = 0, lastState; // 0 racetype, 1 laps, 2 gates, 3 kills

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        setContentView(R.layout.activity_setup);
        setResult(Activity.RESULT_CANCELED);

        getViewState(state);
    }

    private void spinnerRaceType() {
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, list);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(3);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                list[3] = "";
                spinnerArrayAdapter.notifyDataSetChanged();
                spinnerOpened = true;
                return false;
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnerOpened) {
                    spinnerOpened = false;
                    MainActivity.raceType = i+1;
                }
                if (MainActivity.raceType > 0) {
                    if (MainActivity.raceType < 3) state = 1;
                    else state = 3;
                    removeView(spinner);
                    getViewState(state);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getView(spinner);
    }

    private void numberPicker(final String value, int min, int max) {
        final LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
        final NumberPicker np = (NumberPicker) findViewById(R.id.np);
        np.setMinValue(min);
        np.setMaxValue(max);
        switch (value) {
            case "LAPS ":
                np.setValue(MainActivity.raceLapsNumber);
                break;
            case "GATES ":
                np.setValue(MainActivity.raceGatesNumber);
                break;
            case "KILLS ":
                np.setValue(MainActivity.raceKillsNumber);
                break;
            case "LIVES ":
                np.setValue(MainActivity.raceLivesNumber);
                break;
        }
        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText(value);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastState = state;
                switch (value) {
                    case "LAPS ":
                        MainActivity.raceLapsNumber = np.getValue();
                        state = 2;
                        break;
                    case "GATES ":
                        MainActivity.raceGatesNumber = np.getValue();
                        state = 4;
                        break;
                    case "KILLS ":
                        MainActivity.raceKillsNumber = np.getValue();
                        state = 4;
                        break;
                    case "LIVES ":
                        MainActivity.raceLivesNumber = np.getValue();
                        state = 5;
                        break;
                }
                removeView(ll);
                getViewState(state);
            }
        });

        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (value) {
                    case "LAPS ":
                        lastState = 0;
                        break;
                    case "GATES ":
                        lastState = 1;
                        break;
                    case "KILLS ":
                        lastState = 0;
                        break;
                }
                state = lastState;
                removeView(ll);
                getViewState(lastState);
            }
        });

        getView(ll);
    }

    private void getViewState(int id) {
        switch (id) {
            case 0:
                spinnerRaceType();
                break;
            case 1:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("LAPS ", 1, 20);
                    }
                }, 500);
                break;
            case 2:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("GATES ", 2, 9);
                    }
                }, 500);
                break;
            case 3:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("KILLS ", 1, 50);
                    }
                }, 500);
                break;
            case 4:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("LIVES ", 0, 20);
                    }
                }, 500);
                break;
            case 5:
                setResult(Activity.RESULT_OK);
                finish();
                break;
        }
    }

    private void getView(final View view) {
        view.setVisibility(View.VISIBLE);
        ViewAnimator.animate(view).translationY(-300, 0).scale(0, 1).alpha(0, 1).duration(500).start();
    }

    private void removeView(final View view) {
        ViewAnimator.animate(view).translationY(0, 300).scale(1, 0).alpha(1, 0).duration(500).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        }, 500);
    }
}