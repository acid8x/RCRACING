package net.igeneric.rcracing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogActivity extends Activity implements View.OnClickListener{

    private int Type, Id;
    private EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        setContentView(R.layout.activity_dialog);
        setResult(Activity.RESULT_CANCELED);
        TextView title = (TextView) findViewById(R.id.dialogTitle);
        TextView message = (TextView) findViewById(R.id.dialogMessage);
        editText = (EditText) findViewById(R.id.dialogEditText);
        Button bl = (Button) findViewById(R.id.buttonLEFT);
        Button br = (Button) findViewById(R.id.buttonRIGHT);
        br.setOnClickListener(this);
        if (bundle != null) {
            Type = bundle.getInt("TYPE");
            if (bundle.containsKey("MESSAGE")) {
                String Message = bundle.getString("MESSAGE");
                message.setText(Message);
            }
            else message.setVisibility(View.GONE);
            title.setText(bundle.getString("TITLE"));
            switch (Type) {
                case 0: //exit
                    editText.setVisibility(View.GONE);
                    bl.setVisibility(View.GONE);
                    break;
                case 1: //change name
                    String Name = bundle.getString("NAME");
                    Id = bundle.getInt("ID");
                    editText.setHint(Name);
                    editText.setOnKeyListener(new View.OnKeyListener() {
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER) || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                                Intent intent = new Intent();
                                intent.putExtra("ID", Id);
                                intent.putExtra("NAME", editText.getText().toString());
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                                return true;
                            }
                            return false;
                        }
                    });
                    bl.setOnClickListener(this);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLEFT:
                finish();
                break;
            case R.id.buttonRIGHT:
                if (Type == 0) ;
                else {
                    Intent intent = new Intent();
                    intent.putExtra("ID", Id);
                    intent.putExtra("NAME", editText.getText().toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }
}