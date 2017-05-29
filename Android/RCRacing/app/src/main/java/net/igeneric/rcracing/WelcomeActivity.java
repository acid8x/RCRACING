package net.igeneric.rcracing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;

public class WelcomeActivity extends Activity {

    private TextView[] tv = new TextView[4];
    private int[] ids = { R.id.w2, R.id.w1, R.id.w3, R.id.w4, R.id.wiv1, R.id.wiv2, R.id.wiv3 };
    private ImageView[] iv = new ImageView[3];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        for (int i=0;i<tv.length;i++) tv[i] = (TextView) findViewById(ids[i]);
        for (int j=0;j<iv.length;j++) iv[j] = (ImageView) findViewById(ids[tv.length+j]);
        Button button = (Button) findViewById(R.id.buttonStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
        ViewAnimator.animate(tv[0]).translationX(-1000,-10).translationY(-10).alpha(0,1).scale(0,0.3f).decelerate().duration(1000)
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
                .thenAnimate(iv[0]).translationX(-1000,0).alpha(0,1).scale(0.5f).duration(666)
                .andAnimate(iv[1]).translationX(1000,0).alpha(0,1).scale(0.5f).duration(666)
                .thenAnimate(iv[2]).alpha(0,1).scale(0.5f).duration(1500)
                .thenAnimate(iv[0]).scale(1f).duration(250)
                .andAnimate(iv[1]).scale(1f).duration(250)
                .andAnimate(iv[2]).scale(1f).duration(250)
                .thenAnimate(iv[0]).bounce().duration(750)
                .andAnimate(iv[1]).bounce().duration(750)
                .andAnimate(iv[2]).bounce().duration(750)
                .thenAnimate(button).alpha(0,1).duration(1000)
                .thenAnimate(button).scale(1,2,1).duration(2000).repeatCount(-1)
                .start();
    }
}
