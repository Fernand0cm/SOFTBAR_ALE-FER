package com.SOFTBAR_F_A.ui.comanda;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.cobro.CobroActivity;
import com.SOFTBAR_F_A.ui.common.Header;
import com.SOFTBAR_F_A.ui.mesas.MesasActivity;
import com.SOFTBAR_F_A.ui.modificadores.ModificadoresActivity;

public class ComandaActivity extends AppCompatActivity {

    private String mesaId;
    private int mesaNumero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comanda);

        mesaId = getIntent().getStringExtra(MesasActivity.EXTRA_MESA_ID);
        mesaNumero = getIntent().getIntExtra(MesasActivity.EXTRA_MESA_NUMERO, 0);

        String subtitulo = mesaNumero > 0
                ? getString(R.string.comanda_mesa_numero, mesaNumero)
                : null;
        Header.aplica(this, getString(R.string.comanda_title), subtitulo);

        Button btnExtras = findViewById(R.id.btn_extras);
        btnExtras.setOnClickListener(v ->
                startActivity(new Intent(this, ModificadoresActivity.class)));

        Button btnCobrar = findViewById(R.id.btn_cobrar);
        btnCobrar.setOnClickListener(v ->
                startActivity(new Intent(this, CobroActivity.class)));
    }
}
