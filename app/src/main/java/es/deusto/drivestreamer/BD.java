package es.deusto.drivestreamer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by asier on 17/12/2017.
 * Creada en SQLiteOpen.
 * Estructura: Un identificador nombre y canciones.
 */

public class BD extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Playlist.db";
    public static final String TABLA_NOMBRES= "";
    public static final String COLUMNA_NOMBRE="";
    public static final String COLUMNA_ID="";

    String sqlCreate="CREATE TABLE Playlist(Identificador INTEGER,Nombre TEXT,Canciones TEXT";

    public BD(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //Metodo que lanza encuanto se crea por primera vez
        db.execSQL(sqlCreate);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionVieja, int versionNueva) {
        //Encuanto detecte un cambio lanzara este metodo con el fin de actualizar la bd
        db.execSQL("DROP TABLE IF EXISTS Playlist");
        db.execSQL(sqlCreate);//


    }
    public void agregar(String nombre){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMNA_NOMBRE, nombre);

        db.insert(TABLA_NOMBRES, null,values);
        db.close();

    }
    public void obtener(int id){

        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMNA_ID, COLUMNA_NOMBRE};

        Cursor cursor =
                db.query(TABLA_NOMBRES,
                        projection,
                        " _id = ?",
                        new String[] { String.valueOf(id) },
                        null,
                        null,
                        null,
                        null);


        if (cursor != null)
            cursor.moveToFirst();

        System.out.println("El nombre es " +  cursor.getString(1) );
        db.close();

    }
}
