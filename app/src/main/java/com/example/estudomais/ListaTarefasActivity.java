package com.example.estudomais;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ListaTarefasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvVazio;
    private TarefaAdapter adapter;
    private final List<Tarefa> lista = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tarefas);

        recyclerView = findViewById(R.id.recyclerTarefas);
        tvVazio      = findViewById(R.id.tvVazio);

        db  = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Minhas Tarefas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new TarefaAdapter(lista, true);
        adapter.setOnClickListener(tarefa -> {
            Intent i = new Intent(this, DetalheTarefaActivity.class);
            i.putExtra("turmaId",  tarefa.getTurmaId());
            i.putExtra("tarefaId", tarefa.getId());
            i.putExtra("turmaNome", tarefa.getTurmaNome());
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTarefas();
    }

    private void carregarTarefas() {
        lista.clear();
        adapter.notifyDataSetChanged();

        // 1. Find all turmas the student belongs to
        db.collection("turmas")
                .whereArrayContains("alunoIds", uid)
                .get()
                .addOnSuccessListener(turmaSnap -> {
                    List<String> turmaIds   = new ArrayList<>();
                    List<String> turmaNomes = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : turmaSnap) {
                        turmaIds.add(doc.getId());
                        turmaNomes.add(doc.getString("nome"));
                    }
                    if (turmaIds.isEmpty()) {
                        tvVazio.setVisibility(View.VISIBLE);
                        return;
                    }
                    carregarTarefasDeTurmas(turmaIds, turmaNomes);
                });
    }

    private void carregarTarefasDeTurmas(List<String> turmaIds, List<String> turmaNomes) {
        // 2. Load student's completed tasks
        db.collection("usuarios").document(uid).collection("concluidas")
                .get()
                .addOnSuccessListener(concluidasSnap -> {
                    Set<String> concluidasIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : concluidasSnap) {
                        concluidasIds.add(doc.getId());
                    }

                    // 3. Load tasks from each turma
                    AtomicInteger pending = new AtomicInteger(turmaIds.size());
                    List<Tarefa> all = new ArrayList<>();

                    for (int i = 0; i < turmaIds.size(); i++) {
                        String tId   = turmaIds.get(i);
                        String tNome = turmaNomes.get(i);
                        db.collection("turmas").document(tId).collection("tarefas")
                                .get()
                                .addOnSuccessListener(tarefaSnap -> {
                                    for (QueryDocumentSnapshot doc : tarefaSnap) {
                                        Tarefa t = doc.toObject(Tarefa.class);
                                        t.setId(doc.getId());
                                        t.setTurmaId(tId);
                                        t.setTurmaNome(tNome);
                                        t.setStatus(calcularStatus(t, concluidasIds));
                                        all.add(t);
                                    }
                                    if (pending.decrementAndGet() == 0) {
                                        // RN11 sort order
                                        Collections.sort(all, (a, b) -> {
                                            int rankA = rankStatus(a.getStatus());
                                            int rankB = rankStatus(b.getStatus());
                                            if (rankA != rankB) return Integer.compare(rankA, rankB);
                                            Timestamp pa = a.getPrazo();
                                            Timestamp pb = b.getPrazo();
                                            if (pa == null && pb == null) return 0;
                                            if (pa == null) return 1;
                                            if (pb == null) return -1;
                                            return pa.compareTo(pb);
                                        });
                                        lista.addAll(all);
                                        adapter.notifyDataSetChanged();
                                        tvVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                                    }
                                });
                    }
                });
    }

    private String calcularStatus(Tarefa t, Set<String> concluidasIds) {
        if (concluidasIds.contains(t.getId())) return "Concluída";
        if (t.getPrazo() != null && t.getPrazo().toDate().before(new Date())) return "Atrasada";
        return "Pendente";
    }

    // Lower rank = shown first (Pendente/Atrasada first, then Concluída last)
    private int rankStatus(String status) {
        if (status == null) return 1;
        switch (status) {
            case "Atrasada":  return 0;
            case "Pendente":  return 1;
            case "Concluída": return 2;
            default:          return 1;
        }
    }
}
