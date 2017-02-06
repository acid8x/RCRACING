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

        Button button = (Button) findViewById(R.id.startButton);

        ViewAnimator.animate(button).scale(0, 1).alpha(0, 1).duration(700).start();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.say("New Race");
                ViewAnimator.animate(view).scale(1, 0).alpha(1, 0).duration(500).start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }, 500);
            }
        });
    }

}
