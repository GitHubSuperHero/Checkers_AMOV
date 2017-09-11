package pt.isec.a21230117.amovtp;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Leonardo on 07/08/2017.
 */

public class User implements Serializable{
    String name, pass;
    List<GameData> lastGames;
    Bitmap photo;
}
