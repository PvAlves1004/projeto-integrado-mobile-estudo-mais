package com.example.estudomais;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TurmasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvVazio;
    private FloatingActionButton fabCriar;

    private TurmaAdapter adapter;
    private final List<Turma> lista = new ArrayList<>();

    private FirebaseFirestore db;
    private boolean modoProfessor;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turmas);

        recyclerView = findViewById(R.id.recyclerTurmas);
        tvVazio      = findViewById(R.id.tvVazio);
        fabCriar     = findViewById(R.id.fabCriarTurma);

        db            = FirebaseFirestore.getInstance();
        modoProfessor = getIntent().getBooleanExtra("modoProfessor", false);
        uid           = getIntent().getStringExtra("uid");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(modoProfessor ? "Minhas Turmas" : "Gerenciar Turmas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new TurmaAdapter(lista);
        adapter.setOnClickListener(turma -> {
            if (!modoProfessor) {
                new AlertDialog.Builder(this)
                        .setTitle(turma.getNome())
                        .setItems(new String[]{"Ver Tarefas", "Editar Turma"}, (d, which) -> {
                            if (which == 0) {
                                abrirTarefas(turma);
                            } else {
                                Intent i = new Intent(this, CriarTurmaActivity.class);
                                i.putExtra("turmaId", turma.getId());
                                startActivity(i);
                            }
                        })
                        .show();
            } else {
                abrirTarefas(turma);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (!modoProfessor) {
            fabCriar.setVisibility(View.VISIBLE);
            fabCriar.setOnClickListener(v ->
                    startActivity(new Intent(this, CriarTurmaActivity.class)));
        }
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void abrirTarefas(Turma turma) {
        Intent i = new Intent(this, TarefasActivity.class);
        i.putExtra("turmaId", turma.getId());
        i.putExtra("turmaNome", turma.getNome());
        i.putExtra("modoProfessor", modoProfessor);
        if (modoProfessor) i.putExtra("uid", uid);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTurmas();
    }

    private void carregarTurmas() {
        lista.clear();
        if (modoProfessor && uid != null) {
            db.collection("turmas")
                    .whereArrayContains("professorIds", uid)
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (QueryDocumentSnapshot doc : snap) {
                            Turma t = doc.toObject(Turma.class);
                            t.setId(doc.getId());
                            lista.add(t);
                        }
                        atualizarUI();
                    });
        } else {
            db.collection("turmas")
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (QueryDocumentSnapshot doc : snap) {
                            Turma t = doc.toObject(Turma.class);
                            t.setId(doc.getId());
                            lista.add(t);
                        }
                        atualizarUI();
                    });
        }
    }

    private void atualizarUI() {
        adapter.notifyDataSetChanged();
        tvVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
