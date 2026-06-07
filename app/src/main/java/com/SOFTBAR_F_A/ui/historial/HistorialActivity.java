package com.SOFTBAR_F_A.ui.historial;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.repository.HistorialRepository;
import com.SOFTBAR_F_A.ui.common.Header;
import com.SOFTBAR_F_A.ui.ticket.TicketActivity;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Historial de tickets: lista las ultimas ventas (mas recientes primero) y
 * permite reabrir el ticket de cada una. No accede a Firestore directamente:
 * delega en {@link HistorialRepository} y gestiona la suscripcion segun el
 * ciclo de vida.
 */
public class HistorialActivity extends AppCompatActivity {

    private static final int LIMITE = 50;

    private LinearLayout lista;
    private TextView txtMensaje;
    private final SimpleDateFormat horaFmt =
            new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
    private final HistorialRepository repositorio = new HistorialRepository();
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        Header.aplica(this, getString(R.string.historial_title),
                getString(R.string.historial_subtitulo));

        lista = findViewById(R.id.lista_historial);
        txtMensaje = findViewById(R.id.txt_historial_mensaje);

        mostrarMensaje(getString(R.string.historial_cargando));
        suscripcion = repositorio.escucharHistorial(LIMITE,
                new HistorialRepository.HistorialListener() {
                    @Override
                    public void onVentas(List<HistorialRepository.VentaItem> ventas) {
                        if (isFinishing() || isDestroyed()) return;
                        pintar(ventas);
                    }

                    @Override
                    public void onError(String mensaje) {
                        if (isFinishing() || isDestroyed()) return;
                        mostrarMensaje(getString(R.string.historial_error,
                                mensaje != null ? mensaje : ""));
                    }
                });
    }

    private void pintar(List<HistorialRepository.VentaItem> ventas) {
        lista.removeAllViews();
        if (ventas.isEmpty()) {
            mostrarMensaje(getString(R.string.historial_vacio));
            return;
        }
        txtMensaje.setVisibility(View.GONE);
        lista.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (HistorialRepository.VentaItem item : ventas) {
            Venta v = item.venta;
            View fila = inflater.inflate(R.layout.item_historial, lista, false);

            ((TextView) fila.findViewById(R.id.txt_hist_numero))
                    .setText(v.getFacturaId() != null
                            ? v.getFacturaId()
                            : getString(R.string.historial_sin_numero));

            String hora = v.getFecha() != null
                    ? horaFmt.format(v.getFecha().toDate())
                    : "";
            String metodo = v.getMetodo() != null ? v.getMetodo() : "";
            ((TextView) fila.findViewById(R.id.txt_hist_detalle))
                    .setText(getString(R.string.historial_detalle, hora, metodo));

            ((TextView) fila.findViewById(R.id.txt_hist_total))
                    .setText(String.format(Locale.getDefault(), "%.2f EUR", v.getTotal()));

            fila.setOnClickListener(view -> abrirTicket(item.id, v.getFacturaId()));
            lista.addView(fila);
        }
    }

    private void abrirTicket(String ventaId, String facturaId) {
        Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra(TicketActivity.EXTRA_VENTA_ID, ventaId);
        intent.putExtra(TicketActivity.EXTRA_FACTURA_ID, facturaId);
        startActivity(intent);
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
