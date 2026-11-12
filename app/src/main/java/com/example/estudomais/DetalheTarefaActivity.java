package com.example.estudomais;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetalheTarefaActivity extends AppCompatActivity {

    private TextView tvTitulo, tvStatus, tvTurma, tvPrazo, tvValeNota, tvValeNotaLabel, tvDescricao;
    private Button btnToggle;

    private FirebaseFirestore db;
    private String uid, turmaId, tarefaId, turmaNome;
    private boolean concluida;
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_tarefa);

        tvTitulo       = findViewById(R.id.tvTitulo);
        tvStatus       = findViewById(R.id.tvStatus);
        tvTurma        = findViewById(R.id.tvTurma);
        tvPrazo        = findViewById(R.id.tvPrazo);
        tvValeNota     = findViewById(R.id.tvValeNota);
        tvValeNotaLabel = findViewById(R.id.tvValeNotaLabel);
        tvDescricao    = findViewById(R.id.tvDescricao);
        btnToggle      = findViewById(R.id.btnToggleConcluida);

        db        = FirebaseFirestore.getInstance();
        uid       = FirebaseAuth.getInstance().getUid();
        turmaId   = getIntent().getStringExtra("turmaId");
        tarefaId  = getIntent().getStringExtra("tarefaId");
        turmaNome = getIntent().getStringExtra("turmaNome");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalhe da Tarefa");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        carregarDados();
        btnToggle.setOnClickListener(v -> toggleConcluida());
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void carregarDados() {
        db.collection("turmas").document(turmaId)
                .collection("tarefas").document(tarefaId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    Tarefa t = doc.toObject(Tarefa.class);
                    if (t == null) return;

                    tvTitulo.setText(t.getTitulo());
                    tvTurma.setText(turmaNome != null ? turmaNome : t.getTurmaNome());
                    tvDescricao.setText(t.getDescricao() != null ? t.getDescricao() : "");
                    if (t.getPrazo() != null)
                        tvPrazo.setText(SDF.format(t.getPrazo().toDate()));

                    if (t.isValeNota()) {
                        tvValeNotaLabel.setVisibility(View.VISIBLE);
                        tvValeNota.setVisibility(View.VISIBLE);
                        String nota = t.getNotaMaxima() != null
                                ? "Vale nota — máximo " + t.getNotaMaxima() + " pontos"
                                : "Vale nota";
                        tvValeNota.setText(nota);
                    }

                    // Check if already concluded
                    db.collection("usuarios").document(uid)
                            .collection("concluidas").document(tarefaId)
                            .get()
                            .addOnSuccessListener(cd -> {
                                concluida = cd.exists();
                                atualizarEstado(t.getPrazo());
                            });
                });
    }

    private void atualizarEstado(Timestamp prazo) {
        if (concluida) {
            tvStatus.setText("Concluída");
            tvStatus.setTextColor(0xFF008080);
            btnToggle.setText("Desmarcar como concluída");
            btnToggle.setBackgroundTintList(
                    androidx.core.content.res.ResourcesCompat.getColorStateList(
                            getResources(), android.R.color.holo_red_dark, null));
        } else if (prazo != null && prazo.toDate().before(new Date())) {
            tvStatus.setText("Atrasada");
            tvStatus.setTextColor(0xFFCC0000);
            btnToggle.setText("Marcar como concluída");
            btnToggle.setBackgroundTintList(
                    androidx.core.content.res.ResourcesCompat.getColorStateList(
                            getResources(), android.R.color.holo_green_dark, null));
        } else {
            tvStatus.setText("Pendente");
            tvStatus.setTextColor(0xFFE87722);
            btnToggle.setText("Marcar como concluída");
            btnToggle.setBackgroundTintList(
                    androidx.core.content.res.ResourcesCompat.getColorStateList(
                            getResources(), R.color.teal_700, null));
        }
    }

    private void toggleConcluida() {
        if (concluida) {
            db.collection("usuarios").document(uid)
                    .collection("concluidas").document(tarefaId)
                    .delete()
                    .addOnSuccessListener(u -> {
                        concluida = false;
                        Toast.makeText(this, "Marcação removida.", Toast.LENGTH_SHORT).show();
                        carregarDados();
                    });
        } else {
            Map<String, Object> dados = new HashMap<>();
            dados.put("turmaId",    turmaId);
            dados.put("concluidaEm", Timestamp.now());
            db.collection("usuarios").document(uid)
                    .collection("concluidas").document(tarefaId)
                    .set(dados)
                    .addOnSuccessListener(u -> {
                        concluida = true;
                        Toast.makeText(this, "Tarefa marcada como concluída!", Toast.LENGTH_SHORT).show();
                        carregarDados();
                    });
        }
    }
}
