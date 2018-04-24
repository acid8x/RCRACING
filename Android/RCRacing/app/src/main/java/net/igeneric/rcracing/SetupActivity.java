package net.igeneric.rcracing;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.florent37.viewanimator.ViewAnimator;

public class SetupActivity extends Activity {

    private static int state, lastState, rbId;
    private NumberPicker np = null;
    private RadioGroup radioGroup = null;
    private TextView tv = null;
    private LinearLayout ll = null;
    private Button button, buttonBack;
    private Drawable on, off;
    private ToggleButton tb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        setContentView(R.layout.activity_setup);
        setResult(Activity.RESULT_CANCELED);

        if (Build.VERSION.SDK_INT < 22) {
            on = this.getResources().getDrawable(R.drawable.gunactivated);
            off = this.getResources().getDrawable(R.drawable.gundesactivated);
        }
        else {
            on = this.getDrawable(R.drawable.gunactivated);
            off = this.getDrawable(R.drawable.gundesactivated);
        }

        tv = (TextView) findViewById(R.id.tv);
        button = (Button) findViewById(R.id.button);
        buttonBack = (Button) findViewById(R.id.buttonBack);
        tb = (ToggleButton) findViewById(R.id.tb);
        tb.setText("");
        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s;
                if (tb.isChecked()) {
                    tb.setBackground(on);
                    s = "YES";
                    MainActivity.raceType = 2;
                }
                else {
                    tb.setBackground(off);
                    s = "NO";
                    MainActivity.raceType = 1;
                }
                tb.setText("");
                MainActivity.say(s);
                s = "WEAPONS ? " + s;
                tv.setText(s);
            }
        });

        lastState = 0;
        state = 0;

        np = (NumberPicker) findViewById(R.id.np);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        ll = (LinearLayout) findViewById(R.id.ll);

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
            if (value.equals("WEAPONS ")) text = "ACTIVATE WEAPONS ?";
            else text = "HOW MANY " + value + "?";
        } else {
            if (MainActivity.raceType > 0) {
                radioGroup.check(rbId);
            }
            text = "SELECT GAME TYPE";
        }
        if (state != -1) MainActivity.say(text);
        if (value.equals("WEAPONS ")) {
            text = "WEAPONS ? ";
            if (tb.isChecked()) text += "YES";
            else text += "NO";
        }
        tv.setText(text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastState = state;
                String tts;
                switch (value) {
                    case "TYPE ":
                        rbId = radioGroup.getCheckedRadioButtonId();
                        RadioButton rb = (RadioButton) findViewById(rbId);
                        tts = rb.getText().toString();
                        break;
                    case "WEAPONS ":
                        tts = "WEAPONS ";
                        if (!tb.isChecked()) {
                            tts += "DE";
                            MainActivity.raceType = 1;
                        } else MainActivity.raceType = 2;
                        tts += "ACTIVATED";
                        break;
                    default:
                        tts = "" + np.getValue() + " " + value;
                        break;
                }
                if (value.equals("LIVES ") && np.getValue() == 0) tts = "NO LIVES LIMIT ";
                MainActivity.say(tts);
                switch (value) {
                    case "TYPE ":
                        int id = radioGroup.getCheckedRadioButtonId();
                        if (id == R.id.rbl) {
                            state = 1;
                            MainActivity.raceType = 1;
                        }
                        else state = 4;
                        if (id == R.id.rb2) MainActivity.raceType = 3;
                        else if (id == R.id.rb3) MainActivity.raceType = 4;
                        break;
                    case "WEAPONS ":
                        state = 2;
                        break;
                    case "LAPS ":
                        MainActivity.raceLapsNumber = np.getValue();
                        state = 3;
                        break;
                    case "GATES ":
                        MainActivity.raceGatesNumber = np.getValue();
                        if (MainActivity.raceType != 1) state = 5;
                        else state = 6;
                        break;
                    case "KILLS ":
                        MainActivity.raceKillsNumber = np.getValue();
                        state = 5;
                        if (MainActivity.raceType == 4) state = 6;
                        break;
                    case "LIVES ":
                        MainActivity.raceLivesNumber = np.getValue();
                        state = 6;
                        break;
                }
                if (MainActivity.raceType != 0) {
                    ViewAnimator
                            .animate(ll).translationY(0, 500).scale(1, 0).alpha(1, 0).duration(500)
                            .andAnimate(buttonBack).scale(1,0).alpha(1, 0).duration(500)
                            .andAnimate(view).translationX(0, -500).scale(1,6).alpha(1,0).duration(500)
                            .thenAnimate(buttonBack).scale(0,1).duration(0)
                            .thenAnimate(view).translationX(-500, 0).scale(6,1).duration(0)
                            .start();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getViewState(state);
                        }
                    }, 550);
                }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (value) {
                    case "TYPE ":
                        lastState = -1;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setResult(Activity.RESULT_CANCELED);
                                finish();
                            }
                        }, 500);
                        break;
                    case "WEAPONS ":
                        lastState = 0;
                        break;
                    case "LAPS ":
                        lastState = 1;
                        break;
                    case "GATES ":
                        lastState = 2;
                        break;
                    case "KILLS ":
                        lastState = 0;
                        break;
                }
                state = lastState;
                ViewAnimator
                        .animate(ll).translationY(0, 500).scale(1, 0).alpha(1, 0).duration(500)
                        .andAnimate(button).scale(1,0).alpha(1, 0).duration(500)
                        .andAnimate(view).translationX(0, 500).scale(1,6).alpha(1,0).duration(500)
                        .thenAnimate(button).scale(0,1).duration(0)
                        .thenAnimate(view).translationX(500, 0).scale(6,1).duration(0)
                        .start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getViewState(lastState);
                    }
                }, 550);
            }
        });

        getView(ll);
    }

    private void getViewState(int id) {
        if (id == 0) {
            radioGroup.setVisibility(View.VISIBLE);
            tb.setVisibility(View.GONE);
            np.setVisibility(View.GONE);
        } else if (id == 1) {
            radioGroup.setVisibility(View.GONE);
            tb.setVisibility(View.VISIBLE);
            np.setVisibility(View.GONE);
        } else {
            radioGroup.setVisibility(View.GONE);
            tb.setVisibility(View.GONE);
            np.setVisibility(View.VISIBLE);
        }
        switch (id) {
            case 0:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("TYPE ",0,0,true);
                    }
                }, 500);
                break;
            case 1:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("WEAPONS ",0,0,false);
                    }
                }, 500);
                break;
            case 2:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("LAPS ", 1, 20,false);
                    }
                }, 500);
                break;
            case 3:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("GATES ", 2, 9,false);
                    }
                }, 500);
                break;
            case 4:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("KILLS ", 1, 50,false);
                    }
                }, 500);
                break;
            case 5:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numberPicker("LIVES ", 0, 20,false);
                    }
                }, 500);
                break;
            case 6:
                setResult(Activity.RESULT_OK);
                finish();
                break;
        }
    }

    private void getView(final View view) {
        view.setAlpha(0);
        ViewAnimator.animate(view).translationY(-500, 0).scale(0, 1).alpha(0, 1).duration(500).andAnimate(button).alpha(0, 1).duration(500).andAnimate(buttonBack).alpha(0, 1).duration(500).start();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}