package com.curso.liga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";
    private static final int GOOGLE_SING_IN = 100;

    Button btnIngresar, btnRegistrarse, btnOlvidarContraseña;
    EditText etEmail, etContraseña;
    ProgressDialog progressDialog;
    LinearLayout linear;

    private FirebaseAuth firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

// ...
// Initialize Firebase Auth
        firebase = FirebaseAuth.getInstance();

        inicializarComponentes();

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accederUsuario();
            }
        });

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, Registrarse.class);
                //startActivity(intent);
            }
        });

        btnOlvidarContraseña.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, RestablecerContrasenia.class));
            }
        });

        verificarSession();

    }

    private void verificarSession() {

        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);

        String e = preferences.getString("email", null);
        String c = preferences.getString("contraseña", null);

        if (e != null & c != null) {
            etEmail.setText(e);
            etContraseña.setText(c);
            linear.setVisibility(View.INVISIBLE);
            accederUsuario();
        }

    }

    private void inicializarComponentes() {
        firebase = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etMEmail);
        etContraseña = findViewById(R.id.etMContraseña);
        btnIngresar = findViewById(R.id.btnMIngresar);
        btnRegistrarse = findViewById(R.id.btnMRegistrarse);
        btnOlvidarContraseña = findViewById(R.id.btnMOlvidarContraseña);
        progressDialog = new ProgressDialog(this);
        linear = findViewById(R.id.llMLinearLayout);
        etEmail.setText("");
        etContraseña.setText("");
    }


    private void accederUsuario() {
        String email = etEmail.getText().toString();
        String contrasenia = etContraseña.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Porfavor ingrese un email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(contrasenia)) {
            Toast.makeText(this, "Porfavor ingrese una contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contrasenia.length() <= 5) {
            Toast.makeText(this, "La contraseña debe tener minimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Accediendo");
        progressDialog.show();

        firebase.signInWithEmailAndPassword(email, contrasenia).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(login.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(login.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    alerta();
                }
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        linear.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = firebase.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(login.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void alerta() {
        AlertDialog.Builder alert = new AlertDialog.Builder(login.this);
        alert.setTitle("ERROR!");
        alert.setMessage("Se ha producido un error autenticando al usuario");
        alert.setPositiveButton("Aceptar", null);
        alert.show();
    }
}