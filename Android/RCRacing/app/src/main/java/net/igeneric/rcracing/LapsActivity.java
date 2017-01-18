package net.igeneric.rcracing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.florent37.viewanimator.ViewAnimator;

public class LapsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        setContentView(R.layout.activity_laps);
        setResult(Activity.RESULT_CANCELED);

        TextView tv = (TextView) findViewById(R.id.tvLaps);
        String text = "" + MainActivity.raceLapsNumber;
        tv.setText(text);

        SeekBar seekBarLaps = (SeekBar) findViewById(R.id.seekBarLaps);
        seekBarLaps.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MainActivity.raceLapsNumber = i;
                TextView tv = (TextView) findViewById(R.id.tvLaps);
                String text = "" + MainActivity.raceLapsNumber;
                tv.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button buttonBack = (Button) findViewById(R.id.buttonReturnLaps);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.selLaps);
                ViewAnimator
                        .animate(relativeLayout)
                        .translationY(0, -200)
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

        Button buttonNext = (Button) findViewById(R.id.buttonNextLaps);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.selLaps);
                ViewAnimator
                        .animate(relativeLayout)
                        .translationY(0, -200)
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

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.selLaps);
        ViewAnimator
                .animate(relativeLayout)
                .translationY(-200, 0)
                .alpha(0,1)
                .duration(500)
                .start();
    }
}
