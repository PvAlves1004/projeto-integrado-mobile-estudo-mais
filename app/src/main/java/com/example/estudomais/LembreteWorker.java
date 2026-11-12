package com.example.estudomais;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LembreteWorker extends Worker {

    public LembreteWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return Result.success();

        String turmaId   = getInputData().getString("turmaId");
        String turmaNome = getInputData().getString("turmaNome");
        String tarefaId  = getInputData().getString("tarefaId");
        String titulo    = getInputData().getString("titulo");
        String tipo      = getInputData().getString("tipo");

        if (turmaId == null || tarefaId == null) return Result.success();

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Check if still pending (not in concluidas)
            DocumentSnapshot concluidaDoc = Tasks.await(
                    db.collection("usuarios").document(uid)
                            .collection("concluidas").document(tarefaId).get());
            if (concluidaDoc.exists()) return Result.success(); // Already done

            NotificacaoHelper.criarCanal(getApplicationContext());

            String corpo = buildCorpo(tipo, turmaNome);
            int notifId = (tarefaId + tipo).hashCode();
            NotificacaoHelper.disparar(getApplicationContext(), notifId,
                    titulo != null ? titulo : "Lembrete de tarefa",
                    corpo, turmaId, tarefaId, turmaNome);

        } catch (Exception e) {
            return Result.retry();
        }
        return Result.success();
    }

    private String buildCorpo(String tipo, String turmaNome) {
        String prefix = turmaNome != null ? "[" + turmaNome + "] " : "";
        if (tipo == null) return prefix + "Prazo se aproximando!";
        switch (tipo) {
            case "72h": return prefix + "Prazo em 3 dias.";
            case "48h": return prefix + "Prazo em 2 dias.";
            case "24h": return prefix + "Prazo amanhã!";
            case "12h": return prefix + "Prazo em 12 horas!";
            case "6h":  return prefix + "Prazo em 6 horas!";
            default:    return prefix + "Prazo se aproximando!";
        }
    }
}
