package com.SOFTBAR_F_A.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.ui.barra.BarraActivity;
import com.SOFTBAR_F_A.ui.caja.CajaActivity;
import com.SOFTBAR_F_A.ui.config.ConfigActivity;
import com.SOFTBAR_F_A.ui.informes.InformesActivity;
import com.SOFTBAR_F_A.ui.login.LoginActivity;
import com.SOFTBAR_F_A.ui.mesas.MesasActivity;
import com.SOFTBAR_F_A.ui.stock.StockActivity;
import com.SOFTBAR_F_A.ui.turno.TurnoActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView txtEmail = findViewById(R.id.txt_user_email);
        if (user != null && user.getEmail() != null) {
            txtEmail.setText(user.getEmail());
        }

        findViewById(R.id.btn_turno).setOnClickListener(v ->
                startActivity(new Intent(this, TurnoActivity.class)));
        findViewById(R.id.btn_mesas).setOnClickListener(v ->
                startActivity(new Intent(this, MesasActivity.class)));
        findViewById(R.id.btn_barra).setOnClickListener(v ->
                startActivity(new Intent(this, BarraActivity.class)));
        findViewById(R.id.btn_caja).setOnClickListener(v ->
                startActivity(new Intent(this, CajaActivity.class)));
        findViewById(R.id.btn_informes).setOnClickListener(v ->
                startActivity(new Intent(this, InformesActivity.class)));
        findViewById(R.id.btn_stock).setOnClickListener(v ->
                startActivity(new Intent(this, StockActivity.class)));
        findViewById(R.id.btn_config).setOnClickListener(v ->
                startActivity(new Intent(this, ConfigActivity.class)));

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
