package com.curso.liga.ui.mostrar;

import static android.content.ContentValues.TAG;

import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.curso.liga.ui.editar.EditarFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class MostrarFragment extends Fragment {

    static ArrayList<String> registros;
    static ArrayList<String> imagenes;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference StorageRef;

    public static MostrarFragment newInstance() {
        return new MostrarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_mostrar, container, false);

        ListView list = root.findViewById(R.id.lvMListaJugadores);
        SQLite sqlite = new SQLite(getContext());

        sqlite.abrirConexion();
        StorageRef = FirebaseStorage.getInstance().getReference();

        Cursor cursor = sqlite.obtenerJugadoresActivos();
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
                dialogo.setNegativeButton("Editar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Fragment fragment = new EditarFragment();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host_fragment_content_main, fragment);
                        fragmentTransaction.commit();
                    }
                });
                dialogo.setNeutralButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = Integer.parseInt(registros.get(position).toString().substring(4, 6));
                        sqlite.abrirConexion();
                        sqlite.eliminarJugador(String.valueOf(id));
                        DeletePhotoAndDocument(String.valueOf(id));
                        Toast.makeText(getContext(), "Producto eliminado fisicamente", Toast.LENGTH_SHORT).show();
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

    private void DeleteDocument (String id){
        db.collection("objects").document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "El producto fue eliminado");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "No se pudo eliminar", e);
                    }
                });

    }

    private void DeletePhotoAndDocument (String id){
        ////////////////// firebase

        DocumentReference docRef = db.collection("objects").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> producto = document.getData();

                        for (String clave:producto.keySet()) {
                            Object valor = producto.get(clave);
                            switch (clave){
                                case "img":
                                    //System.out.println(valor);
                                    //cargarImagen(pathImg, ivFoto);
                                    StorageReference ImageSorage = StorageRef.child("images/" + valor.toString());

                                    ImageSorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(), "images/ bueno" + valor.toString(), Toast.LENGTH_SHORT).show();
                                            DeleteDocument(id);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Toast.makeText(getContext(), "images/ error" + valor.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                default:
                                    System.out.println("no existe el dato");
                            }
                        }
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(getContext(), "El producto no se encuentra", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al consultar el producto", Toast.LENGTH_SHORT).show();
                }
            }
        });
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