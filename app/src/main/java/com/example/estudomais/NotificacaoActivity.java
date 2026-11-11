package com.example.estudomais;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificacaoActivity extends AppCompatActivity {

    private EditText editTitulo, editMensagem;
    private CheckBox checkAluno, checkResponsavel;
    private Button btnEnviar;

    private FirebaseFirestore db;
    private String remetenteId;
    private String remetenteNome = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacao);

        editTitulo       = findViewById(R.id.editTitulo);
        editMensagem     = findViewById(R.id.editMensagem);
        checkAluno       = findViewById(R.id.checkAluno);
        checkResponsavel = findViewById(R.id.checkResponsavel);
        btnEnviar        = findViewById(R.id.btnEnviar);

        db          = FirebaseFirestore.getInstance();
        remetenteId = FirebaseAuth.getInstance().getUid();

        carregarNomeProfessor();

        btnEnviar.setOnClickListener(v -> enviarNotificacao());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Enviar Notificação");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregarNomeProfessor() {
        if (remetenteId == null) return;
        db.collection("usuarios").document(remetenteId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String nome = doc.getString("nomeCompleto");
                        remetenteNome = nome != null ? nome : "";
                    }
                });
    }

    private void enviarNotificacao() {
        String titulo   = editTitulo.getText().toString().trim();
        String mensagem = editMensagem.getText().toString().trim();

        if (titulo.isEmpty() || mensagem.isEmpty()) {
            Toast.makeText(this, "Preencha título e mensagem", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> destinatarios = new ArrayList<>();
        if (checkAluno.isChecked())       destinatarios.add("ALUNO");
        if (checkResponsavel.isChecked()) destinatarios.add("RESPONSAVEL");

        if (destinatarios.isEmpty()) {
            Toast.makeText(this, "Selecione ao menos um destinatário", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviar.setEnabled(false);

        Notificacao notificacao = new Notificacao(
                titulo, mensagem, remetenteId, remetenteNome, destinatarios);

        db.collection("notificacoes")
                .add(notificacao)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Notificação enviada!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao enviar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnEnviar.setEnabled(true);
                });
    }
}
