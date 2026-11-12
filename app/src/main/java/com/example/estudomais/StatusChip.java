package com.example.estudomais;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/** Aplica as cores de "selo" (texto + fundo claro) a um TextView de status. */
final class StatusChip {

    static final int SUCESSO = 0;
    static final int PERIGO  = 1;
    static final int ALERTA  = 2;

    private StatusChip() {}

    static void aplicar(TextView tv, int tipo) {
        int texto, fundo;
        switch (tipo) {
            case SUCESSO: texto = R.color.success; fundo = R.color.success_light; break;
            case PERIGO:  texto = R.color.danger;  fundo = R.color.danger_light;  break;
            default:      texto = R.color.accent;  fundo = R.color.accent_light;  break;
        }
        tv.setTextColor(ContextCompat.getColor(tv.getContext(), texto));
        tv.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(tv.getContext(), fundo)));
    }
}
