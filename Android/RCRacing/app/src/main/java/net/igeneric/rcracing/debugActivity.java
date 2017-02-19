package net.igeneric.rcracing;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class DebugActivity extends Activity implements View.OnClickListener{

    private Button b1,b2,b3,b4,b5,b6,b7,b8;
    private static int state = 0;
    private LinearLayout ll;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        setContentView(R.layout.activity_debug);
        setResult(Activity.RESULT_CANCELED);
        state = 0;
        MainActivity.debugString = "";
        b1 = (Button) findViewById(R.id.button1);
        b1.setOnClickListener(this);
        b2 = (Button) findViewById(R.id.button2);
        b2.setOnClickListener(this);
        b3 = (Button) findViewById(R.id.button3);
        b3.setOnClickListener(this);
        b4 = (Button) findViewById(R.id.button4);
        b4.setOnClickListener(this);
        b5 = (Button) findViewById(R.id.button5);
        b5.setOnClickListener(this);
        b6 = (Button) findViewById(R.id.button6);
        b6.setOnClickListener(this);
        b7 = (Button) findViewById(R.id.button7);
        b7.setOnClickListener(this);
        b8 = (Button) findViewById(R.id.button8);
        b8.setOnClickListener(this);
        ll = (LinearLayout) findViewById(R.id.ll2);
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) findViewById(v.getId());
        MainActivity.debugString += button.getText().toString();
        if (state == 0) {
            b1.setText("D");
            b2.setText("G");
            b3.setText("K");
            b4.setText("Z");
            ll.setVisibility(View.GONE);
        }
        else if (state == 1) {
            b1.setText("1");
            b2.setText("2");
            b3.setText("3");
            b4.setText("4");
            ll.setVisibility(View.VISIBLE);
        }
        else {
            setResult(Activity.RESULT_OK);
            finish();
        }
        state++;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
