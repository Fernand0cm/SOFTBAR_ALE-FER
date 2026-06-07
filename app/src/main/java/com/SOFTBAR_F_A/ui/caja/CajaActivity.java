package com.SOFTBAR_F_A.ui.caja;

import android.os.Bundle;
import android.text.InputType;
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
import com.SOFTBAR_F_A.data.Turno;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CajaActivity extends AppCompatActivity {

    private TextView txtApertura, txtEfectivo, txtTarjeta, txtRetiradas, txtTotalEsperado;
    private TextView txtContado, txtDiferencia;
    private LinearLayout listaMovimientos;
    private TextView txtMovimientosVacios;
    private Button btnMovimiento;
    private Button btnCierre;

    private final List<Venta> ventasDelTurno = new ArrayList<>();
    private final List<MovimientoCaja> movimientosDelTurno = new ArrayList<>();
    private final SimpleDateFormat horaFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private String turnoActivoId;
    private ResumenCaja resumenActual;
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
        txtContado = findViewById(R.id.txt_caja_contado);
        txtDiferencia = findViewById(R.id.txt_caja_diferencia);
        listaMovimientos = findViewById(R.id.lista_movimientos);
        txtMovimientosVacios = findViewById(R.id.txt_movimientos_vacios);

        btnMovimiento = findViewById(R.id.btn_movimiento);
        btnMovimiento.setOnClickListener(v -> mostrarDialogMovimiento());

        btnCierre = findViewById(R.id.btn_cierre);
        btnCierre.setOnClickListener(v -> cerrarTurno());

        cargarTurnoActivo();
    }

    private void cargarTurnoActivo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            pintarSinTurno();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.TURNOS)
                .whereEqualTo(FirestoreSchema.Fields.ESTADO, Turno.ABIERTO)
                .whereEqualTo(FirestoreSchema.Fields.USUARIO_UID, user.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) {
                        pintarSinTurno();
                        return;
                    }
                    turnoActivoId = snap.getDocuments().get(0).getId();
                    btnMovimiento.setEnabled(true);
                    btnCierre.setEnabled(true);
                    txtMovimientosVacios.setText(R.string.caja_movimientos_vacios);
                    suscribirseAVentas();
                    suscribirseAMovimientos();
                });
    }

    private void suscribirseAVentas() {
        suscripcionVentas = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.VENTAS)
                .whereEqualTo(FirestoreSchema.Fields.TURNO_ID, turnoActivoId)
                .orderBy(FirestoreSchema.Fields.FECHA, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    ventasDelTurno.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        ventasDelTurno.add(doc.toObject(Venta.class));
                    }
                    refrescarPantalla();
                });
    }

    private void suscribirseAMovimientos() {
        suscripcionMovimientos = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.MOVIMIENTOS_CAJA)
                .whereEqualTo(FirestoreSchema.Fields.TURNO_ID, turnoActivoId)
                .orderBy(FirestoreSchema.Fields.FECHA, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    movimientosDelTurno.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        movimientosDelTurno.add(doc.toObject(MovimientoCaja.class));
                    }
                    refrescarPantalla();
                });
    }

    private void refrescarPantalla() {
        resumenActual = ResumenCaja.calcular(
                ventasDelTurno, movimientosDelTurno,
                getString(R.string.cobro_efectivo),
                getString(R.string.cobro_tarjeta));

        txtApertura.setText(formato(resumenActual.getApertura()));
        txtEfectivo.setText(formato(resumenActual.getVentasEfectivo()));
        txtTarjeta.setText(formato(resumenActual.getVentasTarjeta()));
        txtRetiradas.setText("-" + formato(resumenActual.getRetiradas()));
        txtTotalEsperado.setText(formato(resumenActual.efectivoEsperado()));
        txtContado.setText("--");
        txtDiferencia.setText("--");
        txtDiferencia.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        pintarMovimientos();
    }

    private String formato(double importe) {
        return String.format(Locale.getDefault(), "%.2f EUR", importe);
    }

    private void pintarMovimientos() {
        listaMovimientos.removeAllViews();

        List<Object> filas = new ArrayList<>();
        filas.addAll(movimientosDelTurno);
        filas.addAll(ventasDelTurno);

        if (filas.isEmpty()) {
            txtMovimientosVacios.setVisibility(View.VISIBLE);
            return;
        }
        txtMovimientosVacios.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MovimientoCaja m : movimientosDelTurno) {
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

        for (Venta v : ventasDelTurno) {
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
        if (turnoActivoId == null) {
            Toast.makeText(this, R.string.turno_error_sin_turno, Toast.LENGTH_SHORT).show();
            return;
        }

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
        m.setTurnoId(turnoActivoId);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            m.setUsuarioUid(user.getUid());
            m.setUsuarioEmail(user.getEmail());
        }
        FirebaseFirestore.getInstance().collection(FirestoreSchema.Collections.MOVIMIENTOS_CAJA).add(m);
    }

    private void cerrarTurno() {
        if (turnoActivoId == null) {
            Toast.makeText(this, R.string.turno_error_sin_turno, Toast.LENGTH_SHORT).show();
            return;
        }
        if (resumenActual == null) {
            resumenActual = ResumenCaja.calcular(
                    ventasDelTurno, movimientosDelTurno,
                    getString(R.string.cobro_efectivo),
                    getString(R.string.cobro_tarjeta));
        }

        final double esperado = resumenActual.efectivoEsperado();
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(padding, padding / 2, padding, 0);

        TextView txtEsperado = new TextView(this);
        txtEsperado.setText(getString(R.string.caja_cierre_esperado, esperado));
        txtEsperado.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        contenedor.addView(txtEsperado);

        EditText inputContado = new EditText(this);
        inputContado.setHint(R.string.caja_cierre_contado_hint);
        inputContado.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        inputContado.setSingleLine(true);
        contenedor.addView(inputContado);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.caja_cierre_titulo)
                .setView(contenedor)
                .setNegativeButton(R.string.dialog_cancelar, null)
                .setPositiveButton(R.string.caja_cierre_turno, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String txt = inputContado.getText().toString().trim();
                    if (TextUtils.isEmpty(txt)) {
                        Toast.makeText(this, R.string.caja_cierre_error_contado,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double contado;
                    try {
                        contado = Double.parseDouble(txt.replace(',', '.'));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.dialog_error_precio,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    confirmarCierre(contado, esperado, dialog);
                }));
        dialog.show();
    }

    private void confirmarCierre(double contado, double esperado, AlertDialog dialogContado) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.caja_cierre_confirmar_titulo)
                .setMessage(R.string.caja_cierre_confirmar_mensaje)
                .setNegativeButton(R.string.dialog_cancelar, null)
                .setPositiveButton(R.string.caja_cierre_confirmar_ok,
                        (d, w) -> guardarCierre(contado, esperado, dialogContado))
                .show();
    }

    private void guardarCierre(double contado, double esperado, AlertDialog dialog) {
        double diferencia = contado - esperado;
        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.TURNOS)
                .document(turnoActivoId)
                .update(
                        FirestoreSchema.Fields.ESTADO, Turno.CERRADO,
                        FirestoreSchema.Fields.FECHA_CIERRE, Timestamp.now(),
                        FirestoreSchema.Fields.EFECTIVO_CONTADO, contado,
                        FirestoreSchema.Fields.EFECTIVO_ESPERADO, esperado,
                        FirestoreSchema.Fields.DIFERENCIA_CAJA, diferencia)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    txtContado.setText(formato(contado));
                    txtDiferencia.setText(formato(diferencia));
                    txtDiferencia.setTextColor(ContextCompat.getColor(this,
                            diferencia < 0 ? R.color.warning : R.color.brand_600));
                    Toast.makeText(this,
                            getString(R.string.caja_cierre_resumen, contado, diferencia),
                            Toast.LENGTH_LONG).show();
                    limpiarSuscripciones();
                    pintarTurnoCerrado(contado, diferencia);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private void pintarTurnoCerrado(double contado, double diferencia) {
        turnoActivoId = null;
        ventasDelTurno.clear();
        movimientosDelTurno.clear();
        txtContado.setText(formato(contado));
        txtDiferencia.setText(formato(diferencia));
        txtDiferencia.setTextColor(ContextCompat.getColor(this,
                diferencia < 0 ? R.color.warning : R.color.brand_600));
        listaMovimientos.removeAllViews();
        txtMovimientosVacios.setText(R.string.turno_detalle_vacio);
        txtMovimientosVacios.setVisibility(View.VISIBLE);
        btnMovimiento.setEnabled(false);
        btnCierre.setEnabled(false);
    }

    private void pintarSinTurno() {
        turnoActivoId = null;
        ventasDelTurno.clear();
        movimientosDelTurno.clear();
        txtApertura.setText(formato(0));
        txtEfectivo.setText(formato(0));
        txtTarjeta.setText(formato(0));
        txtRetiradas.setText("-" + formato(0));
        txtTotalEsperado.setText(formato(0));
        txtContado.setText("--");
        txtDiferencia.setText("--");
        txtDiferencia.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        listaMovimientos.removeAllViews();
        txtMovimientosVacios.setText(R.string.turno_detalle_vacio);
        txtMovimientosVacios.setVisibility(View.VISIBLE);
        btnMovimiento.setEnabled(false);
        btnCierre.setEnabled(false);
    }

    private void limpiarSuscripciones() {
        if (suscripcionVentas != null) {
            suscripcionVentas.remove();
            suscripcionVentas = null;
        }
        if (suscripcionMovimientos != null) {
            suscripcionMovimientos.remove();
            suscripcionMovimientos = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        limpiarSuscripciones();
    }
}
