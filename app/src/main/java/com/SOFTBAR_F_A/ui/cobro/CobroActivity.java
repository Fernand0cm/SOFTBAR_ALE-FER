package com.SOFTBAR_F_A.ui.cobro;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Dinero;
import com.SOFTBAR_F_A.data.LineaComanda;
import com.SOFTBAR_F_A.data.repository.CobroRepository;
import com.SOFTBAR_F_A.ui.comanda.ComandaActivity;
import com.SOFTBAR_F_A.ui.common.ConexionUtil;
import com.SOFTBAR_F_A.ui.common.Header;
import com.SOFTBAR_F_A.ui.mesas.MesasActivity;
import com.SOFTBAR_F_A.ui.ticket.TicketActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

public class CobroActivity extends AppCompatActivity {

    public static final String EXTRA_TOTAL = "total";
    public static final String EXTRA_LINEAS = "lineas";

    private final CobroRepository cobroRepository = new CobroRepository();

    private double total;
    private String comandaId;
    private String mesaId;
    private List<LineaComanda> lineasBarra;
    private EditText inputPagoEfectivo;
    private EditText inputPagoTarjeta;
    private TextView txtCambio;
    private Button btnConfirmar;
    private boolean cobroEnCurso;
    private double pagoEfectivo;
    private double pagoTarjeta;
    private double importeRecibido;
    private double cambio;

    private String metodoSeleccionado = "Efectivo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobro);

        total = getIntent().getDoubleExtra(EXTRA_TOTAL, 0.0);
        comandaId = getIntent().getStringExtra(ComandaActivity.EXTRA_COMANDA_ID);
        mesaId = getIntent().getStringExtra(MesasActivity.EXTRA_MESA_ID);
        Object extraLineas = getIntent().getSerializableExtra(EXTRA_LINEAS);
        if (extraLineas instanceof List<?>) {
            lineasBarra = new java.util.ArrayList<>();
            for (Object item : (List<?>) extraLineas) {
                if (item instanceof LineaComanda) {
                    lineasBarra.add((LineaComanda) item);
                }
            }
        }

        Header.aplica(this, getString(R.string.cobro_title));

        TextView txtTotal = findViewById(R.id.txt_total_cobro);
        if (txtTotal != null) {
            txtTotal.setText(String.format(Locale.getDefault(), "%.2f EUR", total));
        }

        inputPagoEfectivo = findViewById(R.id.input_pago_efectivo);
        inputPagoTarjeta = findViewById(R.id.input_pago_tarjeta);
        txtCambio = findViewById(R.id.txt_cambio_cobro);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                refrescarCambio();
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        inputPagoEfectivo.addTextChangedListener(watcher);
        inputPagoTarjeta.addTextChangedListener(watcher);

        findViewById(R.id.btn_efectivo).setOnClickListener(v -> seleccionarMetodo(
                getString(R.string.cobro_efectivo)));
        findViewById(R.id.btn_tarjeta).setOnClickListener(v -> seleccionarMetodo(
                getString(R.string.cobro_tarjeta)));
        findViewById(R.id.btn_mixto).setOnClickListener(v -> seleccionarMetodo(
                getString(R.string.cobro_mixto)));

        btnConfirmar = findViewById(R.id.btn_confirmar);
        btnConfirmar.setOnClickListener(v -> confirmarVenta());

