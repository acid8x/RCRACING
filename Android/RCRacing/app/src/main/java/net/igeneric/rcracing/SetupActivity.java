package net.igeneric.rcracing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
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
        MainActivity.say(text);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.tts.isSpeaking()) return;
                ViewAnimator
                        .animate(view)
                        .translationX(0, -150)
                        .duration(150)
                        .translationX(-150, 0)
                        .duration(150)
                        .start();
                lastState = state;
                String tts = "";
                if (value.equals("RACETYPE ")) {
                    RadioButton rb = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
                    tts = rb.getText().toString();
                }
                else tts = "" + np.getValue() + " " + value;
                if (value.equals("LIVES ") && np.getValue() == 0) tts = "NO LIVES LIMIT ";
                MainActivity.say(tts);
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
                if (MainActivity.raceType != 0) {
                    ViewAnimator.animate(ll).translationY(0, 500).scale(1, 0).alpha(1, 0).duration(500).start();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getViewState(state);
                        }
                    }, 500);
                }
            }
        });

        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.tts.isSpeaking()) return;
                ViewAnimator
                        .animate(view)
                        .translationX(0, 150)
                        .duration(150)
                        .translationX(150, 0)
                        .duration(150)
                        .start();
                switch (value) {
                    case "RACETYPE ":
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setResult(Activity.RESULT_CANCELED);
                                finish();
                            }
                        }, 500);
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
                ViewAnimator.animate(ll).translationY(0, 500).scale(1, 0).alpha(1, 0).duration(500).start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getViewState(lastState);
                    }
                }, 500);
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
        ViewAnimator.animate(view).translationY(-500, 0).scale(0, 1).alpha(0, 1).duration(500).start();
    }
}