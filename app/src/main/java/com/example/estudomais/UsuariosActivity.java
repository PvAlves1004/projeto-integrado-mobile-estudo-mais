package com.example.estudomais;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsuariosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvVazio;
    private EditText editBusca;
    private Spinner spinnerFiltro;

    private UsuarioAdapter adapter;
    private final List<Usuario> listaCompleta = new ArrayList<>();
    private final List<String> idsCompleta = new ArrayList<>();
    private final List<Usuario> listaFiltrada = new ArrayList<>();
    private final List<String> idsFiltrada = new ArrayList<>();

    private String filtroAtual = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        recyclerView = findViewById(R.id.recyclerUsuarios);
        tvVazio      = findViewById(R.id.tvVazio);
        editBusca    = findViewById(R.id.editBusca);
        spinnerFiltro = findViewById(R.id.spinnerFiltro);

        adapter = new UsuarioAdapter(listaFiltrada, idsFiltrada);
        adapter.setOnClickListener((usuario, uid) -> {
            Intent i = new Intent(this, EditarUsuarioActivity.class);
            i.putExtra("uid", uid);
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos", "ALUNO", "PROFESSOR"});
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(filtroAdapter);
        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                filtroAtual = filtroAdapter.getItem(pos);
                aplicarFiltro(editBusca.getText().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        editBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                aplicarFiltro(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Usuários");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        carregarUsuarios();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    @Override
    protected void onResume() {
        super.onResume();
        carregarUsuarios();
    }

    private void carregarUsuarios() {
        FirebaseFirestore.getInstance().collection("usuarios")
                .get()
                .addOnSuccessListener(snap -> {
                    listaCompleta.clear();
                    idsCompleta.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Usuario u = doc.toObject(Usuario.class);
                        listaCompleta.add(u);
                        idsCompleta.add(doc.getId());
                    }
                    aplicarFiltro(editBusca.getText().toString());
                });
    }

    private void aplicarFiltro(String busca) {
        listaFiltrada.clear();
        idsFiltrada.clear();
        String termo = busca.trim().toLowerCase();

        for (int i = 0; i < listaCompleta.size(); i++) {
            Usuario u = listaCompleta.get(i);

            boolean perfil = filtroAtual.equals("Todos")
                    || filtroAtual.equalsIgnoreCase(u.getPerfilId());
            boolean texto = termo.isEmpty()
                    || (u.getNomeCompleto() != null && u.getNomeCompleto().toLowerCase().contains(termo))
                    || (u.getMatricula() != null && u.getMatricula().toLowerCase().contains(termo));

            if (perfil && texto) {
                listaFiltrada.add(u);
                idsFiltrada.add(idsCompleta.get(i));
            }
        }

        adapter.notifyDataSetChanged();
        tvVazio.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
