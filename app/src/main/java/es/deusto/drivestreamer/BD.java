package es.deusto.drivestreamer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by asier on 17/12/2017.
 * Creada en SQLiteOpen.
 * Estructura: Un identificador nombre y canciones.
 */

public class BD extends SQLiteOpenHelper {
    String sql="CREATE TABLE Playlist(Identificador INTEGER,Nombre TEXT,Canciones TEXT";

    public BD(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //Metodo que lanza encuanto se crea por primera vez
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //Encuanto detecte un cambio lanzara este metodo con el fin de actualizar la bd
        db.execSQL("DROP TABLE IF EXISTS Playlist");
        db.execSQL(sql);//


    }
}
