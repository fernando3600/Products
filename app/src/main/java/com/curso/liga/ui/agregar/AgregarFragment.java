package com.curso.liga.ui.agregar;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class AgregarFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {

    private ImageView ivFoto;
    private Button btnLimpiar, btnGuardar;
    private ImageButton btnCalendarioNacimiento;
    private EditText etID, etNombre, etFechaNacimiento, etPaisOrigen;
    Spinner spinnerActivo;

    DatePickerDialog datePickerDialog;
    Calendar c;
    private int anio, mes, dia;

    public String currentFotoPath = "", img = "";
    String activo = "";
    private Uri fotoUri;
    public final int REQUEST_TAKE_FOTO = 1;

    public SQLite sqLite;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference StorageRef;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("server/saving-data/fireblog");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_agregar, container, false);

        sqLite = new SQLite(getContext());

        /////////////////////////////
        StorageRef = FirebaseStorage.getInstance().getReference();


        StorageReference ref = StorageRef.child("images");

        ref.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    System.out.println(item.getPath() +"  name: *************************************************************************** "+ item.getName());
                }
            }
        });
        //////////////////////////////////////

        Componentes(root);
        return root;
    }

    private void Componentes(View root) {
        EditTextComponentes(root);
        ButtonComponentes(root);
    }

    private void AddToFirebase (Integer id,String nombre,String fechaNacimiento,String paisOrigen,String img,String activo){

        Map<String, Object> producto = new HashMap<>();
        producto.put("producto", nombre);
        producto.put("fecha", fechaNacimiento);
        producto.put("descripcion", paisOrigen);
        producto.put("activo", activo);
        producto.put("img", img);

        db.collection("objects").document(Integer.toString(id))
                .set(producto)
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

    private void EditTextComponentes(View root) {
        etID = root.findViewById(R.id.etAId);
        etNombre = root.findViewById(R.id.etANombre);
        etFechaNacimiento = root.findViewById(R.id.etAFechaNacimiento);
        etPaisOrigen = root.findViewById(R.id.etAPaisOrigen);

        spinnerActivo = root.findViewById(R.id.spAActivo);
        ArrayAdapter<CharSequence> activoAdapter = ArrayAdapter.createFromResource(getContext(), R.array.activo,
                R.layout.spinner_item);
        spinnerActivo.setAdapter(activoAdapter);
        spinnerActivo.setOnItemSelectedListener(this);
    }

    private void ButtonComponentes(View root) {
        btnCalendarioNacimiento = root.findViewById(R.id.ibAFechaNacimiento);
        btnGuardar = root.findViewById(R.id.btnAGuardar);
        btnLimpiar = root.findViewById(R.id.btnALimpiar);
        ivFoto = root.findViewById(R.id.ivAFoto);

        btnCalendarioNacimiento.setOnClickListener(this);
        btnGuardar.setOnClickListener(this);
        btnLimpiar.setOnClickListener(this);
        ivFoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibAFechaNacimiento:
                c = Calendar.getInstance();
                anio = c.get(Calendar.YEAR);
                mes = c.get(Calendar.MONTH);
                dia = c.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(getContext(), null, anio, mes, dia);
                datePickerDialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            etFechaNacimiento.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        }
                    });
                }
                break;
            case R.id.btnAGuardar:
                if (etID.getText().toString().equals("")  || etNombre.getText().toString().equals("") ||
                        etFechaNacimiento.getText().toString().equals("") || etPaisOrigen.getText().toString().equals("") ||
                        img.equals("") || activo.equals("")) {
                    Toast.makeText(getContext(), "Porfavor llene todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    int id = Integer.parseInt(etID.getText().toString());
                    String nombre = etNombre.getText().toString();
                    String fechaNacimiento = etFechaNacimiento.getText().toString();
                    String paisOrigen = etPaisOrigen.getText().toString();

                    AddToFirebase(id, nombre, fechaNacimiento, paisOrigen, img.split("/")[9], activo);
                    UploadPhoto(img.split("/")[9]);
                    System.out.println("AQUI EMPIEZAN LOS DATOS");
                   // System.out.println("ID = " + id);
                    //System.out.println("Nombre = " + nombre);
                    //System.out.println("Fecha Nacimiento = " + fechaNacimiento);
                    //System.out.println("Pais = " + paisOrigen);
                    //System.out.println("IMG = " + img);
                    //System.out.println("Activo = " + activo);

                    // Create a Cloud Storage reference from the app
                    sqLite.abrirConexion();
                    boolean band = sqLite.agregarJugador(id, nombre, fechaNacimiento, paisOrigen,
                            img, activo);
                    sqLite.cerraConexion();
                }
                break;
            case R.id.ivAFoto:
                Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (tomarFoto.resolveActivity(getActivity().getPackageManager()) != null) {
                    File fotoFile = null;
                    try {
                        fotoFile = createImageFile();
                    } catch (Exception ex) {
                        Toast.makeText(getContext(), "ERROR DE FOTOGRAFIA " + ex.toString(), Toast.LENGTH_SHORT).show();
                    }

                    if (fotoFile != null) {
                        fotoUri = FileProvider.getUriForFile(getContext(), "com.curso.liga", fotoFile);
                        tomarFoto.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                        startActivityForResult(tomarFoto, REQUEST_TAKE_FOTO);
                    }
                }
                break;
            case R.id.btnALimpiar:
                limpiar();
                break;
        }
    }

    private File createImageFile() throws IOException {
        currentFotoPath = "";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreImagen = "imagen_" + timeStamp;
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
                Toast.makeText(getContext(), "img " + img.split("/")[9], Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                //Toast.makeText(getContext(), "Fallo en onActivityResult " + ex.toString(), Toast.LENGTH_SHORT).show();
            }
        }
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spAActivo:
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //posicion = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}