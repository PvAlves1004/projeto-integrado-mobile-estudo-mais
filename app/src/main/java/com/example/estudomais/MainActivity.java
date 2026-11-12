package com.example.estudomais;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private EditText editIdentificador, editPassword;
    private Button btnEnter;
    private TextView tvForgotPassword;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        editIdentificador = findViewById(R.id.user);
        editPassword      = findViewById(R.id.password);
        btnEnter          = findViewById(R.id.enterButtton);
        tvForgotPassword  = findViewById(R.id.forgotPassword);

        btnEnter.setOnClickListener(v -> realizarLogin());
        tvForgotPassword.setOnClickListener(v -> enviarRedefinicaoSenha());
    }

    private void realizarLogin() {
        String identificador = editIdentificador.getText().toString().trim();
        String senha         = editPassword.getText().toString().trim();

        if (identificador.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail/matrícula e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnter.setEnabled(false);

        if (identificador.contains("@")) {
            fazerLoginComEmail(identificador, senha);
        } else {
            buscarEmailPorMatricula(identificador, email -> fazerLoginComEmail(email, senha));
        }
    }

    private void fazerLoginComEmail(String email, String senha) {
        auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(result -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "E-mail/matrícula ou senha incorretos", Toast.LENGTH_SHORT).show();
                    btnEnter.setEnabled(true);
                });
    }

    private void buscarEmailPorMatricula(String matricula, OnEmailEncontrado callback) {
        db.collection("usuarios")
                .whereEqualTo("matricula", matricula)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "Matrícula não encontrada", Toast.LENGTH_SHORT).show();
                        btnEnter.setEnabled(true);
                        return;
                    }
                    QueryDocumentSnapshot doc =
                            (QueryDocumentSnapshot) snap.getDocuments().get(0);
                    String email = doc.getString("email");
                    if (email == null || email.isEmpty()) {
                        Toast.makeText(this, "Usuário sem e-mail cadastrado", Toast.LENGTH_SHORT).show();
                        btnEnter.setEnabled(true);
                        return;
                    }
                    callback.onEncontrado(email);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao buscar matrícula", Toast.LENGTH_SHORT).show();
                    btnEnter.setEnabled(true);
                });
    }

    private void enviarRedefinicaoSenha() {
        String identificador = editIdentificador.getText().toString().trim();

        if (identificador.isEmpty()) {
            Toast.makeText(this, "Digite seu e-mail ou matrícula", Toast.LENGTH_SHORT).show();
            return;
        }

        if (identificador.contains("@")) {
            dispararResetEmail(identificador);
        } else {
            buscarEmailPorMatricula(identificador, this::dispararResetEmail);
        }
    }

    private void dispararResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "E-mail de redefinição enviado!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Não foi possível enviar o e-mail", Toast.LENGTH_SHORT).show());
    }

    interface OnEmailEncontrado {
        void onEncontrado(String email);
    }
}
