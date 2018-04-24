package net.igeneric.rcracing;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.viewanimator.ViewAnimator;

public class WelcomeActivity extends Activity {

    private RelativeLayout welcomePage;
    private TextView[] tv = new TextView[4];
    private int[] ids = { R.id.w2, R.id.w1, R.id.w3, R.id.w4, R.id.wiv1, R.id.wiv2, R.id.wiv3 };
    private ImageView[] iv = new ImageView[3];
    private DisplayMetrics metrics;
    private float scale = 1f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        for (int i=0;i<tv.length;i++) tv[i] = findViewById(ids[i]);
        for (int j=0;j<iv.length;j++) iv[j] = findViewById(ids[tv.length+j]);
        welcomePage = findViewById(R.id.welcomePage);
        final Button button = findViewById(R.id.buttonStart);
        ViewTreeObserver vto = button.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                button.getViewTreeObserver().removeOnPreDrawListener(this);
                float ivWidth = button.getMeasuredWidth();
                float scWidth = metrics.widthPixels;
                scale = (scWidth/ivWidth)/10;
                float by = button.getY();
                float y = (metrics.heightPixels - by)*scale;
                button.setY(metrics.heightPixels-y);
                welcomePage.setScaleX(scale);
                welcomePage.setScaleY(scale);
                return true;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
        ViewAnimator.animate(welcomePage).scale(scale*1.1f).duration(100)
                .thenAnimate(tv[0]).translationX(-1000,-10).translationY(-10).alpha(0,1).scale(0,0.3f).decelerate().duration(1000)
                .thenAnimate(tv[1]).translationX(1000,10).alpha(0,1).scale(0,1).accelerate().duration(777)
                .thenAnimate(tv[0]).translationX(-10,-1000).duration(333).thenAnimate(tv[0]).duration(2000)
                .thenAnimate(tv[0]).translationX(-1000,-10).scale(0.3f,2f).duration(333)
                .thenAnimate(tv[1]).translationX(10,100,-10,10).duration(333)
                .thenAnimate(tv[0]).scale(2,1).duration(666)
                .thenAnimate(tv[2]).alpha(0,1).duration(1000)
                .thenAnimate(tv[0]).duration(1000)
                .thenAnimate(tv[0]).alpha(1,0).duration(1000)
                .andAnimate(tv[1]).alpha(1,0).duration(1000)
                .andAnimate(tv[2]).alpha(1,0)
                .andAnimate(tv[3]).alpha(0,0.1f,0.3f,1f).duration(3000)
                .thenAnimate(tv[3]).duration(1000)
                .thenAnimate(tv[3]).alpha(1,0).duration(1000)
                .thenAnimate(tv[3]).duration(1000)
                .thenAnimate(iv[0]).translationX(-1000,0).alpha(0,1).scale(0.25f).duration(666)
                .andAnimate(iv[1]).translationX(1000,0).alpha(0,1).scale(0.25f).duration(666)
                .thenAnimate(iv[2]).alpha(0,1).scale(0.25f).duration(1500)
                .thenAnimate(welcomePage).scale(4f).duration(250)
                .thenAnimate(iv[0]).bounce().duration(750)
                .andAnimate(iv[1]).bounce().duration(750)
                .andAnimate(iv[2]).bounce().duration(750)
                .thenAnimate(button).translationY(0).scale(0.25f).duration(100)
                .thenAnimate(button).translationY(100).alpha(0,1).duration(1000)
                .andAnimate(iv[0]).translationY(-50).duration(1000)
                .andAnimate(iv[1]).translationY(-50).duration(1000)
                .andAnimate(iv[2]).translationY(-50).duration(1000)
                .thenAnimate(button).scale(0.2f,0.4f,0.2f).duration(2000).repeatCount(-1)
                .start();
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
