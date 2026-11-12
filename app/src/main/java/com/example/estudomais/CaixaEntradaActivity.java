package com.example.estudomais;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Collections;

import java.util.ArrayList;
import java.util.List;

public class CaixaEntradaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvVazio;
    private NotificacaoAdapter adapter;
    private final List<Notificacao> lista = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private String uid;
    private String perfilUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caixa_entrada);

        recyclerView = findViewById(R.id.recyclerNotificacoes);
        tvVazio      = findViewById(R.id.tvVazio);

        db            = FirebaseFirestore.getInstance();
        uid           = FirebaseAuth.getInstance().getUid();
        perfilUsuario = getIntent().getStringExtra("perfil");

        adapter = new NotificacaoAdapter(lista, uid);
        adapter.setOnItemClickListener(this::marcarComoLida);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Caixa de Entrada");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ouvirNotificacoes();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void ouvirNotificacoes() {
        if (perfilUsuario == null || uid == null) return;

        listenerRegistration = db.collection("notificacoes")
                .whereArrayContains("destinatarios", perfilUsuario)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar notificações",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    lista.clear();

                    if (snapshot != null) {
                        for (var doc : snapshot.getDocuments()) {
                            Notificacao n = doc.toObject(Notificacao.class);
                            if (n != null) {
                                n.setId(doc.getId());
                                lista.add(n);
                            }
                        }
                    }

                    // Ordenar por data decrescente em memória (evita índice composto no Firestore)
                    Collections.sort(lista, (a, b) -> {
                        if (a.getCriadaEm() == null) return 1;
                        if (b.getCriadaEm() == null) return -1;
                        return b.getCriadaEm().compareTo(a.getCriadaEm());
                    });

                    adapter.notifyDataSetChanged();
                    tvVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void marcarComoLida(Notificacao notificacao) {
        if (uid == null || notificacao.getId() == null) return;
        if (notificacao.getLidaPor() != null
                && notificacao.getLidaPor().contains(uid)) return;

        db.collection("notificacoes").document(notificacao.getId())
                .update("lidaPor", FieldValue.arrayUnion(uid));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
