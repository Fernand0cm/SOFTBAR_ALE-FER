package com.SOFTBAR_F_A.ui.comanda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.cobro.CobroActivity;
import com.SOFTBAR_F_A.ui.modificadores.ModificadoresActivity;

public class ComandaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comanda);

        Button btnExtras = findViewById(R.id.btn_extras);
        btnExtras.setOnClickListener(v ->
                startActivity(new Intent(this, ModificadoresActivity.class)));

        Button btnCobrar = findViewById(R.id.btn_cobrar);
        btnCobrar.setOnClickListener(v ->
                startActivity(new Intent(this, CobroActivity.class)));

        Button btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());
    }
}
