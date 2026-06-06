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
import com.SOFTBAR_F_A.data.Comanda;
import com.SOFTBAR_F_A.data.LineaComanda;
import com.SOFTBAR_F_A.data.Mesa;
import com.SOFTBAR_F_A.data.Turno;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.data.verifactu.ConfiguracionFiscal;
import com.SOFTBAR_F_A.data.verifactu.Factura;
import com.SOFTBAR_F_A.data.verifactu.GeneradorQrVerifactu;
import com.SOFTBAR_F_A.data.verifactu.HashVerifactu;
import com.SOFTBAR_F_A.ui.comanda.ComandaActivity;
import com.SOFTBAR_F_A.ui.common.Header;
import com.SOFTBAR_F_A.ui.mesas.MesasActivity;
import com.SOFTBAR_F_A.ui.ticket.TicketActivity;
import com.SOFTBAR_F_A.data.Producto;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CobroActivity extends AppCompatActivity {

    public static final String EXTRA_TOTAL = "total";
    public static final String EXTRA_LINEAS = "lineas";

    private static final double TIPO_IVA = 0.10;

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
        cobroEnCurso = true;
        btnConfirmar.setEnabled(false);

        Date ahora = new Date();
        Timestamp ts = new Timestamp(ahora);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            restaurarBotonCobro();
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
                        restaurarBotonCobro();
                        Toast.makeText(this, R.string.cobro_error_sin_turno,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    guardarCobro(ts, ahora, snap.getDocuments().get(0).getId(), user);
                })
                .addOnFailureListener(e -> {
                    restaurarBotonCobro();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void guardarCobro(Timestamp ts, Date ahora, String turnoId, FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat anyoFmt = new SimpleDateFormat("yyyy", Locale.ROOT);
        String anyo = anyoFmt.format(ahora);
        DocumentReference contadorRef = db.collection(FirestoreSchema.Collections.CONTADORES)
                .document(FirestoreSchema.Documents.CONTADOR_FACTURAS + "_" + anyo);
        DocumentReference configRef = db.collection(FirestoreSchema.Collections.CONFIGURACION)
                .document(FirestoreSchema.Documents.CONFIG_FISCAL);
        DocumentReference ventaRef = db.collection(FirestoreSchema.Collections.VENTAS).document();
        DocumentReference comandaRef = comandaId != null
                ? db.collection(FirestoreSchema.Collections.COMANDAS).document(comandaId)
                : null;

        db.runTransaction(transaction -> {
                    Comanda comanda = null;
                    if (comandaRef != null) {
                        DocumentSnapshot comandaDoc = transaction.get(comandaRef);
                        if (comandaDoc.exists()) {
                            comanda = comandaDoc.toObject(Comanda.class);
                        }
                    }

                    DocumentSnapshot configDoc = transaction.get(configRef);
                    ConfiguracionFiscal config = configDoc.exists()
                            ? configDoc.toObject(ConfiguracionFiscal.class)
                            : new ConfiguracionFiscal();
                    if (config == null) config = new ConfiguracionFiscal();

                    DocumentSnapshot contador = transaction.get(contadorRef);
                    long ultimo = contador.exists() && contador.getLong(FirestoreSchema.Fields.ULTIMO) != null
                            ? contador.getLong(FirestoreSchema.Fields.ULTIMO)
                            : 0L;
                    String hashAnterior = contador.exists()
                            ? contador.getString(FirestoreSchema.Fields.HASH_ULTIMO)
                            : "";
                    if (hashAnterior == null) hashAnterior = "";
                    int siguiente = (int) ultimo + 1;
                    List<LineaComanda> lineas = comanda != null ? comanda.getLineas() : lineasBarra;
                    if (lineas == null || lineas.isEmpty()) {
                        throw new FirebaseFirestoreException(
                                "No hay lineas para cobrar",
                                FirebaseFirestoreException.Code.ABORTED
                        );
                    }

                    Map<DocumentReference, Integer> nuevosStocks = new HashMap<>();
                    for (LineaComanda linea : lineas) {
                        if (linea.getCodigoBarras() == null) continue;

                        DocumentReference productoRef = db
                                .collection(FirestoreSchema.Collections.PRODUCTOS)
                                .document(linea.getCodigoBarras());

                        DocumentSnapshot productoDoc = transaction.get(productoRef);

                        if (!productoDoc.exists()) {
                            throw new FirebaseFirestoreException(
                                    "Producto no encontrado: " + linea.getNombre(),
                                    FirebaseFirestoreException.Code.ABORTED
                            );
                        }

                        Producto producto = productoDoc.toObject(Producto.class);

                        if (producto == null) {
                            throw new FirebaseFirestoreException(
                                    "Producto no valido: " + linea.getNombre(),
                                    FirebaseFirestoreException.Code.ABORTED
                            );
                        }

                        int stockActual = producto.getStock();
                        int cantidadVendida = linea.getCantidad();

                        if (stockActual < cantidadVendida) {
                            throw new FirebaseFirestoreException(
                                    "Stock insuficiente: " + producto.getNombre(),
                                    FirebaseFirestoreException.Code.ABORTED
                            );
                        }

                        nuevosStocks.put(
                                productoRef,
                                stockActual - cantidadVendida
                        );
                    }

                    for (Map.Entry<DocumentReference, Integer> entry : nuevosStocks.entrySet()) {
                        transaction.update(
                                entry.getKey(),
                                "stock",
                                entry.getValue()
                        );
                    }

                    int mesaNumero = comanda != null ? comanda.getMesaNumero() : 0;

                    Factura factura = construirFactura(
                            ts, ahora, siguiente, hashAnterior, config);
                    factura.setMetodo(metodoSeleccionado);
                    factura.setMesaId(mesaId);
                    factura.setMesaNumero(mesaNumero);
                    factura.setLineas(lineas);
                    factura.setPagoEfectivo(pagoEfectivo);
                    factura.setPagoTarjeta(pagoTarjeta);
                    factura.setImporteRecibido(importeRecibido);
                    factura.setCambio(cambio);
                    String facturaId = factura.getNumero().replace("/", "-");
                    DocumentReference facturaRef = db.collection(FirestoreSchema.Collections.FACTURAS)
                            .document(facturaId);

                    Venta venta = new Venta(ts, total, metodoSeleccionado);
                    venta.setFacturaId(facturaId);
                    venta.setComandaId(comandaId);
                    venta.setMesaId(mesaId);
                    venta.setMesaNumero(mesaNumero);
                    venta.setLineas(lineas);
                    venta.setTurnoId(turnoId);
                    venta.setUsuarioUid(user.getUid());
                    venta.setUsuarioEmail(user.getEmail());
                    venta.setPagoEfectivo(pagoEfectivo);
                    venta.setPagoTarjeta(pagoTarjeta);
                    venta.setImporteRecibido(importeRecibido);
                    venta.setCambio(cambio);

                    Map<String, Object> contadorData = new HashMap<>();
                    contadorData.put(FirestoreSchema.Fields.ULTIMO, siguiente);
                    contadorData.put(FirestoreSchema.Fields.HASH_ULTIMO, factura.getHashActual());

                    transaction.set(contadorRef, contadorData);
                    transaction.set(ventaRef, venta);
                    transaction.set(facturaRef, factura);

                    if (comandaId != null) {
                        transaction.update(
                                comandaRef,
                                FirestoreSchema.Fields.ESTADO, Comanda.PAGADA);
                    }
                    if (mesaId != null) {
                        transaction.update(
                                db.collection(FirestoreSchema.Collections.MESAS).document(mesaId),
                                FirestoreSchema.Fields.ESTADO, Mesa.LIBRE,
                                FirestoreSchema.Fields.COMANDA_ACTIVA_ID, null);
                    }

                    return new ResultadoCobro(ventaRef.getId(), facturaId);
                })
                .addOnSuccessListener(resultado ->
                        irAlTicket(resultado.ventaId, resultado.facturaId))
                .addOnFailureListener(e -> {
                    restaurarBotonCobro();
                    Toast.makeText(this,
                            e.getLocalizedMessage() != null
                                    ? e.getLocalizedMessage()
                                    : getString(R.string.cobro_error_guardar),
                            Toast.LENGTH_LONG).show();
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

        pagoEfectivo = Math.max(0, efectivo - Math.max(0, pagado - total));
        pagoTarjeta = tarjeta;
        importeRecibido = pagado;
        cambio = Math.max(0, pagado - total);
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

    private Factura construirFactura(Timestamp ts, Date ahora, int siguiente,
                                     String hashAnterior, ConfiguracionFiscal config) {
        SimpleDateFormat anyoFmt = new SimpleDateFormat("yyyy", Locale.ROOT);
        SimpleDateFormat fechaIso = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        SimpleDateFormat fechaQr = new SimpleDateFormat("dd-MM-yyyy", Locale.ROOT);

        String numero = String.format(Locale.ROOT, "%s-%04d/%s",
                config.getSerie(), siguiente, anyoFmt.format(ahora));
        double cuotaIva = total - (total / (1 + TIPO_IVA));

        String hashActual = HashVerifactu.calcular(
                numero, fechaIso.format(ahora), config.getNifEmisor(),
                total, cuotaIva, hashAnterior);

        String urlValidacion = GeneradorQrVerifactu.construirUrl(
                config.getNifEmisor(), numero, fechaQr.format(ahora), total);

        return new Factura(numero, ts, config.getNifEmisor(),
                total, cuotaIva, hashAnterior, hashActual, urlValidacion);
    }

    private void irAlTicket(String ventaId, String facturaId) {
        Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra(TicketActivity.EXTRA_VENTA_ID, ventaId);
        intent.putExtra(TicketActivity.EXTRA_FACTURA_ID, facturaId);
        startActivity(intent);
        finish();
    }

    private static class ResultadoCobro {
        final String ventaId;
        final String facturaId;

        ResultadoCobro(String ventaId, String facturaId) {
            this.ventaId = ventaId;
            this.facturaId = facturaId;
        }
    }
}
