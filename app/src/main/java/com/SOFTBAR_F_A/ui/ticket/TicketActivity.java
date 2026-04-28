package com.SOFTBAR_F_A.ui.ticket;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;

public class TicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        Button btnImprimir = findViewById(R.id.btn_imprimir);
        btnImprimir.setOnClickListener(v ->
                Toast.makeText(this, R.string.ticket_pendiente, Toast.LENGTH_SHORT).show());

        Button btnEmail = findViewById(R.id.btn_email);
        btnEmail.setOnClickListener(v ->
                Toast.makeText(this, R.string.ticket_pendiente, Toast.LENGTH_SHORT).show());

        Button btnCerrar = findViewById(R.id.btn_cerrar);
        btnCerrar.setOnClickListener(v -> finish());
    }
}
