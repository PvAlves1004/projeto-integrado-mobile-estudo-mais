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
    private TextView tvForgotPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        auth = FirebaseAuth.getInstance();

        editEmail        = findViewById(R.id.user);
        editPassword     = findViewById(R.id.password);
        btnEnter         = findViewById(R.id.enterButtton);
        tvForgotPassword = findViewById(R.id.forgotPassword);

        btnEnter.setOnClickListener(v -> realizarLogin());
        tvForgotPassword.setOnClickListener(v -> enviarRedefinicaoSenha());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual != null) {
            verificarPerfilEDirecionar(usuarioAtual.getUid());
        }
    }

    private void verificarPerfilEDirecionar(String uid) {
        FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    // TODO 1: se o documento não existir (doc.exists()), chame negarAcesso() e return

                    // TODO 2: converta o documento: Usuario usuario = doc.toObject(Usuario.class);

                    // TODO 3: se o usuário não estiver ativo, chame negarAcesso() e return

                    // TODO 4: switch (usuario.getPerfilId()) com um case para
                    //         "ADMIN", "PROFESSOR" e "ALUNO", abrindo a Home certa.
                    //         default: negarAcesso()
                    //         Depois de abrir a tela: finish()
                })
                .addOnFailureListener(e -> {
                    // TODO 5: Toast "Erro de conexão" e reabilitar o btnEnter
                });
    }

    private void negarAcesso() {
        // TODO: auth.signOut(), Toast "Acesso não autorizado", btnEnter.setEnabled(true)
    }




    private void enviarRedefinicaoSenha() {
        String email = editEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Digite seu e-mail para redefinir a senha", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "E-mail de redefinição enviado!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "E-mail não encontrado", Toast.LENGTH_SHORT).show());
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
                    verificarPerfilEDirecionar(result.getUser().getUid());;
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                    btnEnter.setEnabled(true);
                });
    }
}
