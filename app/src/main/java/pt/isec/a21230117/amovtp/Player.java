package pt.isec.a21230117.amovtp;

import java.io.Serializable;

/**
 * Created by Leonardo on 11/08/2017.
 */

public class Player implements Serializable{

    String name;
    int color, pontos;

    Player(){pontos = 0; color = 0;}

    Player(String name){
        this.name = name;
        color = 0;
        pontos = 0;
    }
    Player(String name, int color){
        this.name = name;
        this.color = color;
        pontos = 0;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }
}
