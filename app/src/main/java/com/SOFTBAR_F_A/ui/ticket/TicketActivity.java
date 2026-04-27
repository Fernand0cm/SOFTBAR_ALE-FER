package com.SOFTBAR_F_A.ui.ticket;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;

public class TicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        Button btnCerrar = findViewById(R.id.btn_cerrar);
        btnCerrar.setOnClickListener(v -> finish());
    }
}
