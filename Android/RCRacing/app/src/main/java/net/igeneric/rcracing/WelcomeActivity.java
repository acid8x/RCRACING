package net.igeneric.rcracing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.github.florent37.viewanimator.ViewAnimator;

public class WelcomeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        setContentView(R.layout.activity_welcome);
        setResult(Activity.RESULT_CANCELED);

        final Button button = (Button) findViewById(R.id.startButton);
        ViewAnimator.animate(button).translationX(-1000, 0).alpha(0, 1).scale(0, 1).duration(1000).decelerate().start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewAnimator.animate(button).scale(1f,1.1f,1f).duration(3000).repeatCount(-1).start();
            }
        }, 1000);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.say("New Race");
                ViewAnimator.animate(button).scale(1,2,1).duration(500).thenAnimate(button).translationX(0, 1000).alpha(1, 0).scale(1, 0).duration(1000).accelerate().start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }, 1500);
            }
        });
    }
}
