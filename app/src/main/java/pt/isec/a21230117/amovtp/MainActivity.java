package pt.isec.a21230117.amovtp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static pt.isec.a21230117.amovtp.GameCPUActivity.FROM_MAIN;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void createOfflineGame(View v)
    {
        Intent intent = new Intent(this, GameSettingsActivity.class);
        startActivity(intent);
    }

    public void createCPUGame(View v)
    {
        Player one = new Player("Leonardo", 2);
        Intent intent = new Intent(this, GameCPUActivity.class);
        intent.putExtra("from", FROM_MAIN);
        intent.putExtra("player", one);
        startActivity(intent);
    }

    public void createOnlineGame(View v)
    {
        Player one = new Player("Leonardo");
        Intent intent = new Intent(this, GameOnlineActivity.class);
        intent.putExtra("player", one);
        startActivity(intent);
    }

    public void joinOnlineGame(View v)
    {
        Player two = new Player("Tomás");
        Intent intent = new Intent(this, GameOnlineActivityClient.class);
        intent.putExtra("player", two);
        startActivity(intent);
    }


}
