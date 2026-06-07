package com.SOFTBAR_F_A.ui.cierres;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Turno;
import com.SOFTBAR_F_A.data.repository.CierresRepository;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Consulta de cierres historicos: lista los turnos cerrados con su arqueo
 * (esperado, contado y diferencia). No accede a Firestore directamente: delega
 * en {@link CierresRepository} y gestiona la suscripcion segun el ciclo de vida.
 */
public class CierresActivity extends AppCompatActivity {

    private LinearLayout lista;
    private TextView txtMensaje;
    private final SimpleDateFormat fechaFmt =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final CierresRepository repositorio = new CierresRepository();
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cierres);

        Header.aplica(this, getString(R.string.cierres_title),
                getString(R.string.cierres_subtitulo));

        lista = findViewById(R.id.lista_cierres);
        txtMensaje = findViewById(R.id.txt_cierres_mensaje);

        mostrarMensaje(getString(R.string.cierres_cargando));
        suscripcion = repositorio.escucharCierres(new CierresRepository.CierresListener() {
            @Override
            public void onCierres(List<Turno> cierres) {
                if (isFinishing() || isDestroyed()) return;
                pintar(cierres);
            }

            @Override
            public void onError(String mensaje) {
                if (isFinishing() || isDestroyed()) return;
                mostrarMensaje(getString(R.string.cierres_error,
                        mensaje != null ? mensaje : ""));
            }
        });
    }

    private void pintar(List<Turno> cierres) {
        lista.removeAllViews();
        if (cierres.isEmpty()) {
            mostrarMensaje(getString(R.string.cierres_vacio));
            return;
        }
        txtMensaje.setVisibility(View.GONE);
        lista.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Turno t : cierres) {
            View fila = inflater.inflate(R.layout.item_cierre, lista, false);

            Timestamp fc = t.getFechaCierre();
            ((TextView) fila.findViewById(R.id.txt_cierre_fecha))
                    .setText(fc != null ? fechaFmt.format(fc.toDate()) : "");

            double diferencia = t.getDiferenciaCaja();
            TextView txtDif = fila.findViewById(R.id.txt_cierre_diferencia);
            txtDif.setText(String.format(Locale.getDefault(), "%+.2f EUR", diferencia));
            txtDif.setTextColor(ContextCompat.getColor(this,
                    diferencia < 0 ? R.color.warning : R.color.brand_600));

            ((TextView) fila.findViewById(R.id.txt_cierre_detalle))
                    .setText(getString(R.string.cierres_detalle,
                            t.getEfectivoEsperado(), t.getEfectivoContado()));

            lista.addView(fila);
        }
    }

    private void mostrarMensaje(String mensaje) {
        lista.setVisibility(View.GONE);
        txtMensaje.setVisibility(View.VISIBLE);
        txtMensaje.setText(mensaje);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) suscripcion.remove();
    }
}
