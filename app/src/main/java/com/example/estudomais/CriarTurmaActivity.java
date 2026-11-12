package com.example.estudomais;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CriarTurmaActivity extends AppCompatActivity {

    private EditText editNome, editDisciplina, editSemestre;
    private TextView tvProfessores, tvAlunos;
    private Button btnProfessores, btnAlunos, btnSalvar;

    private FirebaseFirestore db;
    private String turmaId = null; // null = create mode, non-null = edit mode

    private List<String> listProfIds   = new ArrayList<>();
    private List<String> listProfNomes = new ArrayList<>();
    private boolean[]    selProf       = new boolean[0];

    private List<String> listAluIds    = new ArrayList<>();
    private List<String> listAluNomes  = new ArrayList<>();
    private boolean[]    selAlu        = new boolean[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_turma);

        editNome       = findViewById(R.id.editNomeTurma);
        editDisciplina = findViewById(R.id.editDisciplina);
        editSemestre   = findViewById(R.id.editSemestre);
        tvProfessores  = findViewById(R.id.tvProfessoresSelecionados);
        tvAlunos       = findViewById(R.id.tvAlunosSelecionados);
        btnProfessores = findViewById(R.id.btnSelecionarProfessores);
        btnAlunos      = findViewById(R.id.btnSelecionarAlunos);
        btnSalvar      = findViewById(R.id.btnSalvarTurma);

        db      = FirebaseFirestore.getInstance();
        turmaId = getIntent().getStringExtra("turmaId");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(turmaId != null ? "Editar Turma" : "Nova Turma");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        carregarUsuarios();
        btnProfessores.setOnClickListener(v -> abrirDialogProfessores());
        btnAlunos.setOnClickListener(v -> abrirDialogAlunos());
        btnSalvar.setOnClickListener(v -> salvar());
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void carregarUsuarios() {
        db.collection("usuarios").get().addOnSuccessListener(snap -> {
            listProfIds   = new ArrayList<>();
            listProfNomes = new ArrayList<>();
            listAluIds    = new ArrayList<>();
            listAluNomes  = new ArrayList<>();

            for (QueryDocumentSnapshot doc : snap) {
                String perfil = doc.getString("perfilId");
                if (perfil == null) perfil = doc.getString("perfilID");
                if (perfil == null) continue;
                String nome = doc.getString("nomeCompleto");
                if (nome == null || nome.isEmpty()) nome = doc.getString("email");
                if ("PROFESSOR".equalsIgnoreCase(perfil)) {
                    listProfIds.add(doc.getId());
                    listProfNomes.add(nome != null ? nome : doc.getId());
                } else if ("ALUNO".equalsIgnoreCase(perfil)) {
                    listAluIds.add(doc.getId());
                    listAluNomes.add(nome != null ? nome : doc.getId());
                }
            }
            selProf = new boolean[listProfIds.size()];
            selAlu  = new boolean[listAluIds.size()];

            if (turmaId != null) carregarDadosTurma();
        });
    }

    private void carregarDadosTurma() {
        db.collection("turmas").document(turmaId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    Turma t = doc.toObject(Turma.class);
                    if (t == null) return;

                    editNome.setText(t.getNome());
                    editDisciplina.setText(t.getDisciplina());
                    editSemestre.setText(t.getSemestre());

                    List<String> profIds = t.getProfessorIds();
                    for (int i = 0; i < listProfIds.size(); i++) {
                        if (profIds.contains(listProfIds.get(i))) selProf[i] = true;
                    }
                    atualizarTexto(tvProfessores, selProf, listProfNomes);

                    List<String> aluIds = t.getAlunoIds();
                    for (int i = 0; i < listAluIds.size(); i++) {
                        if (aluIds.contains(listAluIds.get(i))) selAlu[i] = true;
                    }
                    atualizarTexto(tvAlunos, selAlu, listAluNomes);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar turma: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void abrirDialogProfessores() {
        if (listProfNomes.isEmpty()) {
            Toast.makeText(this, "Carregando... tente novamente.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean[] tempSel = selProf.clone();
        new AlertDialog.Builder(this)
                .setTitle("Selecionar professores")
                .setMultiChoiceItems(listProfNomes.toArray(new String[0]), tempSel,
                        (d, which, checked) -> tempSel[which] = checked)
                .setPositiveButton("OK", (d, w) -> {
                    selProf = tempSel;
                    atualizarTexto(tvProfessores, selProf, listProfNomes);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirDialogAlunos() {
        if (listAluNomes.isEmpty()) {
            Toast.makeText(this, "Carregando... tente novamente.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean[] tempSel = selAlu.clone();
        new AlertDialog.Builder(this)
                .setTitle("Selecionar alunos")
                .setMultiChoiceItems(listAluNomes.toArray(new String[0]), tempSel,
                        (d, which, checked) -> tempSel[which] = checked)
                .setPositiveButton("OK", (d, w) -> {
                    selAlu = tempSel;
                    atualizarTexto(tvAlunos, selAlu, listAluNomes);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarTexto(TextView tv, boolean[] sel, List<String> nomes) {
        List<String> escolhidos = new ArrayList<>();
        for (int i = 0; i < sel.length; i++) {
            if (sel[i]) escolhidos.add(nomes.get(i));
        }
        tv.setText(escolhidos.isEmpty() ? "Nenhum selecionado" : String.join(", ", escolhidos));
    }

    private void salvar() {
        String nome       = editNome.getText().toString().trim();
        String disciplina = editDisciplina.getText().toString().trim();
        String semestre   = editSemestre.getText().toString().trim();

        if (nome.isEmpty() || disciplina.isEmpty() || semestre.isEmpty()) {
            Toast.makeText(this, "Preencha nome, disciplina e ano/série.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> profIds = new ArrayList<>();
        for (int i = 0; i < selProf.length; i++) {
            if (selProf[i]) profIds.add(listProfIds.get(i));
        }
        List<String> aluIds = new ArrayList<>();
        for (int i = 0; i < selAlu.length; i++) {
            if (selAlu[i]) aluIds.add(listAluIds.get(i));
        }

        btnSalvar.setEnabled(false);

        if (turmaId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("nome", nome);
            updates.put("disciplina", disciplina);
            updates.put("semestre", semestre);
            updates.put("professorIds", profIds);
            updates.put("alunoIds", aluIds);

            db.collection("turmas").document(turmaId)
                    .update(updates)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "Turma atualizada!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnSalvar.setEnabled(true);
                    });
        } else {
            Turma turma = new Turma(nome, disciplina, semestre);
            turma.setProfessorIds(profIds);
            turma.setAlunoIds(aluIds);

            db.collection("turmas").add(turma)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Turma criada!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnSalvar.setEnabled(true);
                    });
        }
    }
}
