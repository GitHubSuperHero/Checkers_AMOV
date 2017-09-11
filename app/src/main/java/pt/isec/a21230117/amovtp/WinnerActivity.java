package pt.isec.a21230117.amovtp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class WinnerActivity extends AppCompatActivity {
    Player winner, loser;
    TextView winnerName, winnerResult, loserName, loserResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);
        Intent i = getIntent();
        if (i != null) {
            winner = (Player) i.getSerializableExtra("winner");
            loser = (Player) i.getSerializableExtra("loser");
        }
        setText();
    }

    void setText(){
        winnerName = (TextView) findViewById(R.id.winnerName);
        winnerResult = (TextView) findViewById(R.id.winnerResult);
        loserName = (TextView) findViewById(R.id.loserName);
        loserResult = (TextView) findViewById(R.id.loserResult);

        winnerName.setText(winner.getName());
        winnerResult.setText(""+winner.getPontos());
        loserName.setText(loser.getName());
        loserResult.setText(""+loser.getPontos());
    }

    public void confirma(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
