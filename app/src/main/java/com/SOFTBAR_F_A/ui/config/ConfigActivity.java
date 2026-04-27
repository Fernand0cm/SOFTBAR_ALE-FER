package com.SOFTBAR_F_A.ui.config;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Button btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());
    }
}
