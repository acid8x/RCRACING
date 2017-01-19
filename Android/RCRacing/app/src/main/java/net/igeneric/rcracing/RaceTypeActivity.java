package net.igeneric.rcracing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.github.florent37.viewanimator.ViewAnimator;

public class RaceTypeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        setContentView(R.layout.activity_racetype);
        setResult(Activity.RESULT_CANCELED);

        int[] radioButtons = { R.id.radioButton, R.id.radioButton2, R.id.radioButton3 };
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        if (MainActivity.raceType > 0) radioGroup.check(radioButtons[MainActivity.raceType-1]);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == R.id.radioButton) MainActivity.raceType = 1;
                else if (i == R.id.radioButton2) MainActivity.raceType = 2;
                else if (i == R.id.radioButton3) MainActivity.raceType = 3;
            }
        });

        Button buttonBack = (Button) findViewById(R.id.buttonReturnRaceType);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.selRaceType);
                ViewAnimator
                        .animate(relativeLayout)
                        .translationY(0,-200)
                        .alpha(1,0)
                        .duration(500)
                        .start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }
                },500);
            }
        });

        Button buttonNext = (Button) findViewById(R.id.buttonNextRaceType);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.selRaceType);
                ViewAnimator
                        .animate(relativeLayout)
                        .translationY(0,-200)
                        .alpha(1,0)
                        .duration(500)
                        .start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                },500);
            }
        });

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.selRaceType);
        ViewAnimator
                .animate(relativeLayout)
                .translationY(200,0)
                .alpha(0,1)
                .duration(500)
                .start();
    }
}
