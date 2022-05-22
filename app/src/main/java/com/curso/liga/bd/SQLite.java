package com.curso.liga.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.util.Log;

import java.util.ArrayList;

public class SQLite {

    String activo = "";
    Sql sql;
    SQLiteDatabase db;

    public SQLite(Context context) {
        sql = new Sql(context);
    }

    public void abrirConexion() {
        Log.i("SQLite", "Se abre la conexion a la base de datos " + sql.getDatabaseName());
        db = sql.getWritableDatabase();
    }

    public void cerraConexion() {
        Log.i("SQLite", "Se cierra la conexion a la base de datos " + sql.getDatabaseName());
        sql.close();
    }

    public boolean agregarJugador(int id, String nombre, String fechaNacimiento, String paisOrigen, String imagen, String activo) {
        ContentValues content = new ContentValues();
        content.put("id", id);
        content.put("nombre", nombre);
        content.put("fecha_nacimiento", fechaNacimiento);
        content.put("pais_origen", paisOrigen);
        content.put("imagen", imagen);
        content.put("activo", activo);

        return (db.insert("jugadores", null, content) != -1) ? true : false;
    }

    public String actualizarJugador(int id, String nombre, String fechaNacimiento, String paisOrigen, String imagen, String activo) {
        ContentValues content = new ContentValues();
        content.put("id", id);
        content.put("nombre", nombre);
        content.put("fecha_nacimiento", fechaNacimiento);
        content.put("pais_origen", paisOrigen);
        content.put("imagen", imagen);
        content.put("activo", activo);

        int valor = db.update("jugadores", content, "id = " + id, null);
        if (valor == 1) {
            return "Se actualizo correctamente el producto";
        } else {
            return "No se pudo actualizar el producto";
        }
    }

    public int eliminarJugador(String id) {
        return db.delete("jugadores", "id = " + id, null);
    }

    public Cursor obtenerJugadoresActivos() {
        return db.rawQuery("select * from jugadores where activo = 1", null);
    }

    public Cursor obtenerJugadoresInactivos() {
        return db.rawQuery("select * from jugadores where activo = 0", null);
    }

    public ArrayList<String> getJugadores(Cursor cursor) {
        ArrayList<String> lista = new ArrayList<>();
        String item = "";
        if (cursor.moveToFirst()) {
            do {
                item += "ID: " + cursor.getString(0) + "\r\n";
                item += "Producto: " + cursor.getString(1) + "\r\n";
                item += "Fecha: " + cursor.getString(2) + "\r\n";
                item += "Descripcion: " + cursor.getString(3) + "\r\n";
                if (cursor.getString(5).equals("1")) {
                    activo = "Si";
                } else {
                    activo = "No";
                }
                item += "Activo: " + activo + "\r\n";
                lista.add(item);
                item = "";
            } while (cursor.moveToNext());
        }
        return lista;
    }

    public ArrayList<String> getImagenes(Cursor cursor) {
        ArrayList<String> lista = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(4));
            } while (cursor.moveToNext());
        }
        return lista;
    }

    public Cursor getID(int id) {
        return db.rawQuery("select * from jugadores where id = " + id, null);
    }

    public String actualizarStatusJugador(int id, String activo) {
        ContentValues content = new ContentValues();
        content.put("id", id);
        content.put("activo", activo);

        int valor = db.update("jugadores", content, "id = " + id, null);
        if (valor == 1) {
            return "Producto eliminado lógicamente";
        } else {
            return "No se pudo eliminar lógicamente el Producto";
        }

    }
}











