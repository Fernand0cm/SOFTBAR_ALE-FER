package com.SOFTBAR_F_A.ui.mesas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.comanda.ComandaActivity;

public class MesasActivity extends AppCompatActivity {

    // Estados de mesa hardcoded de momento (luego vendran de Firestore)
    private static final int[] ESTADOS = {
            R.color.mesa_libre,
            R.color.mesa_libre,
            R.color.mesa_ocupada,
            R.color.mesa_libre,
            R.color.mesa_ocupada,
            R.color.mesa_cobro,
            R.color.mesa_libre,
            R.color.mesa_cerrada
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);

        GridLayout grid = findViewById(R.id.grid_mesas);
        int margen = (int) (8 * getResources().getDisplayMetrics().density);
        int alto = (int) (110 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < ESTADOS.length; i++) {
            int numeroMesa = i + 1;

            TextView mesa = new TextView(this);
            mesa.setText(String.valueOf(numeroMesa));
            mesa.setTextSize(24);
            mesa.setTextColor(ContextCompat.getColor(this, R.color.white));
            mesa.setGravity(Gravity.CENTER);
            mesa.setBackgroundColor(ContextCompat.getColor(this, ESTADOS[i]));
            mesa.setClickable(true);
            mesa.setFocusable(true);
            mesa.setOnClickListener(v ->
                    startActivity(new Intent(this, ComandaActivity.class)));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = alto;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(margen, margen, margen, margen);
            mesa.setLayoutParams(params);

            grid.addView(mesa);
        }

        Button btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());
    }
}
