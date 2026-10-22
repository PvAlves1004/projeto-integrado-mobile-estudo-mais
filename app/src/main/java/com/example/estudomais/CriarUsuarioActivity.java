package com.example.estudomais;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CriarUsuarioActivity extends AppCompatActivity {

    private EditText editName, editEmail, editTelefone, editNascimento, editMatricula;
    private Spinner spinnerPerfil;
    private Button btnRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editName       = findViewById(R.id.editName);
        editEmail      = findViewById(R.id.editEmail);
        editTelefone   = findViewById(R.id.editTelefone);
        editNascimento = findViewById(R.id.editDate);
        editMatricula  = findViewById(R.id.editRegist);
        spinnerPerfil  = findViewById(R.id.spinnerPerfil);
        btnRegister    = findViewById(R.id.registerButton);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"ALUNO", "PROFESSOR"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPerfil.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> criarUsuario());


    }

    private FirebaseAuth obterAuthSecundario() {
        FirebaseApp appPrincipal = FirebaseApp.getInstance();
        FirebaseApp appSecundario;
        try {
            appSecundario = FirebaseApp.getInstance("secundario");
        } catch (IllegalStateException e) {
            appSecundario = FirebaseApp.initializeApp(this, appPrincipal.getOptions(), "secundario");
        }
        return FirebaseAuth.getInstance(appSecundario);
    }

    private String gerarSenhaAleatoria() {
        // TODO: use java.security.SecureRandom
        // Monte uma String de 16 caracteres sorteados de
        // "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
        // Dica: StringBuilder + laço for + random.nextInt(caracteres.length())
        return null;
    }

    private void criarUsuario() {
        String nome       = editName.getText().toString().trim();
        String email      = editEmail.getText().toString().trim();
        String telefone   = editTelefone.getText().toString().trim();
        String nascimento = editNascimento.getText().toString().trim();
        String matricula  = editMatricula.getText().toString().trim();
        String perfil     = spinnerPerfil.getSelectedItem().toString();
        // TODO B: validar nome e e-mail obrigatórios e o formato do e-mail
        //         (Patterns.EMAIL_ADDRESS.matcher(email).matches())

        btnRegister.setEnabled(false);
        FirebaseAuth authSecundario = obterAuthSecundario();

        authSecundario.createUserWithEmailAndPassword(email, gerarSenhaAleatoria())
                .addOnSuccessListener(result -> {
                    FirebaseUser novoUser = result.getUser();
                    Usuario novo = new Usuario(nome, email, telefone, nascimento, matricula, perfil);

                    // Grava pela instância PRINCIPAL, onde o admin está logado
                    FirebaseFirestore.getInstance()
                            .collection("usuarios").document(novoUser.getUid())
                            .set(novo)
                            .addOnSuccessListener(unused -> {
                                // TODO C: authSecundario.sendPasswordResetEmail(email)
                                // TODO D: authSecundario.signOut()
                                // TODO E: Toast de sucesso, limpar os campos, reabilitar o botão
                            })
                            .addOnFailureListener(e -> {
                                // TODO F: a conta foi criada mas o documento não.
                                //         Apague a conta para não sobrar usuário órfão:
                                //         novoUser.delete(), depois authSecundario.signOut(),
                                //         Toast com o erro e reabilitar o botão
                            });
                })
                .addOnFailureListener(e -> {
                    // TODO G: Toast com o erro (ex.: e-mail já em uso) e reabilitar o botão
                });



    }
}
