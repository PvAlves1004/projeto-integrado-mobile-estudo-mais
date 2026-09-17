package com.example.estudomais;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroActivity extends AppCompatActivity {

    private EditText editName, editEmail, editTelefone, editNascimento, editMatricula, editPassword;
    private Spinner spinnerPerfil;
    private Button btnRegister;
    private TextView backToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editName       = findViewById(R.id.editName);
        editEmail      = findViewById(R.id.editEmail);
        editTelefone   = findViewById(R.id.editTelefone);
        editNascimento = findViewById(R.id.editDate);
        editMatricula  = findViewById(R.id.editRegist);
        editPassword   = findViewById(R.id.editPass);
        spinnerPerfil  = findViewById(R.id.spinnerPerfil);
        btnRegister    = findViewById(R.id.registerButton);
        backToLogin    = findViewById(R.id.backToLogin);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"ALUNO", "PROFESSOR", "RESPONSAVEL"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPerfil.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> realizarCadastro());

        backToLogin.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
    }

    private void realizarCadastro() {
        String nome       = editName.getText().toString().trim();
        String email      = editEmail.getText().toString().trim();
        String telefone   = editTelefone.getText().toString().trim();
        String nascimento = editNascimento.getText().toString().trim();
        String matricula  = editMatricula.getText().toString().trim();
        String senha      = editPassword.getText().toString().trim();
        String perfil     = spinnerPerfil.getSelectedItem().toString();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha nome, e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        Toast.makeText(this, "Erro ao obter usuário criado", Toast.LENGTH_SHORT).show();
                        btnRegister.setEnabled(true);
                        return;
                    }

                    Usuario usuario = new Usuario(nome, email, telefone, nascimento, matricula, perfil);

                    db.collection("usuarios").document(user.getUid())
                            .set(usuario)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao salvar dados: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                btnRegister.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro no cadastro: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnRegister.setEnabled(true);
                });
    }
}
