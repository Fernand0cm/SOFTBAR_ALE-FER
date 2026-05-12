package com.SOFTBAR_F_A.ui.caja;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.MovimientoCaja;
import com.SOFTBAR_F_A.data.ResumenCaja;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CajaActivity extends AppCompatActivity {

    private static final String COL_VENTAS = "ventas";
    private static final String COL_MOVIMIENTOS = "movimientos_caja";

    private TextView txtApertura, txtEfectivo, txtTarjeta, txtRetiradas, txtTotalEsperado;
    private LinearLayout listaMovimientos;
    private TextView txtMovimientosVacios;

    private final List<Venta> ventasDelDia = new ArrayList<>();
    private final List<MovimientoCaja> movimientosDelDia = new ArrayList<>();
    private final SimpleDateFormat horaFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private ListenerRegistration suscripcionVentas;
    private ListenerRegistration suscripcionMovimientos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caja);

        Header.aplica(this, getString(R.string.caja_title), getString(R.string.caja_turno_ejemplo));

        txtApertura = findViewById(R.id.txt_caja_apertura);
        txtEfectivo = findViewById(R.id.txt_caja_efectivo);
        txtTarjeta = findViewById(R.id.txt_caja_tarjeta);
        txtRetiradas = findViewById(R.id.txt_caja_retiradas);
        txtTotalEsperado = findViewById(R.id.txt_caja_total);
        listaMovimientos = findViewById(R.id.lista_movimientos);
        txtMovimientosVacios = findViewById(R.id.txt_movimientos_vacios);

        Button btnMovimiento = findViewById(R.id.btn_movimiento);
        btnMovimiento.setOnClickListener(v -> mostrarDialogMovimiento());

        Button btnCierre = findViewById(R.id.btn_cierre);
        btnCierre.setOnClickListener(v ->
                Toast.makeText(this, R.string.caja_pendiente, Toast.LENGTH_SHORT).show());

        suscribirseAVentas();
        suscribirseAMovimientos();
    }

    private Timestamp inicioDelDia() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return new Timestamp(c.getTime());
    }

    private void suscribirseAVentas() {
        suscripcionVentas = FirebaseFirestore.getInstance()
                .collection(COL_VENTAS)
                .whereGreaterThanOrEqualTo("fecha", inicioDelDia())
                .orderBy("fecha", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    ventasDelDia.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        ventasDelDia.add(doc.toObject(Venta.class));
                    }
                    refrescarPantalla();
                });
    }

    private void suscribirseAMovimientos() {
        suscripcionMovimientos = FirebaseFirestore.getInstance()
                .collection(COL_MOVIMIENTOS)
                .whereGreaterThanOrEqualTo("fecha", inicioDelDia())
                .orderBy("fecha", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    movimientosDelDia.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        movimientosDelDia.add(doc.toObject(MovimientoCaja.class));
                    }
                    refrescarPantalla();
                });
    }

    private void refrescarPantalla() {
        ResumenCaja resumen = ResumenCaja.calcular(
                ventasDelDia, movimientosDelDia,
                getString(R.string.cobro_efectivo),
                getString(R.string.cobro_tarjeta));

        txtApertura.setText(formato(resumen.getApertura()));
        txtEfectivo.setText(formato(resumen.getVentasEfectivo()));
        txtTarjeta.setText(formato(resumen.getVentasTarjeta()));
        txtRetiradas.setText("-" + formato(resumen.getRetiradas()));
        txtTotalEsperado.setText(formato(resumen.totalEsperado()));

        pintarMovimientos();
    }

    private String formato(double importe) {
        return String.format(Locale.getDefault(), "%.2f EUR", importe);
    }

    private void pintarMovimientos() {
        listaMovimientos.removeAllViews();

        List<Object> filas = new ArrayList<>();
        filas.addAll(movimientosDelDia);
        filas.addAll(ventasDelDia);

        if (filas.isEmpty()) {
            txtMovimientosVacios.setVisibility(View.VISIBLE);
            return;
        }
        txtMovimientosVacios.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MovimientoCaja m : movimientosDelDia) {
            View item = inflater.inflate(R.layout.item_movimiento_caja, listaMovimientos, false);
            ((TextView) item.findViewById(R.id.txt_hora)).setText(
                    m.getFecha() != null ? horaFmt.format(m.getFecha().toDate()) : "");
            ((TextView) item.findViewById(R.id.txt_descripcion))
                    .setText(etiquetaMovimiento(m));
            TextView importe = item.findViewById(R.id.txt_importe);
            if (MovimientoCaja.RETIRADA.equals(m.getTipo())) {
                importe.setText("-" + formato(Math.abs(m.getImporte())));
                importe.setTextColor(ContextCompat.getColor(this, R.color.warning));
            } else {
                importe.setText(formato(m.getImporte()));
            }
            listaMovimientos.addView(item);
        }

        for (Venta v : ventasDelDia) {
            View item = inflater.inflate(R.layout.item_movimiento_caja, listaMovimientos, false);
            ((TextView) item.findViewById(R.id.txt_hora)).setText(
                    v.getFecha() != null ? horaFmt.format(v.getFecha().toDate()) : "");
            String descripcion = getString(R.string.caja_mov_venta);
            if (v.getMetodo() != null) descripcion += " - " + v.getMetodo();
            ((TextView) item.findViewById(R.id.txt_descripcion)).setText(descripcion);
            ((TextView) item.findViewById(R.id.txt_importe)).setText(formato(v.getTotal()));
            listaMovimientos.addView(item);
        }
    }

    private String etiquetaMovimiento(MovimientoCaja m) {
        if (m.getDescripcion() != null && !m.getDescripcion().trim().isEmpty()) {
            return m.getDescripcion();
        }
        if (m.getTipo() == null) return "";
        switch (m.getTipo()) {
            case MovimientoCaja.APERTURA: return getString(R.string.caja_mov_apertura);
            case MovimientoCaja.ENTRADA:  return getString(R.string.movimiento_entrada);
            case MovimientoCaja.RETIRADA: return getString(R.string.caja_mov_retirada);
            default: return m.getTipo();
        }
    }

    private void mostrarDialogMovimiento() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_movimiento, null);
        RadioGroup grupo = vista.findViewById(R.id.grupo_tipo);
        EditText inputImporte = vista.findViewById(R.id.input_importe);
        EditText inputDescripcion = vista.findViewById(R.id.input_descripcion);
        grupo.check(R.id.radio_entrada);

        new AlertDialog.Builder(this)
                .setTitle(R.string.movimiento_titulo)
                .setView(vista)
                .setPositiveButton(R.string.dialog_guardar, (d, w) -> {
                    String tipo = tipoSeleccionado(grupo.getCheckedRadioButtonId());
                    String txt = inputImporte.getText().toString().trim();
                    if (TextUtils.isEmpty(tipo) || TextUtils.isEmpty(txt)) {
                        Toast.makeText(this, R.string.movimiento_error,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double importe;
                    try {
                        importe = Double.parseDouble(txt.replace(',', '.'));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.dialog_error_precio,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    guardarMovimiento(tipo, importe,
                            inputDescripcion.getText().toString().trim());
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }

    private String tipoSeleccionado(int idRadio) {
        if (idRadio == R.id.radio_apertura) return MovimientoCaja.APERTURA;
        if (idRadio == R.id.radio_entrada)  return MovimientoCaja.ENTRADA;
        if (idRadio == R.id.radio_retirada) return MovimientoCaja.RETIRADA;
        return null;
    }

    private void guardarMovimiento(String tipo, double importe, String descripcion) {
        MovimientoCaja m = new MovimientoCaja(
                new Timestamp(new Date()), tipo, importe, descripcion);
        FirebaseFirestore.getInstance().collection(COL_MOVIMIENTOS).add(m);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcionVentas != null) suscripcionVentas.remove();
        if (suscripcionMovimientos != null) suscripcionMovimientos.remove();
    }
}
