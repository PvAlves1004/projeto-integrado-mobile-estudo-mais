package com.example.estudomais;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificacaoHelper {

    static final String CHANNEL_ID = "estudomais_channel";
    private static final String CHANNEL_NAME = "Estudo+";

    public static void criarCanal(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            canal.setDescription("Notificações de tarefas e prazos");
            ctx.getSystemService(NotificationManager.class).createNotificationChannel(canal);
        }
    }

    public static void disparar(Context ctx, int id, String titulo, String corpo,
                                String turmaId, String tarefaId, String turmaNome) {
        Intent intent = new Intent(ctx, DetalheTarefaActivity.class);
        intent.putExtra("turmaId",   turmaId);
        intent.putExtra("tarefaId",  tarefaId);
        intent.putExtra("turmaNome", turmaNome);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pi = PendingIntent.getActivity(ctx, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(corpo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(corpo))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, builder.build());
    }
}
