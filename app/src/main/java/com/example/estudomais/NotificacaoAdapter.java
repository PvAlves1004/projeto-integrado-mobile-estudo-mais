package com.example.estudomais;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {

    private final List<Notificacao> lista;
    private final String uidUsuario;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Notificacao notificacao);
    }

    public NotificacaoAdapter(List<Notificacao> lista, String uidUsuario) {
        this.lista = lista;
        this.uidUsuario = uidUsuario;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacao n = lista.get(position);

        holder.tvTitulo.setText(n.getTitulo());
        holder.tvMensagem.setText(n.getMensagem());
        holder.tvRemetente.setText("De: " + n.getRemetenteNome());

        if (n.getCriadaEm() != null) {
            Date data = n.getCriadaEm().toDate();
            String dataStr = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                    Locale.getDefault()).format(data);
            holder.tvData.setText(dataStr);
        }

        boolean lida = n.getLidaPor() != null && n.getLidaPor().contains(uidUsuario);
        holder.tvTitulo.setTypeface(null, lida ? Typeface.NORMAL : Typeface.BOLD);
        holder.indicadorNaoLida.setVisibility(lida ? View.INVISIBLE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMensagem, tvRemetente, tvData;
        View indicadorNaoLida;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo        = itemView.findViewById(R.id.tvTitulo);
            tvMensagem      = itemView.findViewById(R.id.tvMensagem);
            tvRemetente     = itemView.findViewById(R.id.tvRemetente);
            tvData          = itemView.findViewById(R.id.tvData);
            indicadorNaoLida = itemView.findViewById(R.id.indicadorNaoLida);
        }
    }
}
