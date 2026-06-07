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
import com.SOFTBAR_F_A.data.Permisos;
import com.SOFTBAR_F_A.data.SesionUsuario;
import com.SOFTBAR_F_A.data.Usuario;
import com.SOFTBAR_F_A.data.repository.UsuarioRepository;
import com.SOFTBAR_F_A.ui.barra.BarraActivity;
import com.SOFTBAR_F_A.ui.caja.CajaActivity;
import com.SOFTBAR_F_A.ui.config.ConfigActivity;
import com.SOFTBAR_F_A.ui.historial.HistorialActivity;
import com.SOFTBAR_F_A.ui.informes.InformesActivity;
import com.SOFTBAR_F_A.ui.login.LoginActivity;
import com.SOFTBAR_F_A.ui.mesas.MesasActivity;
import com.SOFTBAR_F_A.ui.stock.StockActivity;
import com.SOFTBAR_F_A.ui.turno.TurnoActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        if (user != null) {
            cargarUsuario(user, txtEmail);
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
        findViewById(R.id.btn_stock).setOnClickListener(v ->
                startActivity(new Intent(this, StockActivity.class)));
        findViewById(R.id.btn_historial).setOnClickListener(v ->
                startActivity(new Intent(this, HistorialActivity.class)));
        findViewById(R.id.btn_config).setOnClickListener(v ->
                startActivity(new Intent(this, ConfigActivity.class)));

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> confirmarLogout());

        registrarObservadorRed();
    }

    private void cargarUsuario(FirebaseUser user, TextView txtEmail) {
        new UsuarioRepository().cargarOCrear(user, new UsuarioRepository.UsuarioCallback() {
            @Override
            public void onUsuario(Usuario usuario) {
                if (isFinishing() || isDestroyed()) return;
                SesionUsuario.establecer(usuario);
                txtEmail.setText(getString(R.string.home_usuario_rol,
                        usuario.getEmail(), usuario.getRol()));
                aplicarPermisos();
            }

            @Override
            public void onError(String mensaje) {
                // Si no se puede cargar el perfil, no bloqueamos la navegacion.
            }
        });
    }

    private void aplicarPermisos() {
        gancho(R.id.btn_turno, Permisos.TURNO);
        gancho(R.id.btn_mesas, Permisos.MESAS);
        gancho(R.id.btn_barra, Permisos.BARRA);
        gancho(R.id.btn_caja, Permisos.CAJA);
        gancho(R.id.btn_informes, Permisos.INFORMES);
        gancho(R.id.btn_stock, Permisos.STOCK);
        gancho(R.id.btn_historial, Permisos.HISTORIAL);
        gancho(R.id.btn_config, Permisos.CONFIG);
    }

    private void gancho(int idBoton, String modulo) {
        View boton = findViewById(idBoton);
        if (boton != null) {
            boton.setVisibility(SesionUsuario.puede(modulo) ? View.VISIBLE : View.GONE);
        }
    }

    private void confirmarLogout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.home_logout_confirmar_titulo)
                .setMessage(R.string.home_logout_confirmar_mensaje)
                .setNegativeButton(R.string.dialog_cancelar, null)
                .setPositiveButton(R.string.home_logout, (d, w) -> {
                    SesionUsuario.limpiar();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .show();
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
