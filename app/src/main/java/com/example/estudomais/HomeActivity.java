package com.example.estudomais;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends AppCompatActivity {

    private TextView tvBoasVindas, tvPerfil;
    private Button btnCadastrarUsuario, btnGerenciarUsuarios, btnGerenciarTurmas;
    private Button btnMinhasTurmas;
    private Button btnMinhasTarefas, btnCalendario;
    private Button btnSair;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvBoasVindas         = findViewById(R.id.tvBoasVindas);
        tvPerfil             = findViewById(R.id.tvPerfil);
        btnCadastrarUsuario  = findViewById(R.id.btnCadastrarUsuario);
        btnGerenciarUsuarios = findViewById(R.id.btnGerenciarUsuarios);
        btnGerenciarTurmas   = findViewById(R.id.btnGerenciarTurmas);
        btnMinhasTurmas      = findViewById(R.id.btnMinhasTurmas);
        btnMinhasTarefas     = findViewById(R.id.btnMinhasTarefas);
        btnCalendario        = findViewById(R.id.btnCalendario);
        btnSair              = findViewById(R.id.btnSair);

        db  = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        solicitarPermissaoNotificacao();
        carregarPerfil();

        btnSair.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void carregarPerfil() {
        if (uid == null) return;

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        encerrarAcesso("Perfil não encontrado. Contate o administrador.");
                        return;
                    }

                    Boolean ativo = doc.getBoolean("ativo");
                    if (Boolean.FALSE.equals(ativo)) {
                        encerrarAcesso("Usuário inativo. Contate o administrador.");
                        return;
                    }

                    String nome   = doc.getString("nomeCompleto");
                    String perfil = doc.getString("perfilId");
                    if (perfil == null || perfil.isEmpty())
                        perfil = doc.getString("perfilID");
                    if (perfil == null) perfil = "";

                    tvBoasVindas.setText("Olá, " + (nome != null ? nome : "usuário") + "!");
                    tvPerfil.setText(perfil);

                    inscreverTopico(perfil);
                    configurarUIPorPerfil(perfil);
                    agendarSincronizacao();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar perfil: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void encerrarAcesso(String motivo) {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, motivo, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void configurarUIPorPerfil(String perfil) {
        switch (perfil.toUpperCase()) {
            case "ADMIN":
                btnCadastrarUsuario.setVisibility(View.VISIBLE);
                btnGerenciarUsuarios.setVisibility(View.VISIBLE);
                btnGerenciarTurmas.setVisibility(View.VISIBLE);

                btnCadastrarUsuario.setOnClickListener(v ->
                        startActivity(new Intent(this, CadastroActivity.class)));
                btnGerenciarUsuarios.setOnClickListener(v ->
                        startActivity(new Intent(this, UsuariosActivity.class)));
                btnGerenciarTurmas.setOnClickListener(v ->
                        startActivity(new Intent(this, TurmasActivity.class)));
                break;

            case "PROFESSOR":
                btnMinhasTurmas.setVisibility(View.VISIBLE);
                btnMinhasTurmas.setOnClickListener(v -> {
                    Intent i = new Intent(this, TurmasActivity.class);
                    i.putExtra("modoProfessor", true);
                    i.putExtra("uid", uid);
                    startActivity(i);
                });
                break;

            default: // ALUNO
                btnMinhasTarefas.setVisibility(View.VISIBLE);
                btnCalendario.setVisibility(View.VISIBLE);
                btnMinhasTarefas.setOnClickListener(v ->
                        startActivity(new Intent(this, ListaTarefasActivity.class)));
                btnCalendario.setOnClickListener(v ->
                        startActivity(new Intent(this, CalendarioActivity.class)));
                break;
        }
    }

    private void inscreverTopico(String perfil) {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        switch (perfil.toUpperCase()) {
            case "ALUNO":     fm.subscribeToTopic("alunos");      break;
            case "PROFESSOR": fm.subscribeToTopic("professores"); break;
        }
    }

    private void agendarSincronizacao() {
        SyncWorker.agendar(this);
    }
}
