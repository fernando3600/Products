package com.curso.liga.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Sql extends SQLiteOpenHelper {

    private static final String DATABASE = "jugadores.db";
    private static final int VERSION = 1;

    private final String tablaJugadores = "create table jugadores (" +
            "id integer primary key autoincrement not null, " +
            "nombre text not null, " +
            "fecha_nacimiento text not null, " +
            "pais_origen text not null, " +
            "imagen text not null, " +
            "activo integer not null);";

    public Sql(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(tablaJugadores);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("drop table if exists jugadores");
            db.execSQL(tablaJugadores);
        }
    }
}
