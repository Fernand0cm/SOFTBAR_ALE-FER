package com.SOFTBAR_F_A.ui.cobro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.ticket.TicketActivity;

public class CobroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobro);

        Button btnConfirmar = findViewById(R.id.btn_confirmar);
        btnConfirmar.setOnClickListener(v -> {
            startActivity(new Intent(this, TicketActivity.class));
            finish();
        });

        Button btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());
    }
}
