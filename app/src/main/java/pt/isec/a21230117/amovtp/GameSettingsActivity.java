package pt.isec.a21230117.amovtp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import static pt.isec.a21230117.amovtp.GameCPUActivity.FROM_MAIN;

public class GameSettingsActivity extends AppCompatActivity {
    Player one, two;
    Button confirm;
    EditText etNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamesettings);

        one = new Player("Leonardo");
        two = new Player();

        confirm = (Button) findViewById(R.id.btConfrim);
        etNome = (EditText) findViewById(R.id.etName);

        confirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startNextActivity();
            }
        });
    }

    void startNextActivity(){
        two.setName(etNome.getText().toString());
        if(one.getColor() != 0 && two.getColor() != 0 && !two.getName().isEmpty()){
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("from", FROM_MAIN);
            intent.putExtra("one", one);
            intent.putExtra("two", two);
            startActivity(intent);
            finish();
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.nooption), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void chooseSide(View v) {
        if(v.getId() == getResources().getIdentifier("player_one_black", "id", getPackageName())){
            one.setColor(1);
            two.setColor(2);
            v.setBackgroundColor(Color.argb(170, 255, 255, 0));
            LinearLayout v2 = (LinearLayout) findViewById(getResources().getIdentifier("player_one_white", "id", getPackageName()));
            v2.setBackgroundColor(Color.TRANSPARENT);
        }

        if(v.getId() == getResources().getIdentifier("player_one_white", "id", getPackageName())){
            one.setColor(2);
            two.setColor(1);
            v.setBackgroundColor(Color.argb(170, 255, 255, 0));
            LinearLayout v2 = (LinearLayout) findViewById(getResources().getIdentifier("player_one_black", "id", getPackageName()));
            v2.setBackgroundColor(Color.TRANSPARENT);
        }
    }


}
