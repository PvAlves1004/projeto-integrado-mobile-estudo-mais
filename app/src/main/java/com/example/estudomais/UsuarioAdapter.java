package com.example.estudomais;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.ViewHolder> {

    private final List<Usuario> lista;
    private final List<String> ids;
    private OnClickListener listener;

    public interface OnClickListener {
        void onClick(Usuario usuario, String uid);
    }

    public UsuarioAdapter(List<Usuario> lista, List<String> ids) {
        this.lista = lista;
        this.ids = ids;
    }

    public void setOnClickListener(OnClickListener l) { this.listener = l; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Usuario u = lista.get(position);
        String uid = ids.get(position);

        h.tvNome.setText(u.getNomeCompleto());
        h.tvEmail.setText(u.getEmail());
        h.tvPerfil.setText(u.getPerfilId());
        h.tvStatus.setText(u.isAtivo() ? "Ativo" : "Inativo");
        h.tvStatus.setTextColor(u.isAtivo() ? 0xFF008080 : 0xFFCC0000);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(u, uid);
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvEmail, tvPerfil, tvStatus;

        ViewHolder(@NonNull View v) {
            super(v);
            tvNome   = v.findViewById(R.id.tvNome);
            tvEmail  = v.findViewById(R.id.tvEmail);
            tvPerfil = v.findViewById(R.id.tvPerfil);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}
