package com.example.estudomais;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditarUsuarioActivity extends AppCompatActivity {

    private EditText editNome, editTelefone, editNascimento;
    private TextView tvEmail, tvPerfil, tvMatricula;
    private Button btnSalvar, btnToggleAtivo;

    private FirebaseFirestore db;
    private String uid;
    private boolean ativoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        editNome       = findViewById(R.id.editNome);
        editTelefone   = findViewById(R.id.editTelefone);
        editNascimento = findViewById(R.id.editNascimento);
        tvMatricula    = findViewById(R.id.tvMatricula);
        tvEmail        = findViewById(R.id.tvEmail);
        tvPerfil       = findViewById(R.id.tvPerfil);
        btnSalvar      = findViewById(R.id.btnSalvar);
        btnToggleAtivo = findViewById(R.id.btnToggleAtivo);

        db  = FirebaseFirestore.getInstance();
        uid = getIntent().getStringExtra("uid");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar Usuário");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        carregarDados();

        btnSalvar.setOnClickListener(v -> salvar());
        btnToggleAtivo.setOnClickListener(v -> confirmarToggle());
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void carregarDados() {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    editNome.setText(doc.getString("nomeCompleto"));
                    editTelefone.setText(doc.getString("telefone"));
                    editNascimento.setText(doc.getString("nascimento"));
                    tvMatricula.setText(doc.getString("matricula"));
                    tvEmail.setText(doc.getString("email"));
                    tvPerfil.setText(doc.getString("perfilId"));
                    Boolean ativo = doc.getBoolean("ativo");
                    ativoAtual = Boolean.TRUE.equals(ativo);
                    btnToggleAtivo.setText(ativoAtual ? "Desativar" : "Ativar");
                    btnToggleAtivo.setBackgroundTintList(
                            androidx.core.content.res.ResourcesCompat.getColorStateList(
                                    getResources(),
                                    ativoAtual ? android.R.color.holo_red_dark
                                               : android.R.color.holo_green_dark,
                                    null));
                });
    }

    private void salvar() {
        String nome       = editNome.getText().toString().trim();
        String telefone   = editTelefone.getText().toString().trim();
        String nascimento = editNascimento.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(this, "Nome obrigatório", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> dados = new HashMap<>();
        dados.put("nomeCompleto", nome);
        dados.put("telefone", telefone);
        dados.put("nascimento", nascimento);

        btnSalvar.setEnabled(false);
        db.collection("usuarios").document(uid)
                .update(dados)
                .addOnSuccessListener(u -> {
                    Toast.makeText(this, "Dados atualizados!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSalvar.setEnabled(true);
                });
    }

    private void confirmarToggle() {
        String acao = ativoAtual ? "desativar" : "ativar";
        new AlertDialog.Builder(this)
                .setTitle("Confirmar")
                .setMessage("Deseja " + acao + " este usuário?")
                .setPositiveButton("Sim", (d, w) -> toggleAtivo())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void toggleAtivo() {
        db.collection("usuarios").document(uid)
                .update("ativo", !ativoAtual)
                .addOnSuccessListener(u -> {
                    ativoAtual = !ativoAtual;
                    btnToggleAtivo.setText(ativoAtual ? "Desativar" : "Ativar");
                    Toast.makeText(this,
                            "Usuário " + (ativoAtual ? "ativado" : "desativado"),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
