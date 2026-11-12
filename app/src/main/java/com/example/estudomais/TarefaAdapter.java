package com.example.estudomais;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Tarefa tarefa); }

    private final List<Tarefa> lista;
    private final boolean modoAluno;
    private OnClickListener listener;
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public TarefaAdapter(List<Tarefa> lista, boolean modoAluno) {
        this.lista     = lista;
        this.modoAluno = modoAluno;
    }

    public void setOnClickListener(OnClickListener l) { this.listener = l; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Tarefa t = lista.get(position);
        h.tvTitulo.setText(t.getTitulo());

        if (t.getPrazo() != null) {
            h.tvPrazo.setText("Prazo: " + SDF.format(t.getPrazo().toDate()));
        } else {
            h.tvPrazo.setText("Sem prazo definido");
        }

        if (t.isValeNota()) h.tvValeNota.setVisibility(View.VISIBLE);
        else h.tvValeNota.setVisibility(View.GONE);

        if (modoAluno) {
            if (t.getTurmaNome() != null && !t.getTurmaNome().isEmpty()) {
                h.tvTurma.setVisibility(View.VISIBLE);
                h.tvTurma.setText(t.getTurmaNome());
            }
            if (t.getStatus() != null) {
                h.tvStatus.setVisibility(View.VISIBLE);
                h.tvStatus.setText(t.getStatus());
                switch (t.getStatus()) {
                    case "Concluída":
                    case "Entrega confirmada":
                        StatusChip.aplicar(h.tvStatus, StatusChip.SUCESSO);
                        break;
                    case "Atrasada":
                        StatusChip.aplicar(h.tvStatus, StatusChip.PERIGO);
                        break;
                    default:
                        StatusChip.aplicar(h.tvStatus, StatusChip.ALERTA);
                }
            }
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(t); });
    }

    @Override public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvPrazo, tvValeNota, tvStatus, tvTurma;

        ViewHolder(@NonNull View v) {
            super(v);
            tvTitulo  = v.findViewById(R.id.tvTituloTarefa);
            tvPrazo   = v.findViewById(R.id.tvPrazoTarefa);
            tvValeNota = v.findViewById(R.id.tvValeNota);
            tvStatus  = v.findViewById(R.id.tvStatusTarefa);
            tvTurma   = v.findViewById(R.id.tvTurmaTarefa);
        }
    }
}
