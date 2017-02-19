package net.igeneric.rcracing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogActivity extends Activity implements View.OnClickListener{

    private int Type;
    private String Name, Message = "";
    private TextView title, message;
    private EditText editText;
    private Button bl, br;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(MainActivity.activityInfo);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        setContentView(R.layout.activity_dialog);
        setResult(Activity.RESULT_CANCELED);
        title = (TextView) findViewById(R.id.dialogTitle);
        message = (TextView) findViewById(R.id.dialogMessage);
        editText = (EditText) findViewById(R.id.dialogEditText);
        bl = (Button) findViewById(R.id.buttonLEFT);
        br = (Button) findViewById(R.id.buttonRIGHT);
        br.setText(R.string.ok);
        br.setOnClickListener(this);
        if (bundle != null) {
            Type = bundle.getInt("TYPE");
            if (bundle.containsKey("MESSAGE")) {
                Message = bundle.getString("MESSAGE");
                message.setText(Message);
            }
            else message.setVisibility(View.GONE);
            title.setText(bundle.getString("TITLE"));
            switch (Type) {
                case 0: //exit
                    editText.setVisibility(View.GONE);
                    bl.setVisibility(View.GONE);
                    break;
                case 1: //question
                    editText.setVisibility(View.GONE);
                    bl.setOnClickListener(this);
                    bl.setText(R.string.cancel);
                    break;
                case 2: //change name
                    Name = bundle.getString("NAME");
                    editText.setHint(Name);
                    bl.setOnClickListener(this);
                    bl.setText(R.string.cancel);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {

    }
}

/*

listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Players o = (Players) parent.getItemAtPosition(position);
        int Id = o.getId();
        String Name = o.getName();
        Intent serverIntent = new Intent(MainActivity.this, PlayerEditorActivity.class);
        serverIntent.putExtra("ID", Id);
        serverIntent.putExtra("NAME", Name);
        startActivityForResult(serverIntent, REQUEST_PLAYER_EDITOR);

super.onCreate(savedInstanceState);
Intent intent = getIntent();
Bundle bundle = intent.getExtras();
if (bundle != null) {
    Id = bundle.getInt("ID");
    Name = bundle.getString("NAME");
}
setContentView(R.layout.activity_player_editor);
setResult(PlayerEditorActivity.RESULT_CANCELED);

*/