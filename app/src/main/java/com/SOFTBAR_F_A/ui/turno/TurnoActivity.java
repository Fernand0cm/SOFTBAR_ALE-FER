package com.SOFTBAR_F_A.ui.turno;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;

public class TurnoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turno);

        Button btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());
    }
}
