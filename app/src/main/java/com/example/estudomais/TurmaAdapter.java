package com.example.estudomais;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TurmaAdapter extends RecyclerView.Adapter<TurmaAdapter.ViewHolder> {

    private final List<Turma> lista;
    private OnClickListener listener;

    public interface OnClickListener {
        void onClick(Turma turma);
    }

    public TurmaAdapter(List<Turma> lista) {
        this.lista = lista;
    }

    public void setOnClickListener(OnClickListener l) { this.listener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_turma, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Turma t = lista.get(position);
        h.tvNome.setText(t.getNome());
        h.tvDisciplina.setText(t.getDisciplina());
        h.tvSemestre.setText(t.getSemestre());
        h.tvStatus.setText(t.isAtiva() ? "Ativa" : "Inativa");
        StatusChip.aplicar(h.tvStatus, t.isAtiva() ? StatusChip.SUCESSO : StatusChip.PERIGO);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(t); });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvDisciplina, tvSemestre, tvStatus;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNome       = v.findViewById(R.id.tvNomeTurma);
            tvDisciplina = v.findViewById(R.id.tvDisciplina);
            tvSemestre   = v.findViewById(R.id.tvSemestre);
            tvStatus     = v.findViewById(R.id.tvStatusTurma);
        }
    }
}
