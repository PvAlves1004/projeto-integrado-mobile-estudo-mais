# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Modelos do Firestore (não ofuscar — nomes dos campos devem bater com o banco)
-keep class com.example.estudomais.Usuario { *; }
-keep class com.example.estudomais.Turma { *; }
-keep class com.example.estudomais.Tarefa { *; }

# WorkManager
-keep class androidx.work.** { *; }
-keep class com.example.estudomais.SyncWorker { *; }
-keep class com.example.estudomais.LembreteWorker { *; }

# Manter nomes de Activities para navegação
-keep class com.example.estudomais.**Activity { *; }
