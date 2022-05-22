package com.curso.liga.ui.editar;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.curso.liga.R;
import com.curso.liga.bd.SQLite;
import com.curso.liga.databinding.FragmentEditarBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditarFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {

    private ImageView ivFoto;
    private Button btnLimpiar, btnGuardar, btnBuscar;
    private ImageButton btnCalendarioNacimiento;
    private EditText etID, etNombre, etFechaNacimiento, etPaisOrigen;
    Spinner spinnerActivo;

    DatePickerDialog datePickerDialog;
    Calendar c;
    private int anio, mes, dia;

    String activo = "";
    public String currentFotoPath, img = "";
    public static int band = 0;
    private Uri fotoUri;
    public final int REQUEST_TAKE_FOTO = 1;

    public SQLite sqLite;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference StorageRef;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_editar, container, false);

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
        etID = root.findViewById(R.id.etEId);
        etNombre = root.findViewById(R.id.etENombre);
        etFechaNacimiento = root.findViewById(R.id.etEFechaNacimiento);
        etPaisOrigen = root.findViewById(R.id.etEPaisOrigen);

        spinnerActivo = root.findViewById(R.id.spEActivo);
        ArrayAdapter<CharSequence> activoAdapter = ArrayAdapter.createFromResource(getContext(), R.array.activo,
                R.layout.spinner_item);
        spinnerActivo.setAdapter(activoAdapter);
        spinnerActivo.setOnItemSelectedListener(this);
    }

    private void ButtonComponentes(View root) {
        btnCalendarioNacimiento = root.findViewById(R.id.ibEFechaNacimiento);
        btnGuardar = root.findViewById(R.id.btnEGuardar);
        btnLimpiar = root.findViewById(R.id.btnELimpiar);
        ivFoto = root.findViewById(R.id.ivEFoto);
        btnBuscar = root.findViewById(R.id.btnEBuscar);

        btnCalendarioNacimiento.setOnClickListener(this);
        btnGuardar.setOnClickListener(this);
        btnLimpiar.setOnClickListener(this);
        ivFoto.setOnClickListener(this);
        btnBuscar.setOnClickListener(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibEFechaNacimiento:
                if (band == 1) {
                    c = Calendar.getInstance();
                    anio = c.get(Calendar.YEAR);
                    mes = c.get(Calendar.MONTH);
                    dia = c.get(Calendar.DAY_OF_MONTH);
                    datePickerDialog = new DatePickerDialog(getContext(), null, anio, mes, dia);
                    datePickerDialog.show();
                    datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            etFechaNacimiento.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Ingrese fecha", Toast.LENGTH_SHORT).show();
                    band = 0;
                }
                break;
            case R.id.btnEBuscar:
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
                                int id = Integer.parseInt(etID.getText().toString());
                                String nombre = cursor.getString(1);
                                String fechaNacimiento = cursor.getString(2);
                                String paisOrigen = cursor.getString(3);
                                img = cursor.getString(4);
                                String activo = cursor.getString(5);

                                GetDataDocument(etID.getText().toString(), img);

                                //etNombre.setText(nombre);
                                //etFechaNacimiento.setText(fechaNacimiento);
                                //etPaisOrigen.setText(paisOrigen);
                                //cargarImagen(img, ivFoto);
                                //int a = obtenerPosicion(spinnerActivo, activo);
                                //spinnerActivo.setSelection(obtenerPosicion(spinnerActivo, activo));
                                //band = 1;

                            } while (cursor.moveToNext());
                        }
                    }
                    sqLite.cerraConexion();
                }
                break;
            case R.id.btnEGuardar:
                if (band == 1) {
                    if (etID.getText().toString().equals("") || etNombre.getText().toString().equals("") ||
                            etFechaNacimiento.getText().toString().equals("") || etPaisOrigen.getText().toString().equals("") ||
                            img.equals("") || activo.equals("")) {
                        Toast.makeText(getContext(), "Porfavor llene todos los campos", Toast.LENGTH_SHORT).show();

                    } else {
                        int id = Integer.parseInt(etID.getText().toString());
                        String nombre = etNombre.getText().toString();
                        String fechaNacimiento = etFechaNacimiento.getText().toString();
                        String paisOrigen = etPaisOrigen.getText().toString();

                        sqLite.abrirConexion();
                        String mensaje = sqLite.actualizarJugador(id, nombre, fechaNacimiento, paisOrigen, img, activo);
                        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                        AddToFirebase(id, nombre, fechaNacimiento, paisOrigen, img.split("/")[9], activo);
                        UploadPhoto(img.split("/")[9]);
                        sqLite.cerraConexion();
                    }
                } else {
                    Toast.makeText(getContext(), "Porfavor ingrese un ID", Toast.LENGTH_SHORT).show();
                    band = 0;
                }
                break;
            case R.id.ivEFoto:
                if (band == 1) {
                    Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (tomarFoto.resolveActivity(getActivity().getPackageManager()) != null) {
                        File fotoFile = null;
                        try {
                            fotoFile = createImageFile();
                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "EROR DE FOTOGRAFIA" + ex.toString(), Toast.LENGTH_SHORT).show();
                        }

                        if (fotoFile != null) {
                            fotoUri = FileProvider.getUriForFile(getContext(), "com.curso.liga", fotoFile);
                            tomarFoto.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                            startActivityForResult(tomarFoto, REQUEST_TAKE_FOTO);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Ingrese imagen", Toast.LENGTH_SHORT).show();
                    band = 0;
                }
                break;
            case R.id.btnELimpiar:
                limpiar();
                break;
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
                                    int a = obtenerPosicion(spinnerActivo, valor.toString());
                                    spinnerActivo.setSelection(obtenerPosicion(spinnerActivo, activo));
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

    private void AddToFirebase (Integer id,String nombre,String fechaNacimiento,String paisOrigen,String img,String activo){

        Map<String, Object> producto = new HashMap<>();
        producto.put("producto", nombre);
        producto.put("fecha", fechaNacimiento);
        producto.put("descripcion", paisOrigen);
        producto.put("activo", activo);
        producto.put("img", img);

        db.collection("objects").document(Integer.toString(id))
                .set(producto, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Toast.makeText(getContext(), "Se registro correctamente el producto", Toast.LENGTH_SHORT).show();
                        limpiar();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(getContext(), "No se pudo registrar el producto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void UploadPhoto(String path){
        StorageReference filePath = StorageRef.child("images").child(path);

        filePath.putFile(fotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "Se subio correctamente la foto", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "No se subio la foto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int obtenerPosicion(Spinner spinnerActivo, String item) {
        int pos = 0;
        for (int i = 0; i < spinnerActivo.getCount(); i++) {
            String a = spinnerActivo.getItemAtPosition(i).toString();
            if (a.equals("Si")) {
                a = "1";
            } else if (a.equals("No")) {
                a = "0";
            }
            if (a.equals(item)) {
                pos = i;
            }
        }
        return pos;
    }

    private File createImageFile() throws IOException {
        currentFotoPath = "";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreImagen = "imagen_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(nombreImagen, ".jpg", storageDir);
        currentFotoPath = image.getAbsolutePath();
        //Toast.makeText(getContext(), "La foto esta en " + currentFotoPath, Toast.LENGTH_SHORT).show();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        img = "";
        if (requestCode == REQUEST_TAKE_FOTO && resultCode == Activity.RESULT_OK) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(currentFotoPath);
            ivFoto.setImageBitmap(imageBitmap);
            try {
                ivFoto.setImageURI(fotoUri);
                img = currentFotoPath;
                //Toast.makeText(getContext(), "img " + img, Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                //Toast.makeText(getContext(), "Fallo en onActivityResult " + ex.toString(), Toast.LENGTH_SHORT).show();
            }
        }
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

    private void limpiar() {
        etID.setText("");
        etNombre.setText("");
        etFechaNacimiento.setText("");
        etPaisOrigen.setText("");
        ivFoto.setImageResource(R.drawable.ic_menu_camera);

        ArrayAdapter<CharSequence> activoAdapter = ArrayAdapter.createFromResource(getContext(), R.array.activo,
                R.layout.spinner_item);
        spinnerActivo.setAdapter(activoAdapter);

        etID.requestFocus();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //posicion = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spEActivo:
                switch (position) {
                    case 1:
                        activo = "1";
                        break;
                    case 2:
                        activo = "0";
                        break;
                    default:
                        activo = "";
                        break;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}