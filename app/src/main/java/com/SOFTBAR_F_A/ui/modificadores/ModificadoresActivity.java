package com.SOFTBAR_F_A.ui.modificadores;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.common.Header;

public class ModificadoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        Header.aplica(this, getString(R.string.modificadores_title));
    }
}
