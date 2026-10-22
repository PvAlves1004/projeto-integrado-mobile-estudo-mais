package com.example.estudomais;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AlunoHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnVoltarLogin = findViewById(R.id.btnVoltarLogin);
        Button btnCriarUsuario = findViewById(R.id.btnCriarUsuario)
        btnVoltarLogin.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
