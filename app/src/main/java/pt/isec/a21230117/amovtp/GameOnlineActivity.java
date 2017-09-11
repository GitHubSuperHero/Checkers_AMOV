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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static pt.isec.a21230117.amovtp.GameCPUActivity.FROM_GAME_ONLINE_ONE;

public class GameOnlineActivity extends AppCompatActivity {
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

    ImageView[][] boardView;
    TextView nome1, nome2, pontos1, pontos2;

    int deX, deY, paraX, paraY, vezJogar = 2, fim;

    List<Pos> obriga;

    Player one, two;
    Pos pos;
    Board serverBoard;

    boolean mao, obrigaDama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        deX = 0;
        deY = 0;
        paraX = 0;
        paraY = 0;
        fim = 0;
        mao = false;
        obrigaDama = false;
        obriga = new ArrayList<>();
        one = new Player(getString(R.string.playerone));
        two = new Player(getString(R.string.playertwo));
        procMsg = new Handler();
        serverBoard = new Board(board);
        pos = null;
        output = null;
        input = null;
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
            one = (Player) i.getSerializableExtra("player");
        }
        setPlayerInfo();
        representBoard();
        server();
    }

    void setTextViewListeners() {
        nome2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveGameOne();
            }
        });
    }

    void setPlayerInfo(){
        nome1 = (TextView) findViewById(R.id.nomeJogador1);
        nome2 = (TextView) findViewById(R.id.nomeJogador2);
        pontos1 = (TextView) findViewById(R.id.peoesJogador1);
        pontos2 = (TextView) findViewById(R.id.peoesJogador2);
        one.setColor(PRETA);
        two.setColor(BRANCA);
        nome1.setText(one.getName());
        nome2.setText(two.getName());
        pontos1.append(" "+ one.getPontos());
        pontos2.append(" "+ two.getPontos());
        setTextViewListeners();
    }

    boolean validaFim() {
        int countB = 0, countW = 0, countWC = 0, countBC = 0;
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++) {
                if (board[x][y] == 1)
                    countB++;

                if (board[x][y] == 2)
                    countW++;

                if (board[x][y] == 3)
                    countBC++;

                if (board[x][y] == 4)
                    countWC++;
            }

        if (countB == 0 && countW == 0 && countBC == 1 && countWC == 1)
            return true;

        if(countB == 0 && countBC == 0)
            return true;

        if(countW == 0 && countWC == 0)
            return true;

        return validaFimComPeao();
    }

    boolean validaFimComPeao(){
        int countB = 0, countW = 0, lastWX=0, lastWY=0, lastBX=0, lastBY=0;
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++) {
                if (board[x][y] == 1) {
                    lastBX = x;
                    lastBY = y;
                    countB++;
                }

                if (board[x][y] == 2) {
                    lastWX = x;
                    lastWY = y;
                    countW++;
                }
            }
        if(countW == 1 && countB == 1){
            return true;
        }
        if(countW == 1){
            if(validaUltimaBranca(lastWX, lastWY, lastWX - 2, lastWY -2) ||
                    validaUltimaBranca(lastWX, lastWY, lastWX - 2, lastWY + 2) ||
                    valida(lastWX, lastWY, lastWX - 1, lastWY -1, 2) ||
                    valida(lastWX, lastWY, lastWX - 1, lastWY + 1, 2) ){
                return false;
            }
            return true;
        }

        if(countB == 1){
            if(validaUltimaPreta(lastBX, lastBY, lastBX +2, lastBY +2) ||
                    validaUltimaPreta(lastBX, lastBY, lastBX +2, lastBY - 2) ||
                    valida(lastBX, lastBY, lastBX + 1, lastBY +1, 1) ||
                    valida(lastBX, lastBY, lastBX + 1, lastBY - 1, 1) ){
                return false;
            }
            return true;
        }
        return false;
    }


    void validaRainha(int x, int y, int cor) {
        if (x == 0 && cor == 2) {
            board[x][y] = 4;
        }
        if (x == 7 && cor == 1) {
            board[x][y] = 3;
        }
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

    boolean validaUltimaBranca(int x, int y, int tox, int toy){
        if(tox > 7 || tox < 0 || toy > 7 || toy < 0){
            return false;
        }

        if ((x - tox) == 2 && (y - toy) == 2) {
            if (board[tox + 1][toy + 1] == 1 || board[tox + 1][toy + 1] == 3) {
                return true;
            }
        }

        if ((x - tox) == 2 && (y - toy) == -2) {
            if (board[tox + 1][toy - 1] == 1 || board[tox + 1][toy - 1] == 3) {
                return true;
            }
        }

        return false;
    }

    boolean validaUltimaPreta(int x, int y, int tox, int toy){
        if(tox > 7 || tox < 0 || toy > 7 || toy < 0){
            return false;
        }

        if ((x - tox) == -2 && (y - toy) == 2) {
            if (board[tox - 1][toy + 1] == 2 || board[tox - 1][toy + 1] == 4) {
                return true;
            }
        }

        if ((x - tox) == -2 && (y - toy) == -2) {
            if (board[tox - 1][toy - 1] == 2 || board[tox - 1][toy - 1] == 4) {
                return true;
            }
        }
        return false;
    }

    boolean valida(int x, int y, int tox, int toy, int cor) {
        if(tox > 7 || tox < 0 || toy > 7 || toy < 0){
            return false;
        }
        if (board[tox][toy] == 1 || board[tox][toy] == 2 || board[tox][toy] == 3 || board[tox][toy] == 4) {
            return false;
        }


        if ((x - tox) == 2 && (y - toy) == 2 && cor == 2) {
                if (board[tox + 1][toy + 1] == 1 || board[tox + 1][toy + 1] == 3) {
                    comePreta(tox+1, toy+1);
                    return true;
                }
        }

        if ((x - tox) == 2 && (y - toy) == -2 && cor == 2) {
                if (board[tox + 1][toy - 1] == 1 || board[tox + 1][toy - 1] == 3) {
                    comePreta(tox+1, toy-1);
                    return true;
                }
        }

        if ((x - tox) == -2 && (y - toy) == 2 && cor == 1) {
                if (board[tox - 1][toy + 1] == 2 || board[tox - 1][toy + 1] == 4) {
                   comeBranca(tox-1, toy+1);
                    return true;
                }
        }

        if ((x - tox) == -2 && (y - toy) == -2 && cor == 1) {
                if (board[tox - 1][toy - 1] == 2 || board[tox - 1][toy - 1] == 4) {
                    comeBranca(tox-1, toy-1);
                    return true;
            }
        }

        if ((x - tox) == 1 && cor == 2 || (x - tox) == -1 && cor == 1)
            if ((y - toy) == 1 || (y - toy == -1)) {
                return true;
            }
        return false;
    }

    void comeBranca(int x, int y){
        board[x][y] = 0;
        if(one.getColor()==1){
            one.setPontos(one.getPontos()+1);
        }else {
            two.setPontos(two.getPontos()+1);
        }
    }
    void comePreta(int x, int y){
        board[x][y] = 0;
        if(one.getColor()==2){
            one.setPontos(one.getPontos()+1);
        }else {
            two.setPontos(two.getPontos()+1);
        }
    }

    void movePeao(int x, int y, int tox, int toy, int cor) {
        if(cor > 2) {
            if (verificaObrigatoriaDamas(x, y, cor)) {
                if (validaDamas(x, y, tox, toy, cor, true)) {
                    board[x][y] = 0;
                    board[tox][toy] = cor;
                }
                if (vezJogar == 1) {
                    if (verificaObrigatoriaDamas(tox, toy, cor)) {
                        vezJogar = 1;
                        obrigaDama = true;
                    } else {
                        vezJogar = 2;
                        obrigaDama = false;
                    }
                } else {
                    if (verificaObrigatoriaDamas(tox, toy, cor)) {
                        vezJogar = 2;
                        obrigaDama = true;
                    } else {
                        vezJogar = 1;
                        obrigaDama = false;
                    }
                }
            } else {
                if (board[x][y] == cor && board[x][y] != 0 && validaDamas(x, y, tox, toy, cor, false)) {
                    board[x][y] = 0;
                    board[tox][toy] = cor;
                    if (vezJogar == 1) {
                        vezJogar = 2;
                        obrigaDama = false;
                    } else {
                        vezJogar = 1;
                        obrigaDama = false;
                    }
                    validaRainha(tox, toy, cor);
                }
            }
        }
        if(cor < 3 && !obrigaDama) {
            if (verificaObrigatoria(x, y, cor)) {
                if (tox == paraX && toy == paraY && valida(x, y, tox, toy, cor)) {
                    board[x][y] = 0;
                    board[tox][toy] = cor;
                    if (vezJogar == 1) {
                        if (verificaObrigatoria(tox, toy, cor)) {
                            vezJogar = 1;
                        } else {
                            vezJogar = 2;
                        }
                    } else {
                        if (verificaObrigatoria(tox, toy, cor)) {
                            vezJogar = 2;
                        } else {
                            vezJogar = 1;
                        }
                    }
                    validaRainha(tox, toy, cor);
                }
            } else {
                if (board[x][y] == cor && board[x][y] != 0 && valida(x, y, tox, toy, cor)) {
                    board[x][y] = 0;
                    board[tox][toy] = cor;
                    if (vezJogar == 1) {
                        vezJogar = 2;
                    } else vezJogar = 1;
                    validaRainha(tox, toy, cor);
                }
            }
        }
        mao = false;
    }

    boolean verificaObrigatoriaDamas(int a, int b, int cor){
        int x = a, y = b, count = 0;
        if(cor == 3){
            for(int i = 1; i < 8; i++){
                if(x+i > 7 || y+i > 7){
                    break;
                }
                if(board[x+i][y+i] == 1 || board[x+i][y+i] == 3 ){
                    count++;
                }
                if(board[x+i][y+i] == 2 || board[x+i][y+i] == 4 ){
                    if(board[x+i-1][y+i-1] == 2 || board[x+i-1][y+i-1] == 4){
                        break;
                    }
                }
                if(board[x+i][y+i] == 0){
                    if((board[x+i-1][y+i-1] == 2 || board[x+i-1][y+i-1] == 4)&& count == 0){
                        return true;
                    }
                }
            }
            count = 0;
            for(int i = 1; i < 8; i++){
                if(x+i > 7 || y-i < 0){
                    break;
                }
                if(board[x+i][y-i] == 1 || board[x+i][y-i] == 3 ){
                    count++;
                }
                if(board[x+i][y-i] == 2 || board[x+i][y-i] == 4 ){
                    if(board[x+i-1][y-i+1] == 2 || board[x+i-1][y-i+1] == 4){
                        break;
                    }
                }

                if(board[x+i][y-i] == 0){
                    if((board[x+i-1][y-i+1] == 2 || board[x+i-1][y-i+1] == 4) && count == 0){
                        return true;
                    }
                }
            }
            count = 0;
            for(int i = 1; i < 8; i++){
                if(x-i < 0 || y-i < 0){
                    break;
                }

                if(board[x-i][y-i] == 1 || board[x-i][y-i] == 3 ){
                    count++;
                }
                if(board[x-i][y-i] == 2 || board[x-i][y-i] == 4 ){
                    if(board[x-i+1][y-i+1] == 2 || board[x-i+1][y-i+1] == 4){
                        break;
                    }
                }
                if(board[x-i][y-i] == 0){
                    if((board[x-i+1][y-i+1] == 2 || board[x-i+1][y-i+1] == 4)&& count == 0){
                        return true;
                    }
                }
            }
            count = 0;
            for(int i = 1; i < 8; i++){
                if(x-i < 0 || y+i > 7){
                    break;
                }

                if(board[x-i][y+i] == 1 || board[x-i][y+i] == 3 ){
                    count++;
                }
                if(board[x-i][y+i] == 2 || board[x-i][y+i] == 4 ){
                    if(board[x-i+1][y+i-1] == 2 || board[x-i+1][y+i-1] == 4){
                        break;
                    }
                }
                if(board[x-i][y+i] == 0){
                    if((board[x-i+1][y+i-1] == 2 || board[x-i+1][y+i-1] == 4)&&count ==0){
                        return true;
                    }
                }
            }
        }

        if(cor == 4){
            count = 0;
            for(int i = 1; i < 8; i++){
                if(x+i > 7 || y+i > 7){
                    break;
                }
                if(board[x+i][y+i] == 2 || board[x+i][y+i] == 4 ){
                    count++;
                }
                if(board[x+i][y+i] == 1 || board[x+i][y+i] == 3 ){
                    if(board[x+i-1][y+i-1] == 1 || board[x+i-1][y+i-1] == 3){
                        break;
                    }
                }
                if(board[x+i][y+i] == 0){
                    if((board[x+i-1][y+i-1] == 1 || board[x+i-1][y+i-1] == 3)&& count == 0){
                        return true;
                    }
                }
            }
            count = 0;
            for(int i = 1; i < 8; i++){
                if(x+i > 7 || y-i < 0){
                    break;
                }
                if(board[x+i][y-i] == 2 || board[x+i][y-i] == 4 ){
                    count++;
                }
                if(board[x+i][y-i] == 1 || board[x+i][y-i] == 3 ){
                    if(board[x+i-1][y-i+1] == 1 || board[x+i-1][y-i+1] == 3){
                        break;
                    }
                }
                if(board[x+i][y-i] == 0){
                    if((board[x+i-1][y-i+1] == 1 || board[x+i-1][y-i+1] == 3) && count == 0){
                        return true;
                    }
                }
            }
            count = 0;
            for(int i = 1; i < 8; i++){
                if(x-i < 0 || y-i < 0){
                    break;
                }
                if(board[x-i][y-i] == 2 || board[x-i][y-i] == 4 ){
                    count++;
                }
                if(board[x-i][y-i] == 1 || board[x-i][y-i] == 3 ){
                    if(board[x-i+1][y-i+1] == 1 || board[x-i+1][y-i+1] == 3){
                        break;
                    }
                }
                if(board[x-i][y-i] == 0){
                    if ((board[x - i + 1][y - i + 1] == 1 || board[x - i + 1][y - i + 1] == 3) && count == 0) {
                        return true;
                    }
                }
            }
            count = 0;
            for(int i = 1; i < 8; i++){
                if(x-i < 0 || y+i > 7){
                    break;
                }
                if(board[x-i][y+i] == 2 || board[x-i][y+i] == 4 ){
                    count++;
                }
                if(board[x-i][y+i] == 1 || board[x-i][y+i] == 3 ){
                    if(board[x-i+1][y+i-1] == 1 || board[x-i+1][y+i-1] == 3){
                        break;
                    }
                }
                if(board[x-i][y+i] == 0){
                    if((board[x-i+1][y+i-1] == 1 || board[x-i+1][y+i-1] == 3) && count == 0){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    boolean validaDamas(int x, int y, int tox, int toy, int cor, boolean obriga) {
        if (tox > 7 || tox < 0 || toy > 7 || toy < 0) {
            return false;
        }
        if (board[tox][toy] == 1 || board[tox][toy] == 2 || board[tox][toy] == 3 || board[tox][toy] == 4) {
            return false;
        }

        if (((x - tox) == (y - toy) || (x - tox) == -(y - toy)) && cor == 3) {

            if (x > tox && y > toy) { //direita para cima
                int a = tox, b = toy, count = 0, i = 0, j = 0;
                while (a != x && b != y) {
                    if (board[a][b] == 2 || board[a][b] == 4) {
                        count++;
                        i = a;
                        j = b;
                    }
                    if (board[a][b] == 1 || board[a][b] == 3) {
                        return false;
                    }
                    a++;
                    b++;
                }
                if (count == 1) {
                    comeBranca(i, j);
                    return true;
                }else{
                    if(obriga) {
                        return false;
                    }else{
                        if(count > 1){
                            return false;
                        }
                    }
                }
            }
            if (x < tox && y > toy) { //direita para cima
                int a = tox, b = toy, count = 0, i = 0, j = 0;
                while (a != x && b != y) {
                    if (board[a][b] == 2 || board[a][b] == 4) {
                        count++;
                        i = a;
                        j = b;
                    }
                    if (board[a][b] == 1 || board[a][b] == 3) {
                        return false;
                    }
                    a--;
                    b++;
                }
                if (count == 1) {
                    comeBranca(i, j);
                    return true;
                }else{
                    if(obriga) {
                        return false;
                    }else{
                        if(count > 1){
                            return false;
                        }
                    }
                }
            }
            if (x < tox && y < toy) { //direita para cima
                int a = tox, b = toy, count = 0, i = 0, j = 0;
                while (a != x && b != y) {
                    if (board[a][b] == 2 || board[a][b] == 4) {
                        count++;
                        i = a;
                        j = b;
                    }
                    if (board[a][b] == 1 || board[a][b] == 3) {
                        return false;
                    }
                    a--;
                    b--;
                }
                if (count == 1) {
                    comeBranca(i, j);
                    return true;
                }else{
                    if(obriga) {
                        return false;
                    }else{
                        if(count > 1){
                            return false;
                        }
                    }
                }
            }
            if (x > tox && y < toy) { //direita para cima
                int a = tox, b = toy, count = 0, i = 0, j = 0;
                while (a != x && b != y) {
                    if (board[a][b] == 2 || board[a][b] == 4) {
                        count++;
                        i = a;
                        j = b;
                    }
                    if (board[a][b] == 1 || board[a][b] == 3) {
                        return false;
                    }
                    a++;
                    b--;
                }
                if (count == 1) {
                    comeBranca(i, j);
                    return true;
                }else{
                    if(obriga) {
                        return false;
                    }else{
                        if(count > 1){
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        if (((x - tox) == (y - toy) || (x - tox) == -(y - toy)) && cor == 4) {

            if (x > tox && y > toy) { //direita para cima
                int a = tox, b = toy, count = 0, i = 0, j = 0;
                while (a != x && b != y) {
                    if (board[a][b] == 1 || board[a][b] == 3) {
                        count++;
                        i = a;
                        j = b;
                    }
                    if (board[a][b] == 2 || board[a][b] == 4) {
                        return false;
                    }
                    a++;
                    b++;
                }
                if (count == 1) {
                    comePreta(i, j);
                    return true;
                }else{
                    if(obriga) {
                        return false;
                    }else{
                        if(count > 1){
                            return false;
                        }
                    }
                }
            }
            if (x < tox && y > toy) { //direita para cima
                int a = tox, b = toy, count = 0, i = 0, j = 0;
                while (a != x && b != y) {
                    if (board[a][b] == 1 || board[a][b] == 3) {
                        count++;
                        i = a;
                        j = b;
                    }
                    if (board[a][b] == 2 || board[a][b] == 4) {
                        return false;
                    }
                    a--;
                    b++;
                }
                if (count == 1) {
                    comePreta(i, j);
                    return true;
                }else{
                    if(obriga) {
                        return false;
                    }else{
                        if(count > 1){
                            return false;
                        }
                    }
                }
            }
            if (x < tox && y < toy) { //direita para cima
                int a = tox, b = toy, count = 0, i = 0, j = 0;
                while (a != x && b != y) {
                    if (board[a][b] == 1 || board[a][b] == 3) {
                        count++;
                        i = a;
                        j = b;
                    }
                    if (board[a][b] == 2 || board[a][b] == 4) {
                        return false;
                    }
                    a--;
                    b--;
                }
                if (count == 1) {
                    comePreta(i, j);
                    return true;
                }else{
                    if(obriga) {
                        return false;
                    }else{
                        if(count > 1){
                            return false;
                        }
                    }
                }
            }
            if (x > tox && y < toy) { //direita para cima
                int a = tox, b = toy, count = 0, i = 0, j = 0;
                while (a != x && b != y) {
                    if (board[a][b] == 1 || board[a][b] == 3) {
                        count++;
                        i = a;
                        j = b;
                    }
                    if (board[a][b] == 2 || board[a][b] == 4) {
                        return false;
                    }
                    a++;
                    b--;
                }
                if (count == 1) {
                    comePreta(i, j);
                    return true;
                }else{
                    if(obriga) {
                        return false;
                    }else{
                        if(count > 1){
                            return false;
                        }
                    }
                }
            }
            return true;
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
        if(board[x][y] == PRETA || board[x][y] == DPRETA) {
            if(vezJogar == PRETA) {
                ImageView iv = (ImageView) view;
                iv.setBackgroundColor(Color.BLUE);
                mao = true;
            }
        }
    }

    int vencedor(){
        int countB = 0, countW = 0;
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++) {
                if (board[x][y] == 1 || board[x][y] == 3) {
                    countB++;
                }

                if (board[x][y] == 2 || board[x][y] == 4) {
                    countW++;
                }
            }

            if(countB > countW){
                return 1;
            }else{
                if(countW > countB){
                    return 2;
                }else{
                    return 0;
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
        nome2.setText(two.getName());
        if(one.getColor() == vezJogar){
            nome1.setBackgroundColor(Color.argb(170, 255, 255, 0));
            nome2.setBackgroundColor(Color.TRANSPARENT);
        }else{
            nome2.setBackgroundColor(Color.argb(170, 255, 255, 0));
            nome1.setBackgroundColor(Color.TRANSPARENT);
        }
        pontos1.setText(getString(R.string.eaten) + " " + one.getPontos());
        pontos2.setText(getString(R.string.eaten) + " " + two.getPontos());

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

    void server() {
        String ip = getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.waiting) + "\n(IP: " + ip //espera do cliente
                + ")");
        pd.setTitle(R.string.app_name);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
                if (serverSocket!=null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                    }
                    serverSocket=null;
                }
            }
        });
        pd.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORTaux);
                    socketGame = serverSocket.accept();
                    serverSocket.close();
                    serverSocket=null;
                    serverThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    socketGame = null;
                }
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        if (socketGame == null)
                            finish();
                    }
                });
            }
        });
        t.start();
    }


    Thread serverThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int in;
            try {
                output = new ObjectOutputStream(socketGame.getOutputStream());
                input = new ObjectInputStream(socketGame.getInputStream());
                Log.d("Checker", "Sokcet IO is done");
                sendNewBoard();
                while (!Thread.currentThread().isInterrupted()) {
                    in = input.readInt();
                    if(in == 1) {
                        pos = (Pos) input.readObject(); //tecnica -> 1 posicao, 2 informacao, 3 outra coisa
                        Log.d("Checkers", "Received: " + pos.getX() + " " + pos.getY());
                    }
                    if(in == 2) {
                        two = (Player) input.readObject();
                    }
                    
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            if(pos != null)
                            moveOtherPlayer(pos);
                            representBoard();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        playCPUOne();
                    }
                });
            }
        }
    });

    void moveOtherPlayer(Pos p){
        int x = p.getX(), y = p.getY(), tox = p.getTox(), toy = p.getToy();
        String winner = new String();
        String loser = new String();
        movePeao(x, y, tox, toy, board[x][y]);
        if(validaFim()){
            if(vencedor() == one.getColor()){
                winner = one.getName();
                loser = two.getName();
            }
            if(vencedor() == two.getColor()){
                winner = two.getName();
                loser = one.getName();
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("winner", winner);
            intent.putExtra("loser", loser);
            startActivity(intent);
        }
        representBoard();
    }

    void sendNewBoard() {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("Checkers", "Sending board");
                        Board sendBoard = new Board(board);
                        output.writeInt(1);
                        output.reset();
                        output.writeObject(sendBoard);
                        output.flush();
                        output.writeInt(2);
                        output.reset();
                        output.writeObject(one);
                        output.flush();
                        output.reset();
                        Log.d("Checkers", "Sending one points: " + one.getPontos());
                        output.writeInt(3);
                        output.reset();
                        output.writeObject(two);
                        output.flush();
                        Log.d("Checkers", "Sending two points: " + two.getPontos());
                        output.writeInt(4);
                        output.writeInt(vezJogar);
                        output.flush();
                        output.writeInt(5);
                        output.writeInt(fim);
                        output.flush();
                    } catch (Exception e) {
                        Log.d("Checkers", "Error sending board");
                    }
                }
            });
            t.start();
    }

    class cliqueQuadrado implements View.OnClickListener{
        int x, y;
        Player winner = new Player();
        Player loser = new Player();
        cliqueQuadrado(int x, int y){
            this.x = x;
            this.y = y;

        }
        @Override
        public void onClick(View view) {
                if (!mao) {
                    if (selecionaObrigatorio(x, y, board[x][y])) {
                        seleciona(view, x, y);
                        deX = x;
                        deY = y;
                    }
                } else {
                        movePeao(deX, deY, x, y, board[deX][deY]);
                        if (validaFim()) {
                            fim = 1;
                            sendNewBoard();
                            if (one.getPontos() > two.getPontos()) {
                                winner = one;
                                loser = two;
                            } else {
                                winner = two;
                                loser = one;
                            }
                            Intent intent = new Intent(view.getContext(), WinnerActivity.class);
                            intent.putExtra("winner", winner);
                            intent.putExtra("loser", loser);
                            startActivity(intent);
                            finish();
                        }
                        representBoard();
                }
        }
    }
    @Override
    public void onBackPressed() {
        AlertDialog ad = new AlertDialog.Builder(this).setTitle(R.string.app_name)
                .setMessage(R.string.leave)
                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeIO();
                        finish();
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


    @Override
    protected void onStop() {
        super.onStop();
        closeIO();
    }

    void closeIO(){
        try {
            serverThread.interrupt();
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

    void playCPUOne(){
        Board intentBoard = new Board(board);
        Intent intent = new Intent(this, GameCPUActivity.class);
        intent.putExtra("from", FROM_GAME_ONLINE_ONE);
        intent.putExtra("one", one);
        intent.putExtra("two", two);
        intent.putExtra("board", intentBoard);
        intent.putExtra("vezJogar", vezJogar);
        startActivity(intent);
        finish();
    }

    public void leaveGameOne(){
        AlertDialog ad = new AlertDialog.Builder(this).setTitle(R.string.app_name)
                .setMessage(R.string.cpu)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        closeIO();
                        playCPUOne();
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


    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
