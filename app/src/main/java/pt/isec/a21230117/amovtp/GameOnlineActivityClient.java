package pt.isec.a21230117.amovtp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static pt.isec.a21230117.amovtp.GameCPUActivity.FROM_GAME_ONLINE_TWO;

public class GameOnlineActivityClient extends AppCompatActivity {
    private static final int PORT = 8899;
    private static final int PORTaux = 9988;
    private final int PRETA = 1;
    private final int BRANCA = 2;
    private final int TMAX = 8;
    private final int TMIN = 0;
    private final int DPRETA = 3;
    private final int DBRANCA = 4;

    int[][] board; //0 vazio, 1 preto, 2 branco, 3 rainha preta, 4 rainha branca

    ProgressDialog pd = null;

    ServerSocket serverSocket=null;
    Socket socketGame = null;
    ObjectInputStream input;
    ObjectOutputStream output;
    Handler procMsg = null;

    Pos move = null;
    ImageView[][] boardView;
    TextView nome1, nome2, pontos1, pontos2;

    int deX, deY, paraX, paraY, vezJogar = 2, fim, cpu;

    List<Pos> obriga;

    Player one, two, winner, loser;
    Pos pos;
    Board serverBoard;

    boolean mao, segundaJogada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        deX = 0;
        deY = 0;
        paraX = 0;
        paraY = 0;
        fim = 0;
        cpu = 0;
        mao = false;
        segundaJogada = false;
        obriga = new ArrayList<>();
        one = new Player(getString(R.string.playerone));
        two = new Player(getString(R.string.playertwo));
        procMsg = new Handler();
        pos = null;
        serverBoard = null;
        initiateBoardView();
        setBoard();
        clickListeners();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, R.string.error_netconn, Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        Intent i = getIntent();
        if (i != null) {
            two = (Player) i.getSerializableExtra("player");
        }
        setPlayerInfo();
        representBoard();
        clientDlg();
    }

    void setPlayerInfo(){
        nome1 = (TextView) findViewById(R.id.nomeJogador1);
        nome2 = (TextView) findViewById(R.id.nomeJogador2);
        pontos1 = (TextView) findViewById(R.id.peoesJogador1);
        pontos2 = (TextView) findViewById(R.id.peoesJogador2);
        nome1.setText(one.getName());
        nome2.setText(two.getName());
        pontos1.append(" "+ one.getPontos());
        pontos2.append(" "+ two.getPontos());
        setTextViewListeners();
    }

    void clickListeners() {
        int add = 0;
        for (int x = 0; x < 8; x++) {
            if (x == 1 || x == 3 || x == 5 || x == 7) add = 1;
            for (int y = 0 + add; y < 8; y += 2) {
                add = 0;
                boardView[x][y].setOnClickListener(new cliqueQuadrado(x, y));
            }
        }
    }


    boolean verificaObrigatoria(int x, int y, int cor) {
        if (x - 2 >= 0 && y - 2 >= 0 && (cor == 2 || cor == 4)) {
            if (board[x - 2][y - 2] == 0 && (board[x - 1][y - 1] == 1 || board[x - 1][y - 1] == 3)) {
                paraX = x - 2;
                paraY = y - 2;
                return true;
            }
        }
        if (x - 2 >= 0 && y + 2 <= 7 && (cor == 2 || cor == 4)) {
            if (board[x - 2][y + 2] == 0 && (board[x - 1][y + 1] == 1 || board[x - 1][y + 1] == 3)) {
                paraX = x - 2;
                paraY = y + 2;
                return true;
            }
        }

        if (x + 2 <= 7 && y - 2 >= 0 && (cor == 1 || cor == 3)) {
            if (board[x + 2][y - 2] == 0 && (board[x + 1][y - 1] == 2 || board[x + 1][y - 1] == 4)) {
                paraX = x + 2;
                paraY = y - 2;
                return true;
            }
        }
        if (x + 2 <= 7 && y + 2 <= 7 && (cor == 1 || cor == 3)) {
            if (board[x + 2][y + 2] == 0 && (board[x + 1][y + 1] == 2 || board[x + 1][y + 1] == 4)) {
                paraX = x + 2;
                paraY = y + 2;
                return true;
            }
        }
        return false;
    }

    boolean selecionaObrigatorio(int x, int y, int cor) {
        procuraObrigatorio(cor);
        if (obriga.isEmpty()) {
            return true;
        }
        for (Pos a : obriga) {
            if (a.cor == cor && a.getX() == x && a.getY() == y) {
                limpaObriga();
                return true;
            }
        }
        limpaObriga();
        return false;
    }

    void limpaObriga() {
        obriga.clear();
    }

    void procuraObrigatorio(int cor) {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == cor && verificaObrigatoria(i, j, cor)) {
                    obriga.add(new Pos(i, j, cor));
                }
            }
    }

    void seleciona(View view, int x, int y){
        if(board[x][y] == BRANCA || board[x][y] == DBRANCA) {
            if(vezJogar == BRANCA) {
                ImageView iv = (ImageView) view;
                iv.setBackgroundColor(Color.BLUE);
                mao = true;
            }
        }
    }


    void setBoard(){
        int add = 0;
        board = new int[8][8];
        for(int x = 0; x < 8; x++)
            for(int y = 0; y < 8; y++) {
                board[x][y] =  0;
            }

        for(int x = 0; x < 3; x++) {
            if(x==1) add = 1;
            for (int y = 0 + add; y < 8; y += 2) {
                add = 0;
                board[x][y] = 1;
            }
        }

        for(int x = 5; x < 8; x++) {
            if (x == 6) add = -1;
            for (int y = 1 + add; y < 8; y += 2) {
                add = 0;
                board[x][y] = 2;
            }
        }
    }
    void initiateBoardView(){
        boardView = new ImageView[8][8];
        String square = new String("square_"), square2;
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++) {
                square2 = square.concat("" + x + y);
                Log.v("GameActvity", square2);
                int resID = getResources().getIdentifier(square2, "id", getPackageName());
                boardView[x][y] = (ImageView) findViewById(resID);
            }
        }
    }
    void representBoard(){
        nome1.setText(one.getName());
        if(one.getColor() == vezJogar){
            nome1.setBackgroundColor(Color.argb(170, 255, 255, 0));
            nome2.setBackgroundColor(Color.TRANSPARENT);
        }else{
            nome2.setBackgroundColor(Color.argb(170, 255, 255, 0));
            nome1.setBackgroundColor(Color.TRANSPARENT);
        }
        pontos1.setText(getString(R.string.eaten) + " " + one.getPontos());
        pontos2.setText(getString(R.string.eaten) + " " + two.getPontos());

        Log.d("Checkers", "Pontos representar tabela: " + one.getPontos());
        Log.d("Checkers", "Pontos representar tabela: " + two.getPontos());
        Log.d("Checkers", "(0,0) representar tabela: " + board[0][0]);

        int add = 0;
        for (int x = 0; x < 8; x++) {
            if (x == 1 || x == 3 || x == 5 || x == 7) add = 1;
            for (int y = 0 + add; y < 8; y += 2) {
                add = 0;
                if (boardView[x][y] == null) {
                    Log.v("GameActvity", "Board" + x + y);
                } else {
                    if (board[x][y] == 0) {
                        boardView[x][y].setImageResource(android.R.color.black);
                    }
                    if (board[x][y] == 1) {
                        boardView[x][y].setBackgroundColor(Color.BLACK);
                        boardView[x][y].setImageResource(R.mipmap.ic_black);
                    }
                    if (board[x][y] == 2) {
                        boardView[x][y].setBackgroundColor(Color.BLACK);
                        boardView[x][y].setImageResource(R.mipmap.ic_white);
                    }
                    if (board[x][y] == 3) {
                        boardView[x][y].setBackgroundColor(Color.BLACK);
                        boardView[x][y].setImageResource(R.mipmap.ic_qblack);
                    }
                    if (board[x][y] == 4) {
                        boardView[x][y].setBackgroundColor(Color.BLACK);
                        boardView[x][y].setImageResource(R.mipmap.ic_qwhite);
                    }
                }
            }
        }
    }


    void client(final String strIP, final int Port) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Checkers", "Connecting to the server  " + strIP);
                    socketGame = new Socket(strIP, Port);
                } catch (Exception e) {
                    socketGame = null;
                }
                if (socketGame == null) {
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                    return;
                }
                clientThread.start();
            }
        });
        t.start();
    }

    Thread userInfoThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                output.writeInt(2);
                output.writeObject(two);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });


    Thread clientThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int in = 0;
            try {
                output = new ObjectOutputStream(socketGame.getOutputStream());
                input = new ObjectInputStream(socketGame.getInputStream());
                userInfoThread.start();
                while (!Thread.currentThread().isInterrupted()) {
                    in = input.readInt();
                    Log.d("Checkers", "Received: " + in);
                    if(in == 1) {
                        serverBoard = (Board) input.readObject();
                    }

                    if(in == 2) {
                        one = (Player) input.readObject();
                    }
                    if(in == 3) {
                        two = (Player) input.readObject();
                    }

                    if(in == 4) {
                        vezJogar = input.readInt();
                    }

                    if(in == 5) {
                        fim = input.readInt();
                    }
                    procMsg.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (serverBoard != null) {
                                board = serverBoard.getCell();
                                representBoard();
                            } else {
                                Log.d("Checkers", "serverBoard is null");
                            }
                            if (fim == 1) {
                                if (one.getPontos() > two.getPontos()) {
                                    winner = one;
                                    loser = two;
                                } else {
                                    winner = two;
                                    loser = one;
                                }
                                Intent intent = new Intent(getApplication(), WinnerActivity.class);
                                intent.putExtra("winner", winner);
                                intent.putExtra("loser", loser);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }, 50);
                }
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        playCPUTwo();
                    }
                });
            }
        }
    });


    class cliqueQuadrado implements View.OnClickListener{
        int x, y;
        String winner = new String();
        String loser = new String();
        cliqueQuadrado(int x, int y){
            this.x = x;
            this.y = y;

        }
        @Override
        public void onClick(View view) {
                if (!mao) {
                    //if (selecionaObrigatorio(x, y, board[x][y])) {
                        seleciona(view, x, y);
                        deX = x;
                        deY = y;
                    //}
                } else {
                        move = new Pos(deX, deY, x, y, board[deX][deY]);
                        mao = false;
                        sendMove.start();
                        representBoard();
                    }
                }
    }

    Thread sendMove = new Thread(new Runnable() {
        @Override
        public void run() {
            if(output != null) {
                try {
                    output.writeInt(1);
                    output.writeObject(move);
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    public void onBackPressed() {
        AlertDialog ad = new AlertDialog.Builder(this).setTitle(R.string.app_name)
                .setMessage(R.string.leave)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeIO();
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                    }
                }).create();
        ad.show();
    }


    @Override
    protected void onStop() {
        super.onStop();
        closeIO();
    }

    void closeIO(){
        try {
            clientThread.interrupt();
            if (socketGame != null)
                socketGame.close();
            if (output != null)
                output.close();
            if (input != null)
                input.close();
        } catch (Exception e) {
        }
        input = null;
        output = null;
        socketGame = null;
    }

    protected void onPause() {
        super.onPause();
    }

    void clientDlg() {
        final EditText edtIP = new EditText(this);
        edtIP.setText("192.168.1.133");
        AlertDialog ad = new AlertDialog.Builder(this).setTitle(R.string.app_name)
                .setMessage(R.string.server).setView(edtIP)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client(edtIP.getText().toString(), PORTaux); // to test with emulators: PORTaux);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).create();
        ad.show();
    }


    void playCPUTwo(){
        Board intentBoard = new Board(board);
        Intent intent = new Intent(this, GameCPUActivity.class);
        intent.putExtra("from", FROM_GAME_ONLINE_TWO);
        intent.putExtra("one", one);
        intent.putExtra("two", two);
        intent.putExtra("board", intentBoard);
        intent.putExtra("vezJogar", vezJogar);
        startActivity(intent);
        finish();
    }

    void setTextViewListeners() {
        nome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveGameTwo();
            }
        });
    }

    public void leaveGameTwo(){
        AlertDialog ad = new AlertDialog.Builder(this).setTitle(R.string.app_name)
                .setMessage(R.string.cpu)
                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeIO();
                        playCPUTwo();
                    }
                })
                .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                    }
                }).create();
        ad.show();
    }
}
