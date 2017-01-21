package net.igeneric.rcracing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.florent37.viewanimator.ViewAnimator;

public class SetupActivity extends Activity {

    private static int state = 0, lastState; // 0 racetype, 1 laps, 2 gates, 3 kills
    private NumberPicker np = null;
    private RadioGroup radioGroup = null;
    private TextView tv = null;
    private RelativeLayout ll = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        setContentView(R.layout.activity_setup);
        setResult(Activity.RESULT_CANCELED);

        np = (NumberPicker) findViewById(R.id.np);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        tv = (TextView) findViewById(R.id.tv);
        ll = (RelativeLayout) findViewById(R.id.ll);

        getViewState(state);
    }

    private void numberPicker(final String value, int min, int max, final boolean radio) {
        String text;
        if (!radio) {
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
            text = "HOW MANY " + value + "?";
        } else {
            if (MainActivity.raceType > 0) {
                radioGroup.check(MainActivity.raceType-1);
            }
            text = "SELECT RACE TYPE";
        }
        tv.setText(text);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewAnimator
                        .animate(view)
                        .translationX(0, -75)
                        .translationX(-75, 0)
                        .repeatCount(3)
                        .duration(300)
                        .start();
                lastState = state;
                switch (value) {
                    case "RACETYPE ":
                        int id = radioGroup.getCheckedRadioButtonId();
                        if (id == R.id.rbl) MainActivity.raceType = 1;
                        else if (id == R.id.rb2) MainActivity.raceType = 2;
                        else if (id == R.id.rb3) MainActivity.raceType = 3;
                        state = MainActivity.raceType;
                        if (state == 2) state = 1;
                        break;
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
                ViewAnimator.animate(ll).translationY(0, 300).scale(1, 0).alpha(1, 0).duration(500).start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getViewState(state);
                    }
                }, 400);
            }
        });

        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewAnimator
                        .animate(view)
                        .translationX(0, 75)
                        .translationX(75, 0)
                        .repeatCount(3)
                        .duration(300)
                        .start();
                switch (value) {
                    case "RACETYPE ":
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                        break;
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
                ViewAnimator.animate(ll).translationY(0, 300).scale(1, 0).alpha(1, 0).duration(500).start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getViewState(lastState);
                    }
                }, 400);
            }
        });

        getView(ll);
    }

    private void getViewState(int id) {
        if (id == 0 && radioGroup.getVisibility() == View.GONE) {
            radioGroup.setVisibility(View.VISIBLE);
            np.setVisibility(View.GONE);
        } else if (np.getVisibility() == View.GONE) {
            radioGroup.setVisibility(View.GONE);
            np.setVisibility(View.VISIBLE);
        }
        switch (id) {
            case 0:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("RACETYPE ",0,0,true);
                    }
                }, 500);
                break;
            case 1:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("LAPS ", 1, 20,false);
                    }
                }, 500);
                break;
            case 2:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("GATES ", 2, 9,false);
                    }
                }, 500);
                break;
            case 3:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("KILLS ", 1, 50,false);
                    }
                }, 500);
                break;
            case 4:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("LIVES ", 0, 20,false);
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
        view.setAlpha(0);
        ViewAnimator.animate(view).translationY(-300, 0).scale(0, 1).alpha(0, 1).duration(500).start();
    }
}