package com.SOFTBAR_F_A.ui.mesas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.comanda.ComandaActivity;

public class MesasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);

        Button btnAbrirMesa = findViewById(R.id.btn_abrir_mesa);
        btnAbrirMesa.setOnClickListener(v ->
                startActivity(new Intent(this, ComandaActivity.class)));

        Button btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());
    }
}
