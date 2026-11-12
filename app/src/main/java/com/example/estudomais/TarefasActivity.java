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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TarefasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvVazio;
    private FloatingActionButton fabCriar;

    private TarefaAdapter adapter;
    private final List<Tarefa> lista = new ArrayList<>();

    private FirebaseFirestore db;
    private String turmaId;
    private String turmaNome;
    private boolean modoProfessor;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        recyclerView = findViewById(R.id.recyclerTarefas);
        tvVazio      = findViewById(R.id.tvVazio);
        fabCriar     = findViewById(R.id.fabCriarTarefa);

        db            = FirebaseFirestore.getInstance();
        turmaId       = getIntent().getStringExtra("turmaId");
        turmaNome     = getIntent().getStringExtra("turmaNome");
        modoProfessor = getIntent().getBooleanExtra("modoProfessor", false);
        uid           = getIntent().getStringExtra("uid");
        if (uid == null) uid = FirebaseAuth.getInstance().getUid();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(turmaNome != null ? turmaNome : "Tarefas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new TarefaAdapter(lista, false);
        adapter.setOnClickListener(tarefa -> {
            if (modoProfessor) {
                mostrarOpcoesTarefa(tarefa);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (modoProfessor) {
            fabCriar.setVisibility(View.VISIBLE);
            fabCriar.setOnClickListener(v -> {
                Intent i = new Intent(this, CriarEditarTarefaActivity.class);
                i.putExtra("turmaId", turmaId);
                i.putExtra("turmaNome", turmaNome);
                startActivity(i);
            });
        }

        carregarTarefas();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTarefas();
    }

    private void carregarTarefas() {
        lista.clear();
        db.collection("turmas").document(turmaId).collection("tarefas")
                .orderBy("prazo")
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        Tarefa t = doc.toObject(Tarefa.class);
                        t.setId(doc.getId());
                        t.setTurmaId(turmaId);
                        t.setTurmaNome(turmaNome);
                        lista.add(t);
                    }
                    adapter.notifyDataSetChanged();
                    tvVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void mostrarOpcoesTarefa(Tarefa tarefa) {
        new AlertDialog.Builder(this)
                .setTitle(tarefa.getTitulo())
                .setItems(new String[]{"Editar", "Excluir"}, (d, which) -> {
                    if (which == 0) {
                        Intent i = new Intent(this, CriarEditarTarefaActivity.class);
                        i.putExtra("turmaId", turmaId);
                        i.putExtra("turmaNome", turmaNome);
                        i.putExtra("tarefaId", tarefa.getId());
                        startActivity(i);
                    } else {
                        confirmarExcluir(tarefa);
                    }
                })
                .show();
    }

    private void confirmarExcluir(Tarefa tarefa) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir tarefa")
                .setMessage("Deseja excluir \"" + tarefa.getTitulo() + "\"?")
                .setPositiveButton("Excluir", (d, w) ->
                        db.collection("turmas").document(turmaId)
                                .collection("tarefas").document(tarefa.getId())
                                .delete()
                                .addOnSuccessListener(u -> carregarTarefas()))
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
