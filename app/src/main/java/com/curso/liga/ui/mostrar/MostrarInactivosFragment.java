package com.curso.liga.ui.mostrar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.curso.liga.R;
import com.curso.liga.bd.SQLite;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;

public class MostrarInactivosFragment extends Fragment {

    static ArrayList<String> registros;
    static ArrayList<String> imagenes;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static MostrarInactivosFragment newInstance() {
        return new MostrarInactivosFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mostrar_inactivos, container, false);

        ListView list = root.findViewById(R.id.lvMInactivosListaJugadores);
        SQLite sqlite = new SQLite(getContext());

        sqlite.abrirConexion();

        Cursor cursor = sqlite.obtenerJugadoresInactivos();
        registros = sqlite.getJugadores(cursor);
        imagenes = sqlite.getImagenes(cursor);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, registros);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_jugador, null);
                ((TextView) dialogView.findViewById(R.id.tvDJInformacion)).setText(registros.get(position));
                ImageView ivImagen = dialogView.findViewById(R.id.ivDJFoto);
                cargarImagen(imagenes.get(position), ivImagen);

                AlertDialog.Builder dialogo = new AlertDialog.Builder(getContext());
                dialogo.setTitle("Producto");
                dialogo.setView(dialogView);
                dialogo.setNeutralButton("Activar Producto", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = Integer.parseInt(registros.get(position).toString().substring(4, 6));
                        sqlite.abrirConexion();
                        sqlite.actualizarStatusJugador(id, "1");
                        ChangeStatus(String.valueOf(id));
                        Toast.makeText(getContext(), "Se cambio correctamente el status del producto", Toast.LENGTH_SHORT).show();
                        sqlite.cerraConexion();
                        adapter.remove(adapter.getItem(position));
                        adapter.notifyDataSetChanged();
                    }
                });
                dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialogo.show();
            }
        });

        sqlite.cerraConexion();

        return root;
    }

    private void ChangeStatus (String id){
        DocumentReference washingtonRef = db.collection("objects").document(id);

// Set the "isCapital" field of the city 'DC'
        washingtonRef.update("activo", "1");
    }

    public void cargarImagen(String imagen, ImageView iv) {
        try {
            File fileFoto = new File(imagen);
            Uri uriFoto = FileProvider.getUriForFile(getContext(), "com.curso.liga", fileFoto);
            iv.setImageURI(uriFoto);

        } catch (Exception ex) {
            Log.d("Cargar imagen", "Error al cargar imagen " + imagen + "\nMensaje: " + ex.getMessage() +
                    "\nCausa: " + ex.getCause());
        }
    }

}