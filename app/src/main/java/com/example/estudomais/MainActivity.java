package com.example.estudomais;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnEnter;
    private TextView tvRegister;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        editEmail    = findViewById(R.id.user);
        editPassword = findViewById(R.id.password);
        btnEnter     = findViewById(R.id.enterButtton);
        tvRegister   = findViewById(R.id.registerButton);

        btnEnter.setOnClickListener(v -> realizarLogin());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));
    }

    private void realizarLogin() {
        String email = editEmail.getText().toString().trim();
        String senha = editPassword.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnter.setEnabled(false);

        auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();
                    // TODO: navegar para a tela principal do app
                    btnEnter.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                    btnEnter.setEnabled(true);
                });
    }
}
