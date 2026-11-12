package com.example.estudomais;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeActivity extends AppCompatActivity {

    private TextView tvBoasVindas, tvPerfil, tvAvatar;
    // Atalhos em cartões (MaterialCardView)
    private View btnCadastrarUsuario, btnGerenciarUsuarios, btnGerenciarTurmas;
    private View btnMinhasTurmas, btnEnviarNotificacao;
    private View btnMinhasTarefas, btnCalendario;
    private Button btnSair;

    // Aluno: progresso das tarefas + feed de notificações
    private TextView tvTarefasResumo, tvPercentualTarefas, tvFeedVazio, tvVerTodas;
    private LinearProgressIndicator progressTarefas;
    private View layoutFeed;
    private LinearLayout containerNotificacoes;
    private boolean modoAluno = false;
    private ListenerRegistration feedListener;

    private static final int MAX_FEED = 3;
    private static final SimpleDateFormat SDF_FEED =
            new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvBoasVindas         = findViewById(R.id.tvBoasVindas);
        tvPerfil             = findViewById(R.id.tvPerfil);
        tvAvatar             = findViewById(R.id.tvAvatar);
        btnCadastrarUsuario  = findViewById(R.id.btnCadastrarUsuario);
        btnGerenciarUsuarios = findViewById(R.id.btnGerenciarUsuarios);
        btnGerenciarTurmas   = findViewById(R.id.btnGerenciarTurmas);
        btnMinhasTurmas       = findViewById(R.id.btnMinhasTurmas);
        btnEnviarNotificacao  = findViewById(R.id.btnEnviarNotificacao);
        btnMinhasTarefas     = findViewById(R.id.btnMinhasTarefas);
        btnCalendario        = findViewById(R.id.btnCalendario);
        btnSair              = findViewById(R.id.btnSair);

        tvTarefasResumo       = findViewById(R.id.tvTarefasResumo);
        tvPercentualTarefas   = findViewById(R.id.tvPercentualTarefas);
        progressTarefas       = findViewById(R.id.progressTarefas);
        layoutFeed            = findViewById(R.id.layoutFeed);
        containerNotificacoes = findViewById(R.id.containerNotificacoes);
        tvFeedVazio           = findViewById(R.id.tvFeedVazio);
        tvVerTodas            = findViewById(R.id.tvVerTodas);

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

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza o percentual ao voltar (ex: depois de concluir uma tarefa)
        if (modoAluno) carregarProgressoTarefas();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (modoAluno) ouvirFeedNotificacoes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (feedListener != null) {
            feedListener.remove();
            feedListener = null;
        }
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

                    String primeiroNome = (nome != null && !nome.trim().isEmpty())
                            ? nome.trim().split("\\s+")[0] : "usuário";
                    tvBoasVindas.setText("Olá, " + primeiroNome + "!");
                    tvAvatar.setText(primeiroNome.substring(0, 1).toUpperCase());
                    tvPerfil.setText(rotuloPerfil(perfil));

                    inscreverTopico(perfil);
                    configurarUIPorPerfil(perfil);
                    agendarSincronizacao();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar perfil: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private String rotuloPerfil(String perfil) {
        switch (perfil.toUpperCase()) {
            case "ADMIN":     return "Administrador";
            case "PROFESSOR": return "Professor";
            case "ALUNO":     return "Aluno";
            default:          return perfil;
        }
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
                btnEnviarNotificacao.setVisibility(View.VISIBLE);
                btnEnviarNotificacao.setOnClickListener(v ->
                        startActivity(new Intent(this, NotificacaoActivity.class)));
                break;

            default: // ALUNO
                modoAluno = true;
                btnMinhasTarefas.setVisibility(View.VISIBLE);
                btnCalendario.setVisibility(View.VISIBLE);
                layoutFeed.setVisibility(View.VISIBLE);
                tvVerTodas.setOnClickListener(v -> abrirCaixaEntrada());
                carregarProgressoTarefas();
                ouvirFeedNotificacoes();
                btnMinhasTarefas.setOnClickListener(v ->
                        startActivity(new Intent(this, ListaTarefasActivity.class)));
                btnCalendario.setOnClickListener(v ->
                        startActivity(new Intent(this, CalendarioActivity.class)));
                break;
        }
    }

    // ===== Aluno: percentual de tarefas concluídas =====

    private void carregarProgressoTarefas() {
        if (uid == null) return;

        db.collection("turmas")
                .whereArrayContains("alunoIds", uid)
                .get()
                .addOnSuccessListener(turmaSnap -> {
                    if (turmaSnap.isEmpty()) {
                        mostrarProgresso(0, 0);
                        return;
                    }
                    db.collection("usuarios").document(uid).collection("concluidas")
                            .get()
                            .addOnSuccessListener(concluidasSnap -> {
                                Set<String> concluidasIds = new HashSet<>();
                                for (QueryDocumentSnapshot doc : concluidasSnap)
                                    concluidasIds.add(doc.getId());

                                AtomicInteger pendentes = new AtomicInteger(turmaSnap.size());
                                AtomicInteger total     = new AtomicInteger();
                                AtomicInteger feitas    = new AtomicInteger();

                                for (QueryDocumentSnapshot turma : turmaSnap) {
                                    db.collection("turmas").document(turma.getId())
                                            .collection("tarefas")
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful() && task.getResult() != null) {
                                                    for (QueryDocumentSnapshot t : task.getResult()) {
                                                        total.incrementAndGet();
                                                        if (concluidasIds.contains(t.getId()))
                                                            feitas.incrementAndGet();
                                                    }
                                                }
                                                if (pendentes.decrementAndGet() == 0)
                                                    mostrarProgresso(feitas.get(), total.get());
                                            });
                                }
                            });
                });
    }

    private void mostrarProgresso(int feitas, int total) {
        if (total == 0) {
            tvPercentualTarefas.setText("–");
            tvTarefasResumo.setText("Nenhuma tarefa por enquanto");
            progressTarefas.setProgressCompat(0, true);
            return;
        }
        int pct = Math.round(feitas * 100f / total);
        tvPercentualTarefas.setText(pct + "%");
        tvTarefasResumo.setText(feitas + " de " + total + " concluídas");
        progressTarefas.setProgressCompat(pct, true);
    }

    // ===== Aluno: feed de novas notificações (não lidas) =====

    private void ouvirFeedNotificacoes() {
        if (uid == null) return;
        if (feedListener != null) feedListener.remove();

        // Ativo enquanto a Home está visível (removido no onStop)
        feedListener = db.collection("notificacoes")
                .whereArrayContains("destinatarios", "ALUNO")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;

                    // Ordenar por data decrescente em memória
                    List<Notificacao> naoLidas = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        Notificacao n = doc.toObject(Notificacao.class);
                        if (n == null) continue;
                        boolean lida = n.getLidaPor() != null && n.getLidaPor().contains(uid);
                        if (lida) continue;
                        n.setId(doc.getId());
                        naoLidas.add(n);
                    }
                    naoLidas.sort((a, b) -> {
                        if (a.getCriadaEm() == null) return 1;
                        if (b.getCriadaEm() == null) return -1;
                        return b.getCriadaEm().compareTo(a.getCriadaEm());
                    });

                    containerNotificacoes.removeAllViews();
                    int exibidas = 0;
                    for (Notificacao n : naoLidas) {
                        containerNotificacoes.addView(criarItemFeed(n));
                        if (++exibidas == MAX_FEED) break;
                    }
                    tvFeedVazio.setVisibility(exibidas == 0 ? View.VISIBLE : View.GONE);
                });
    }

    private View criarItemFeed(Notificacao n) {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.item_notificacao, containerNotificacoes, false);
        ((TextView) v.findViewById(R.id.tvTitulo)).setText(n.getTitulo());
        ((TextView) v.findViewById(R.id.tvTitulo)).setTypeface(null, Typeface.BOLD);
        ((TextView) v.findViewById(R.id.tvMensagem)).setText(n.getMensagem());
        ((TextView) v.findViewById(R.id.tvRemetente)).setText("De: " + n.getRemetenteNome());
        if (n.getCriadaEm() != null)
            ((TextView) v.findViewById(R.id.tvData))
                    .setText(SDF_FEED.format(n.getCriadaEm().toDate()));

        v.setOnClickListener(click -> {
            // Marca como lida (sai do feed) e abre a caixa de entrada completa
            db.collection("notificacoes").document(n.getId())
                    .update("lidaPor", FieldValue.arrayUnion(uid));
            abrirCaixaEntrada();
        });
        return v;
    }

    private void abrirCaixaEntrada() {
        Intent i = new Intent(this, CaixaEntradaActivity.class);
        i.putExtra("perfil", "ALUNO");
        startActivity(i);
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
        SyncWorker.rodarAgora(this);
    }
}
