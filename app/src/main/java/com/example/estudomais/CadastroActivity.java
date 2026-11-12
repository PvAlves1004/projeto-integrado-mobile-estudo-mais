package com.example.estudomais;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.SecureRandom;

public class CadastroActivity extends AppCompatActivity {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";

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

        btnRegister.setOnClickListener(v -> realizarCadastro());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cadastrar Usuário");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void realizarCadastro() {
        String nome       = editName.getText().toString().trim();
        String email      = editEmail.getText().toString().trim();
        String telefone   = editTelefone.getText().toString().trim();
        String nascimento = editNascimento.getText().toString().trim();
        String matricula  = editMatricula.getText().toString().trim();
        String perfil     = spinnerPerfil.getSelectedItem().toString();

        if (nome.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Preencha nome e e-mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        String senhaAleatoria = gerarSenha();

        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("secondary");
        } catch (IllegalStateException e) {
            secondaryApp = FirebaseApp.initializeApp(this,
                    FirebaseApp.getInstance().getOptions(), "secondary");
        }

        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseApp appRef = secondaryApp;

        secondaryAuth.createUserWithEmailAndPassword(email, senhaAleatoria)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        Toast.makeText(this, "Erro ao criar usuário", Toast.LENGTH_SHORT).show();
                        btnRegister.setEnabled(true);
                        return;
                    }

                    Usuario usuario = new Usuario(nome, email, telefone, nascimento, matricula, perfil);

                    db.collection("usuarios").document(user.getUid())
                            .set(usuario)
                            .addOnSuccessListener(unused -> {
                                // Envia e-mail para o usuário definir a própria senha (RF08)
                                secondaryAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(t -> {
                                            secondaryAuth.signOut();
                                            try { appRef.delete(); } catch (Exception ignored) {}
                                        });
                                Toast.makeText(this,
                                        nome + " cadastrado! E-mail enviado para definição de senha.",
                                        Toast.LENGTH_LONG).show();
                                limparCampos();
                                btnRegister.setEnabled(true);
                            })
                            .addOnFailureListener(e -> {
                                // Reverte a criação no Auth se o Firestore falhar — delete antes de limpar o app
                                android.util.Log.e("Cadastro", "Firestore falhou: " + e.getMessage(), e);
                                user.delete().addOnCompleteListener(t -> {
                                    secondaryAuth.signOut();
                                    try { appRef.delete(); } catch (Exception ignored) {}
                                });
                                Toast.makeText(this, "Erro ao salvar dados: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                btnRegister.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("Cadastro", "Auth falhou: " + e.getMessage(), e);
                    Toast.makeText(this, "Erro ao criar conta: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnRegister.setEnabled(true);
                });
    }

    private String gerarSenha() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private void limparCampos() {
        editName.setText("");
        editEmail.setText("");
        editTelefone.setText("");
        editNascimento.setText("");
        editMatricula.setText("");
        spinnerPerfil.setSelection(0);
    }
}
