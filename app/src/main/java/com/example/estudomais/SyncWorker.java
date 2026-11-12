package com.example.estudomais;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {

    private static final String WORK_NAME = "sync_tarefas";
    private static final String PREFS     = "estudomais_sync";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void agendar(Context ctx) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, req);
    }

    public static void rodarAgora(Context ctx) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(ctx).enqueueUniqueWork(
                WORK_NAME + "_agora",
                ExistingWorkPolicy.REPLACE,
                req);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) return Result.success();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

            // Check if user is ALUNO
            DocumentSnapshot userDoc = Tasks.await(
                    db.collection("usuarios").document(uid).get());
            if (!userDoc.exists()) return Result.success();
            String perfil = userDoc.getString("perfilId");
            if (perfil == null) perfil = userDoc.getString("perfilID");
            if (!"ALUNO".equalsIgnoreCase(perfil)) return Result.success();

            // Load aluno's turmas
            QuerySnapshot turmaSnap = Tasks.await(
                    db.collection("turmas").whereArrayContains("alunoIds", uid).get());

            Set<String> notificadosNovas      = prefs.getStringSet("notif_novas_" + uid, new HashSet<>());
            Set<String> lembreteAgendados      = prefs.getStringSet("lembrete_ag_" + uid, new HashSet<>());
            Set<String> notificadosNovasUpdate = new HashSet<>(notificadosNovas);
            Set<String> lembreteUpdate         = new HashSet<>(lembreteAgendados);

            NotificacaoHelper.criarCanal(getApplicationContext());

            // Load student's completed tasks
            QuerySnapshot concluidasSnap = Tasks.await(
                    db.collection("usuarios").document(uid).collection("concluidas").get());
            Set<String> concluidasIds = new HashSet<>();
            for (DocumentSnapshot doc : concluidasSnap.getDocuments()) {
                concluidasIds.add(doc.getId());
            }

            int notifId = (int) (System.currentTimeMillis() % 100000);

            for (QueryDocumentSnapshot turmaDoc : turmaSnap) {
                String turmaId   = turmaDoc.getId();
                String turmaNome = turmaDoc.getString("nome");

                QuerySnapshot tarefaSnap = Tasks.await(
                        db.collection("turmas").document(turmaId)
                                .collection("tarefas").get());

                for (QueryDocumentSnapshot tarefaDoc : tarefaSnap) {
                    String tarefaId = tarefaDoc.getId();
                    Tarefa t = tarefaDoc.toObject(Tarefa.class);
                    t.setId(tarefaId);
                    t.setTurmaId(turmaId);
                    t.setTurmaNome(turmaNome);

                    if (t.getPrazo() == null) continue;
                    Date prazo = t.getPrazo().toDate();

                    // RF33: notify new task (not seen before and prazo in future)
                    if (!notificadosNovas.contains(tarefaId) && prazo.after(new Date())) {
                        NotificacaoHelper.disparar(getApplicationContext(), notifId++,
                                "Nova tarefa: " + t.getTitulo(),
                                "Turma: " + turmaNome + " — prazo " +
                                        android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", prazo),
                                turmaId, tarefaId, turmaNome);
                        notificadosNovasUpdate.add(tarefaId);
                    }

                    // Schedule lembretes if not done yet and task not concluded
                    if (!lembreteAgendados.contains(tarefaId) && !concluidasIds.contains(tarefaId)) {
                        agendarLembretes(turmaId, turmaNome, tarefaId, t.getTitulo(),
                                prazo, t.isValeNota());
                        lembreteUpdate.add(tarefaId);
                    }
                }
            }

            // Check for unread professor notifications and fire system tray notification
            Set<String> notifProfVistas  = prefs.getStringSet("notif_prof_" + uid, new HashSet<>());
            Set<String> notifProfUpdate  = new HashSet<>(notifProfVistas);

            QuerySnapshot notifSnap = Tasks.await(
                    db.collection("notificacoes")
                            .whereArrayContains("destinatarios", "ALUNO")
                            .get());

            for (DocumentSnapshot notifDoc : notifSnap.getDocuments()) {
                String notifDocId = notifDoc.getId();
                if (notifProfVistas.contains(notifDocId)) continue;

                Notificacao n = notifDoc.toObject(Notificacao.class);
                if (n == null) continue;

                List<String> lidaPor = n.getLidaPor();
                if (lidaPor != null && lidaPor.contains(uid)) continue; // already read in-app

                NotificacaoHelper.dispararMensagem(getApplicationContext(),
                        notifDocId.hashCode(), n.getTitulo(), n.getMensagem(), "ALUNO");
                notifProfUpdate.add(notifDocId);
            }

            prefs.edit()
                    .putStringSet("notif_novas_" + uid, notificadosNovasUpdate)
                    .putStringSet("lembrete_ag_" + uid, lembreteUpdate)
                    .putStringSet("notif_prof_" + uid, notifProfUpdate)
                    .apply();

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void agendarLembretes(String turmaId, String turmaNome, String tarefaId,
                                   String titulo, Date prazo, boolean valeNota) {
        long agora = System.currentTimeMillis();

        long[] offsets = {
                72 * 3600_000L,
                48 * 3600_000L,
                24 * 3600_000L,
                12 * 3600_000L,
                 6 * 3600_000L
        };

        for (long offset : offsets) {
            long fireAt = prazo.getTime() - offset;
            if (fireAt <= agora) continue; // Already past

            fireAt = ajustarHorarioSilencioso(fireAt, offset);
            long delay = fireAt - agora;
            if (delay <= 0) continue;

            String tipo;
            if      (offset == 72 * 3600_000L) tipo = "72h";
            else if (offset == 48 * 3600_000L) tipo = "48h";
            else if (offset == 24 * 3600_000L) tipo = "24h";
            else if (offset == 12 * 3600_000L) tipo = "12h";
            else                               tipo = "6h";

            Data data = new Data.Builder()
                    .putString("turmaId",   turmaId)
                    .putString("turmaNome", turmaNome)
                    .putString("tarefaId",  tarefaId)
                    .putString("titulo",    titulo)
                    .putString("tipo",      tipo)
                    .build();

            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(LembreteWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build();
            WorkManager.getInstance(getApplicationContext()).enqueue(req);
        }
    }

    // RN15: quiet hours 22:00–07:00
    // Deadline reminders (before prazo): bring forward to 21:00
    // Others: defer to 07:00
    private long ajustarHorarioSilencioso(long fireAt, long offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fireAt);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 22) {
            // Bring forward to 21:00 same day
            cal.set(Calendar.HOUR_OF_DAY, 21);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        } else if (hour < 7) {
            // Defer to 07:00 same day
            cal.set(Calendar.HOUR_OF_DAY, 7);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        }
        return cal.getTimeInMillis();
    }
}
