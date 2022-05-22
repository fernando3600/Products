package com.curso.liga.ui.eliminar;

import static android.content.ContentValues.TAG;

import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.curso.liga.R;
import com.curso.liga.bd.SQLite;

import java.io.File;
import java.util.Calendar;
import java.util.Map;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EliminarFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {

    private EliminarViewModel mViewModel;

    private ImageView ivFoto;
    private Button btnLimpiar, btnEliminar, btnBuscar;
    private TextView etID, etNombre, etFechaNacimiento, etPaisOrigen, etActivo;

    public String img = "";
    public static int band = 0;

    public SQLite sqLite;

    int id;
    String nombre = "", fechaNacimiento = "", paisOrigen = "", activo = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference StorageRef;

    public static EliminarFragment newInstance() {
        return new EliminarFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_eliminar, container, false);

        sqLite = new SQLite(getContext());
        StorageRef = FirebaseStorage.getInstance().getReference();


        Componentes(root);

        return root;
    }

    private void Componentes(View root) {
        EditTextComponentes(root);
        ButtonComponentes(root);
    }

    private void EditTextComponentes(View root) {
        etID = root.findViewById(R.id.etEliId);
        etNombre = root.findViewById(R.id.etEliNombre);
        etFechaNacimiento = root.findViewById(R.id.etEliFechaNacimiento);
        etPaisOrigen = root.findViewById(R.id.etEliPaisOrigen);
        etActivo = root.findViewById(R.id.etEliActivo);

    }

    private void ButtonComponentes(View root) {
        btnEliminar = root.findViewById(R.id.btnEliEliminar);
        btnLimpiar = root.findViewById(R.id.btnEliLimpiar);
        ivFoto = root.findViewById(R.id.ivEliFoto);
        btnBuscar = root.findViewById(R.id.btnEliBuscar);

        btnEliminar.setOnClickListener(this);
        btnLimpiar.setOnClickListener(this);
        ivFoto.setOnClickListener(this);
        btnBuscar.setOnClickListener(this);
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
                        band = 1;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EliminarViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEliBuscar:
                if (etID.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Porfavo ingrese un ID", Toast.LENGTH_SHORT).show();
                    band = 0;
                } else {
                    sqLite.abrirConexion();
                    int idj = Integer.parseInt(etID.getText().toString());
                    if (sqLite.getID(idj).getCount() == 1) {
                        Cursor cursor = sqLite.getID(idj);
                        if (cursor.moveToFirst()) {
                            do {
                                id = Integer.parseInt(etID.getText().toString());
                                nombre = cursor.getString(1);
                                fechaNacimiento = cursor.getString(2);
                                paisOrigen = cursor.getString(3);
                                img = cursor.getString(4);
                                activo = cursor.getString(5);
                                GetDataDocument(etID.getText().toString(), img);
                                //etNombre.setText("Nombre: " + nombre);
                                //etFechaNacimiento.setText("Fecha de nacimiento: " + fechaNacimiento);
                                //etPaisOrigen.setText("Pais de origen: " + paisOrigen);
                                //cargarImagen(img, ivFoto);
                                //if (activo.equals("1")) {
                                //    activo = "Si";
                                //} else {
                                //    activo = "No";
                                //}
                                //etActivo.setText("Activo " + activo);
                                //band = 1;

                            } while (cursor.moveToNext());
                        }
                    } else {
                        Toast.makeText(getContext(), "No existe el ID " + idj, Toast.LENGTH_SHORT).show();
                        band = 0;
                    }
                    sqLite.cerraConexion();
                }
                break;
            case R.id.btnEliEliminar:
                if (band == 1) {
                    View dialogoView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_jugador, null);
                    ((TextView) dialogoView.findViewById(R.id.tvDJInformacion)).setText("¿Desea eliminar el siguiente producto " +
                            "de forma fisica o lógica? \n" +
                            "ID: " + id + "\n" +
                            "Producto: " + nombre + "\n" +
                            "Fecha: " + fechaNacimiento + "\n" +
                            "Descripción: " + paisOrigen + "\n" +
                            "Activo: " + activo + "\n");

                    ImageView image = dialogoView.findViewById(R.id.ivDJFoto);
                    cargarImagen(img, image);
                    AlertDialog.Builder dialogo = new AlertDialog.Builder(getContext());
                    dialogo.setTitle("Importante");
                    dialogo.setView(dialogoView);
                    dialogo.setCancelable(false);
                    dialogo.setPositiveButton("Fisica", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DeletePhotoAndDocument(etID.getText().toString());
                            //DeleteDocument(etID.getText().toString());
                            sqLite.abrirConexion();
                            sqLite.eliminarJugador(etID.getText().toString());
                            limpiar();
                            Toast.makeText(getContext(), "Producto eliminado fisicamente", Toast.LENGTH_SHORT).show();
                            sqLite.cerraConexion();
                            band = 0;
                        }
                    });
                    dialogo.setNegativeButton("Lógica", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (activo.equals("No")) {
                                Toast.makeText(getContext(), "El producto ya esta inactivo", Toast.LENGTH_SHORT).show();
                            } else if (activo.equals("Si")) {
                                DeleteDocumentLogic(etID.getText().toString());
                                sqLite.abrirConexion();
                                String mensaje = sqLite.actualizarStatusJugador(id, "0");
                                limpiar();
                                Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                                sqLite.cerraConexion();
                                band = 0;
                            }
                        }
                    });
                    dialogo.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialogo.show();
                } else {
                    Toast.makeText(getContext(), "Porfavor ingrese un ID", Toast.LENGTH_SHORT).show();
                    band = 0;
                }
                break;
            case R.id.btnEliLimpiar:
                limpiar();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }
    private void GetDataDocument (String id, String pathImg){
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
                                case "descripcion":
                                    System.out.println(valor);
                                    etPaisOrigen.setText(valor.toString());
                                    break;
                                case "fecha":
                                    System.out.println(valor);
                                    etFechaNacimiento.setText(valor.toString());
                                    break;
                                case "img":
                                    System.out.println(valor);
                                    cargarImagen(pathImg, ivFoto);
                                    break;
                                case "producto":
                                    System.out.println(valor);
                                    etNombre.setText(valor.toString());
                                    break;
                                case "activo":
                                    System.out.println(valor);
                                    if (valor.toString().equals("1")) {
                                        activo = "Si";
                                    } else {
                                        activo = "No";
                                    }
                                    etActivo.setText("Activo " + activo);
                                    break;
                                default:
                                    System.out.println("no existe el dato");
                            }
                        }
                        band = 1;
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

    private void DeleteDocumentLogic (String id){
        DocumentReference washingtonRef = db.collection("objects").document(id);

// Set the "isCapital" field of the city 'DC'
        washingtonRef.update("activo", "0");
    }

    private void limpiar() {
        etID.setText("");
        etNombre.setText("");
        etFechaNacimiento.setText("");
        etPaisOrigen.setText("");
        ivFoto.setImageResource(R.drawable.ic_menu_camera);
        etActivo.setText("");

        etID.requestFocus();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}