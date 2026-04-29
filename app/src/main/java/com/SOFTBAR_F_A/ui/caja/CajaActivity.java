package com.SOFTBAR_F_A.ui.caja;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.common.Header;

public class CajaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caja);

        Header.aplica(this, getString(R.string.caja_title), getString(R.string.caja_turno_ejemplo));

        Button btnMovimiento = findViewById(R.id.btn_movimiento);
        btnMovimiento.setOnClickListener(v ->
                Toast.makeText(this, R.string.caja_pendiente, Toast.LENGTH_SHORT).show());

        Button btnCierre = findViewById(R.id.btn_cierre);
        btnCierre.setOnClickListener(v ->
                Toast.makeText(this, R.string.caja_pendiente, Toast.LENGTH_SHORT).show());
    }
}
