package net.igeneric.rcracing;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

public class debugActivity extends Activity implements View.OnClickListener{

    private Button b1,b2,b3,b4,b5;
    private static int state = 0;

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
            b5.setVisibility(View.GONE);
        }
        else if (state == 1) {
            b1.setText("0");
            b2.setText("1");
            b3.setVisibility(View.GONE);
            b4.setVisibility(View.GONE);
            b5.setVisibility(View.GONE);
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
