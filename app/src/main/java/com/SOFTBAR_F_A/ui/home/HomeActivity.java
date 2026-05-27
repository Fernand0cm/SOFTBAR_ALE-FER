package com.SOFTBAR_F_A.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import com.SOFTBAR_F_A.ui.ticket.HistorialTicketsActivity;

public class HomeActivity extends AppCompatActivity {

    private ConnectivityManager.NetworkCallback networkCallback;
    private View dotConexion;
    private TextView txtConexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView txtEmail = findViewById(R.id.txt_user_email);
        if (user != null && user.getEmail() != null) {
            txtEmail.setText(user.getEmail());
        }

        dotConexion = findViewById(R.id.dot_conexion);
        txtConexion = findViewById(R.id.txt_conexion);

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
        findViewById(R.id.btn_historial).setOnClickListener(v ->
                startActivity(new Intent(this, HistorialTicketsActivity.class)));
        findViewById(R.id.btn_stock).setOnClickListener(v ->
                startActivity(new Intent(this, StockActivity.class)));
        findViewById(R.id.btn_config).setOnClickListener(v ->
                startActivity(new Intent(this, ConfigActivity.class)));
        findViewById(R.id.btn_historial).setOnClickListener(v ->
                startActivity(new Intent(this, HistorialTicketsActivity.class)));

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        registrarObservadorRed();
    }

    private void registrarObservadorRed() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        // Estado inicial
        actualizarEstadoConexion(hayInternet(cm));

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> actualizarEstadoConexion(true));
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> actualizarEstadoConexion(false));
            }
        };
        cm.registerNetworkCallback(request, networkCallback);
    }

    private boolean hayInternet(ConnectivityManager cm) {
        Network red = cm.getActiveNetwork();
        if (red == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(red);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void actualizarEstadoConexion(boolean online) {
        if (dotConexion == null || txtConexion == null) return;
        int color = online ? R.color.mesa_libre : R.color.mesa_cerrada;
        int texto = online ? R.string.conexion_online : R.string.conexion_offline;
        dotConexion.setBackgroundColor(ContextCompat.getColor(this, color));
        txtConexion.setText(texto);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            ConnectivityManager cm = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                try {
                    cm.unregisterNetworkCallback(networkCallback);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }
}
