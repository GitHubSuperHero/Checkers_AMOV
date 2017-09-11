package pt.isec.a21230117.amovtp;

import java.io.Serializable;

/**
 * Created by Leonardo on 13/08/2017.
 */

public class Pos implements Serializable{
    int x, y, cor, toy, tox;

    Pos(int x, int y, int cor){
        this.x = x;
        this.y = y;
        this.cor = cor;
    }
    Pos(int x, int y, int tox, int toy, int cor){
        this.x = x;
        this.y = y;
        this.tox = tox;
        this.toy = toy;
        this.cor = cor;
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setCor(int cor) {
        this.cor = cor;
    }

    public int getCor() {
        return cor;
    }

    public void setTox(int tox) {
        this.tox = tox;
    }

    public void setToy(int toy) {
        this.toy = toy;
    }

    public int getTox() {
        return tox;
    }

    public int getToy() {
        return toy;
    }
}
