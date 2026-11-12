package com.example.estudomais;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CalendarioActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvDia, tvVazio;
    private RecyclerView recyclerDia;

    private TarefaAdapter adapter;
    private final List<Tarefa> listaDia = new ArrayList<>();
    private final List<Tarefa> todasTarefas = new ArrayList<>();

    private FirebaseFirestore db;
    private String uid;
    private boolean modoProfessor;
    private static final SimpleDateFormat SDF_DAY =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat SDF_HEADER =
            new SimpleDateFormat("EEEE, dd 'de' MMMM", new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        calendarView = findViewById(R.id.calendarView);
        tvDia        = findViewById(R.id.tvDiaSelecionado);
        tvVazio      = findViewById(R.id.tvVazio);
        recyclerDia  = findViewById(R.id.recyclerDia);

        db            = FirebaseFirestore.getInstance();
        uid           = FirebaseAuth.getInstance().getUid();
        modoProfessor = getIntent().getBooleanExtra("modoProfessor", false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Calendário");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new TarefaAdapter(listaDia, !modoProfessor);
        adapter.setOnClickListener(tarefa -> {
            if (!modoProfessor) {
                Intent i = new Intent(this, DetalheTarefaActivity.class);
                i.putExtra("turmaId",   tarefa.getTurmaId());
                i.putExtra("tarefaId",  tarefa.getId());
                i.putExtra("turmaNome", tarefa.getTurmaNome());
                startActivity(i);
            }
        });
        recyclerDia.setLayoutManager(new LinearLayoutManager(this));
        recyclerDia.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(year, month, day);
            filtrarDia(sel.getTime());
        });

        carregarTarefas();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void carregarTarefas() {
        todasTarefas.clear();
        String campo = modoProfessor ? "professorIds" : "alunoIds";

        db.collection("turmas").whereArrayContains(campo, uid)
                .get()
                .addOnSuccessListener(turmaSnap -> {
                    List<String> ids   = new ArrayList<>();
                    List<String> nomes = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : turmaSnap) {
                        ids.add(doc.getId());
                        nomes.add(doc.getString("nome"));
                    }
                    if (ids.isEmpty()) return;

                    AtomicInteger pending = new AtomicInteger(ids.size());
                    for (int i = 0; i < ids.size(); i++) {
                        String tId   = ids.get(i);
                        String tNome = nomes.get(i);
                        db.collection("turmas").document(tId).collection("tarefas")
                                .get()
                                .addOnSuccessListener(snap -> {
                                    for (QueryDocumentSnapshot doc : snap) {
                                        Tarefa t = doc.toObject(Tarefa.class);
                                        t.setId(doc.getId());
                                        t.setTurmaId(tId);
                                        t.setTurmaNome(tNome);
                                        todasTarefas.add(t);
                                    }
                                    if (pending.decrementAndGet() == 0) {
                                        filtrarDia(new Date(calendarView.getDate()));
                                    }
                                });
                    }
                });
    }

    private void filtrarDia(Date dia) {
        String diaStr = SDF_DAY.format(dia);
        tvDia.setText(SDF_HEADER.format(dia));
        listaDia.clear();
        for (Tarefa t : todasTarefas) {
            if (t.getPrazo() != null && SDF_DAY.format(t.getPrazo().toDate()).equals(diaStr)) {
                listaDia.add(t);
            }
        }
        adapter.notifyDataSetChanged();
        tvVazio.setVisibility(listaDia.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