        seleccionarMetodo(getString(R.string.cobro_efectivo));
    }

    private void confirmarVenta() {
        if (cobroEnCurso) return;
        if (!validarPago()) {
            Toast.makeText(this, R.string.cobro_error_importe, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ConexionUtil.hayConexion(this)) {
            Toast.makeText(this, R.string.cobro_sin_conexion, Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        cobroEnCurso = true;
        btnConfirmar.setEnabled(false);

        CobroRepository.SolicitudCobro solicitud = new CobroRepository.SolicitudCobro();
        solicitud.total = total;
        solicitud.comandaId = comandaId;
        solicitud.mesaId = mesaId;
        solicitud.lineasBarra = lineasBarra;
        solicitud.metodo = metodoSeleccionado;
        solicitud.pagoEfectivo = pagoEfectivo;
        solicitud.pagoTarjeta = pagoTarjeta;
        solicitud.importeRecibido = importeRecibido;
        solicitud.cambio = cambio;

        cobroRepository.registrarCobro(solicitud, user, new CobroRepository.CobroCallback() {
            @Override
            public void onExito(String ventaId, String facturaId) {
                if (isFinishing() || isDestroyed()) return;
                irAlTicket(ventaId, facturaId);
            }

            @Override
            public void onSinTurno() {
                if (isFinishing() || isDestroyed()) return;
                restaurarBotonCobro();
                Toast.makeText(CobroActivity.this, R.string.cobro_error_sin_turno,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String mensaje) {
                if (isFinishing() || isDestroyed()) return;
                restaurarBotonCobro();
                Toast.makeText(CobroActivity.this,
                        mensaje != null ? mensaje : getString(R.string.cobro_error_guardar),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void restaurarBotonCobro() {
        cobroEnCurso = false;
        btnConfirmar.setEnabled(true);
    }

    private void seleccionarMetodo(String metodo) {
        metodoSeleccionado = metodo;
        if (metodo.equals(getString(R.string.cobro_efectivo))) {
            inputPagoEfectivo.setEnabled(true);
            inputPagoTarjeta.setEnabled(false);
            inputPagoEfectivo.setText(formatoNumero(total));
            inputPagoTarjeta.setText(formatoNumero(0));
        } else if (metodo.equals(getString(R.string.cobro_tarjeta))) {
            inputPagoEfectivo.setEnabled(false);
            inputPagoTarjeta.setEnabled(false);
            inputPagoEfectivo.setText(formatoNumero(0));
            inputPagoTarjeta.setText(formatoNumero(total));
        } else {
            inputPagoEfectivo.setEnabled(true);
            inputPagoTarjeta.setEnabled(true);
            inputPagoEfectivo.setText("");
            inputPagoTarjeta.setText(formatoNumero(total));
        }
        refrescarCambio();
    }

    private boolean validarPago() {
        double efectivo = leerImporte(inputPagoEfectivo);
        double tarjeta = leerImporte(inputPagoTarjeta);
        if (efectivo < 0 || tarjeta < 0) return false;
        if (tarjeta > total + 0.0001) return false;
        double pagado = efectivo + tarjeta;
        if (pagado + 0.0001 < total) return false;

        double cambioCalculado = Math.max(0, Dinero.restar(pagado, total));
        pagoEfectivo = Dinero.redondear(Math.max(0, Dinero.restar(efectivo, cambioCalculado)));
        pagoTarjeta = Dinero.redondear(tarjeta);
        importeRecibido = Dinero.redondear(pagado);
        cambio = cambioCalculado;
        return true;
    }

    private void refrescarCambio() {
        double pagado = leerImporte(inputPagoEfectivo) + leerImporte(inputPagoTarjeta);
        double cambioActual = Math.max(0, pagado - total);
        txtCambio.setText(String.format(Locale.getDefault(), "%.2f EUR", cambioActual));
    }

    private double leerImporte(EditText input) {
        if (input == null || input.getText() == null) return 0;
        String txt = input.getText().toString().trim();
        if (txt.isEmpty()) return 0;
        try {
            return Double.parseDouble(txt.replace(',', '.'));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String formatoNumero(double importe) {
        return String.format(Locale.ROOT, "%.2f", importe);
    }

    private void irAlTicket(String ventaId, String facturaId) {
        Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra(TicketActivity.EXTRA_VENTA_ID, ventaId);
        intent.putExtra(TicketActivity.EXTRA_FACTURA_ID, facturaId);
        startActivity(intent);
        finish();
    }
}
