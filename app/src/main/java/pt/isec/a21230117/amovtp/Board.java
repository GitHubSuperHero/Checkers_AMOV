package pt.isec.a21230117.amovtp;

import java.io.Serializable;

/**
 * Created by Leonardo on 04/08/2017.
 */

public class Board implements Serializable {

    int[][] cell;

    Board(int[][] cell){
        this.cell = cell;
    }

    public int[][] getCell() {
        return cell;
    }

    public void setCell(int[][] cell) {
        this.cell = cell;
    }
}
