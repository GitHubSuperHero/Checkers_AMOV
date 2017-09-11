package pt.isec.a21230117.amovtp;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Leonardo on 07/08/2017.
 */

public class GameData implements Serializable{
    String player1, player2;
    int winner;
    Date date;
    int player1socre, player2score;

    GameData(String player1, String player2, int winner, Date date, int player1socre, int player2score){
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
        this.date = date;
        this.player1socre = player1socre;
        this.player2score = player2score;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getPlayer1socre() {
        return player1socre;
    }

    public void setPlayer1socre(int player1socre) {
        this.player1socre = player1socre;
    }

    public int getPlayer2score() {
        return player2score;
    }

    public void setPlayer2score(int player2score) {
        this.player2score = player2score;
    }
}
