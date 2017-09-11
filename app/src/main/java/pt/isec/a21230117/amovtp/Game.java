package pt.isec.a21230117.amovtp;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.Z;

/**
 * Created by Leonardo on 14/08/2017.
 */

public class Game {
    private final int PRETA = 1;
    private final int BRANCA = 2;
    private final int TMAX = 8;
    private final int TMIN = 0;
    private final int DPRETA = 3;
    private final int DBRANCA = 4;

    private int[][] board;
    int deX, deY, paraX, paraY, vezJogar;

    List<Pos> obriga;

    Player one, two;

    boolean mao, segundaJogada;

    Game(Player one, Player two){
        deX = 0;
        deY = 0;
        paraX = 0;
        paraY = 0;
        mao = false;
        segundaJogada = false;
        obriga = new ArrayList<>();
        this.one = one;
        this.two = two;
        vezJogar = 2;
        setInitialBoard();
    }

    public int getVezJogar() {
        return vezJogar;
    }

    public void setVezJogar(int vezJogar) {
        this.vezJogar = vezJogar;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public Player getOne() {
        return one;
    }

    public void setOne(Player one) {
        this.one = one;
    }

    public Player getTwo() {
        return two;
    }

    public void setTwo(Player two) {
        this.two = two;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getCell(int x, int y){
        if(x > TMAX || y > TMAX || x < TMIN || y < TMIN){
            return -1;
        }else{
            return board[x][y];
        }
    }

    void setInitialBoard() {
        int add = 0;
        board = new int[8][8];
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++) {
                board[x][y] = 0;
            }

        for (int x = 0; x < 3; x++) {
            if (x == 1) add = 1;
            for (int y = 0 + add; y < 8; y += 2) {
                add = 0;
                board[x][y] = 1;
            }
        }

        for (int x = 5; x < 8; x++) {
            if (x == 6) add = -1;
            for (int y = 1 + add; y < 8; y += 2) {
                add = 0;
                board[x][y] = 2;
            }
        }
    }

    void movePeao(int x, int y, int tox, int toy, int cor) {
        if (verificaObrigatoria(x, y, cor) && cor < 3) {
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
        mao = false;
    }

    boolean verificaObrigatoriaDama(int x, int y, int cor) {
        int a = x, b = y;
        if (cor == 3) {
            while (a > 0 || b > 0) {
                a--;
                b--;
                if (board[a - 1][b - 1] == 0) {
                    if (cor == 3 && (board[a][b] == 2 || board[a][b] == 4)) {
                        paraX = a - 1;
                        paraY = b - 1;
                    }
                    return true;
                }
            }
            a = x;
            b = y;
            while (a > 0 || b < 7) {
                a--;
                b++;
                if (board[a - 1][b + 1] == 0) {
                    if (cor == 3 && (board[a][b] == 2 || board[a][b] == 4)) {
                        paraX = a - 1;
                        paraY = b + 1;
                    }
                    return true;
                }
            }
            a = x;
            b = y;
            while (a < 7 || b > 0) {
                a++;
                b--;
                if (board[a + 1][b - 1] == 0) {
                    if (cor == 3 && (board[a][b] == 2 || board[a][b] == 4)) {
                        paraX = a + 1;
                        paraY = b - 1;
                    }
                    return true;
                }
            }
            a = x;
            b = y;
            while (a < 7 || b < 7) {
                a++;
                b++;
                if (board[a + 1][b + 1] == 0) {
                    if (cor == 3 && (board[a][b] == 2 || board[a][b] == 4)) {
                        paraX = a + 1;
                        paraY = b + 1;
                    }
                    return true;
                }
            }
        }

        if (cor == 4) {
            while (a > 0 || b > 0) {
                a--;
                b--;
                if (board[a - 1][b - 1] == 0) {
                    if (cor == 3 && (board[a][b] == 1 || board[a][b] == 3)) {
                        paraX = a - 1;
                        paraY = b - 1;
                    }
                    return true;
                }
            }
            a = x;
            b = y;
            while (a > 0 || b < 7) {
                a--;
                b++;
                if (board[a - 1][b + 1] == 0) {
                    if (cor == 3 && (board[a][b] == 1 || board[a][b] == 3)) {
                        paraX = a - 1;
                        paraY = b + 1;
                    }
                    return true;
                }
            }
            a = x;
            b = y;
            while (a < 7 || b > 0) {
                a++;
                b--;
                if (board[a + 1][b - 1] == 0) {
                    if (cor == 3 && (board[a][b] == 1 || board[a][b] == 3)) {
                        paraX = a + 1;
                        paraY = b - 1;
                    }
                    return true;
                }
            }
            a = x;
            b = y;
            while (a < 7 || b < 7) {
                a++;
                b++;
                if (board[a + 1][b + 1] == 0) {
                    if (cor == 3 && (board[a][b] == 1 || board[a][b] == 3)) {
                        paraX = a + 1;
                        paraY = b + 1;
                    }
                    return true;
                }
            }
        }

        return false;
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

    boolean validaUltimaBranca(int x, int y, int tox, int toy) {
        if (tox > 7 || tox < 0 || toy > 7 || toy < 0) {
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

    boolean validaUltimaPreta(int x, int y, int tox, int toy) {
        if (tox > 7 || tox < 0 || toy > 7 || toy < 0) {
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
                }
                if (count > 1) {
                    return false;
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
                }
                if (count > 1) {
                    return false;
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
                }
                if (count > 1) {
                    return false;
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
                }
                if (count > 1) {
                    return false;
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
                }
                if (count > 1) {
                    return false;
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
                }
                if (count > 1) {
                    return false;
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
                }
                if (count > 1) {
                    return false;
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
                }
                if (count > 1) {
                    return false;
                }
            }
            return true;
        }

        if ((x - tox) == 2 && (y - toy) == 2 && cor == 2) {
            if (board[tox + 1][toy + 1] == 1 || board[tox + 1][toy + 1] == 3) {
                comePreta(tox + 1, toy + 1);
                return true;
            }
        }

        if ((x - tox) == 2 && (y - toy) == -2 && cor == 2) {
            if (board[tox + 1][toy - 1] == 1 || board[tox + 1][toy - 1] == 3) {
                comePreta(tox + 1, toy - 1);
                return true;
            }
        }

        if ((x - tox) == -2 && (y - toy) == 2 && cor == 1) {
            if (board[tox - 1][toy + 1] == 2 || board[tox - 1][toy + 1] == 4) {
                comeBranca(tox - 1, toy + 1);
                return true;
            }
        }

        if ((x - tox) == -2 && (y - toy) == -2 && cor == 1) {
            if (board[tox - 1][toy - 1] == 2 || board[tox - 1][toy - 1] == 4) {
                comeBranca(tox - 1, toy - 1);
                return true;
            }
        }

        if ((x - tox) == 1 && cor == 2 || (x - tox) == -1 && cor == 1)
            if ((y - toy) == 1 || (y - toy == -1)) {
                return true;
            }
        return false;
    }

    void comeBranca(int x, int y) {
        board[x][y] = 0;
        if (one.getColor() == 1) {
            one.setPontos(one.getPontos() + 1);
        } else {
            two.setPontos(two.getPontos() + 1);
        }
    }

    void comePreta(int x, int y) {
        board[x][y] = 0;
        if (one.getColor() == 2) {
            one.setPontos(one.getPontos() + 1);
        } else {
            two.setPontos(two.getPontos() + 1);
        }
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
                    countWC++;

                if (board[x][y] == 4)
                    countBC++;
            }

        if (countB == 0 && countW == 0 && countBC == 1 && countWC == 1)
            return true;

        if (countB == 0 && countBC == 0)
            return true;

        if (countW == 0 && countWC == 0)
            return true;

        return validaFimComPeao();
    }

    boolean validaFimComPeao() {
        int countB = 0, countW = 0, lastWX = 0, lastWY = 0, lastBX = 0, lastBY = 0;
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
        if (countW == 1 && countB == 1) {
            return true;
        }
        if (countW == 1) {
            if (validaUltimaBranca(lastWX, lastWY, lastWX - 2, lastWY - 2) ||
                    validaUltimaBranca(lastWX, lastWY, lastWX - 2, lastWY + 2) ||
                    valida(lastWX, lastWY, lastWX - 1, lastWY - 1, 2) ||
                    valida(lastWX, lastWY, lastWX - 1, lastWY + 1, 2)) {
                return false;
            }
            return true;
        }

        if (countB == 1) {
            if (validaUltimaPreta(lastBX, lastBY, lastBX + 2, lastBY + 2) ||
                    validaUltimaPreta(lastBX, lastBY, lastBX + 2, lastBY - 2) ||
                    valida(lastBX, lastBY, lastBX + 1, lastBY + 1, 1) ||
                    valida(lastBX, lastBY, lastBX + 1, lastBY - 1, 1)) {
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

    int vencedor() {
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

        if (countB > countW) {
            return 1;
        } else {
            if (countW > countB) {
                return 2;
            } else {
                return 0;
            }
        }
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

}
