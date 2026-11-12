package com.example.estudomais;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CriarEditarTarefaActivity extends AppCompatActivity {

    private EditText editTitulo, editDescricao, editNotaMaxima;
    private Button btnPrazo, btnSalvar;
    private TextView tvPrazo;
    private CompoundButton checkValeNota;
    private View tilNotaMaxima;

    private FirebaseFirestore db;
    private String turmaId, turmaNome, tarefaId;
    private Calendar prazoSelecionado;
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_editar_tarefa);

        editTitulo    = findViewById(R.id.editTituloTarefa);
        editDescricao = findViewById(R.id.editDescricaoTarefa);
        editNotaMaxima = findViewById(R.id.editNotaMaxima);
        btnPrazo      = findViewById(R.id.btnSelecionarPrazo);
        tvPrazo       = findViewById(R.id.tvPrazoSelecionado);
        checkValeNota = findViewById(R.id.checkValeNota);
        tilNotaMaxima = findViewById(R.id.tilNotaMaxima);
        btnSalvar     = findViewById(R.id.btnSalvarTarefa);

        db        = FirebaseFirestore.getInstance();
        turmaId   = getIntent().getStringExtra("turmaId");
        turmaNome = getIntent().getStringExtra("turmaNome");
        tarefaId  = getIntent().getStringExtra("tarefaId");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(tarefaId != null ? "Editar Tarefa" : "Nova Tarefa");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        checkValeNota.setOnCheckedChangeListener((btn, checked) ->
                tilNotaMaxima.setVisibility(checked ? View.VISIBLE : View.GONE));

        btnPrazo.setOnClickListener(v -> selecionarPrazo());
        btnSalvar.setOnClickListener(v -> salvar());

        if (tarefaId != null) carregarTarefa();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void selecionarPrazo() {
        Calendar cal = prazoSelecionado != null ? prazoSelecionado : Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            new TimePickerDialog(this, (tv, hour, minute) -> {
                prazoSelecionado = Calendar.getInstance();
                prazoSelecionado.set(year, month, day, hour, minute, 0);
                tvPrazo.setText(SDF.format(prazoSelecionado.getTime()));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void carregarTarefa() {
        db.collection("turmas").document(turmaId)
                .collection("tarefas").document(tarefaId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    Tarefa t = doc.toObject(Tarefa.class);
                    if (t == null) return;
                    editTitulo.setText(t.getTitulo());
                    editDescricao.setText(t.getDescricao());
                    checkValeNota.setChecked(t.isValeNota());
                    if (t.isValeNota()) {
                        tilNotaMaxima.setVisibility(View.VISIBLE);
                        if (t.getNotaMaxima() != null)
                            editNotaMaxima.setText(String.valueOf(t.getNotaMaxima()));
                    }
                    if (t.getPrazo() != null) {
                        prazoSelecionado = Calendar.getInstance();
                        prazoSelecionado.setTime(t.getPrazo().toDate());
                        tvPrazo.setText(SDF.format(prazoSelecionado.getTime()));
                    }
                });
    }

    private void salvar() {
        String titulo    = editTitulo.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        boolean valeNota = checkValeNota.isChecked();

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Título obrigatório.", Toast.LENGTH_SHORT).show();
            return;
        }
        // RN12: prazo deve ser futuro
        if (prazoSelecionado == null) {
            Toast.makeText(this, "Selecione o prazo.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tarefaId == null && prazoSelecionado.getTime().before(new Date())) {
            Toast.makeText(this, "O prazo deve ser uma data futura.", Toast.LENGTH_SHORT).show();
            return;
        }

        Double notaMaxima = null;
        if (valeNota) {
            String notaStr = editNotaMaxima.getText().toString().trim();
            if (notaStr.isEmpty()) {
                // RN13
                Toast.makeText(this, "Informe a nota máxima.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                notaMaxima = Double.parseDouble(notaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nota máxima inválida.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Timestamp prazoTs = new Timestamp(prazoSelecionado.getTime());
        String uid = FirebaseAuth.getInstance().getUid();

        Tarefa tarefa = new Tarefa(turmaId, titulo, descricao, prazoTs, valeNota, notaMaxima, uid);
        tarefa.setTurmaNome(turmaNome);

        btnSalvar.setEnabled(false);

        if (tarefaId != null) {
            db.collection("turmas").document(turmaId)
                    .collection("tarefas").document(tarefaId)
                    .set(tarefa)
                    .addOnSuccessListener(u -> { Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show(); finish(); })
                    .addOnFailureListener(e -> { Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show(); btnSalvar.setEnabled(true); });
        } else {
            db.collection("turmas").document(turmaId)
                    .collection("tarefas").add(tarefa)
                    .addOnSuccessListener(ref -> { Toast.makeText(this, "Tarefa criada!", Toast.LENGTH_SHORT).show(); finish(); })
                    .addOnFailureListener(e -> { Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show(); btnSalvar.setEnabled(true); });
        }
    }
}
