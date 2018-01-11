package antimonypidgey.untangle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Angus on 9/28/2015.
 */
public class GameMenu extends AppCompatActivity implements View.OnClickListener{

    EditText nodeCountTextField;
    EditText connectionsTextField;
    EditText seedTextField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        setContentView(ll);
        TextView nodeCountText = new TextView(this);
        nodeCountText.setText("Node Count");
        nodeCountTextField = new EditText(this);
        nodeCountTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
        nodeCountTextField.setText("18");
        TextView connectionsText = new TextView(this);
        connectionsText.setText("Connection Maximum");
        connectionsTextField = new EditText(this);
        connectionsTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
        connectionsTextField.setText("6");
        TextView seedText = new TextView(this);
        seedText.setText("Random Seed (leave blank for random)");
        seedTextField = new EditText(this);
        seedTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
        Button startButton = new Button(this);
        startButton.setText("Begin");
        startButton.setOnClickListener(this);
        ll.addView(nodeCountText);
        ll.addView(nodeCountTextField);
        ll.addView(connectionsText);
        ll.addView(connectionsTextField);
        ll.addView(seedText);
        ll.addView(seedTextField);
        ll.addView(startButton);
    }

    public void onClick(View v) {
        Intent activityChangeIntent = new Intent(GameMenu.this, GameActivity.class);
        activityChangeIntent.putExtra("EXTRA_NODE_COUNT", Integer.parseInt(nodeCountTextField.getText().toString()));
        activityChangeIntent.putExtra("EXTRA_CONNECTION_MAX", Integer.parseInt(connectionsTextField.getText().toString()));
        if (seedTextField.getText().toString().trim().equals(""))
            activityChangeIntent.putExtra("EXTRA_SEED", new Random().nextInt());
        else
            activityChangeIntent.putExtra("EXTRA_SEED", Integer.parseInt(seedTextField.getText().toString()));
        GameMenu.this.startActivity(activityChangeIntent);
    }
}
