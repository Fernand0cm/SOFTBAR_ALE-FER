package com.SOFTBAR_F_A.ui.cobro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Comanda;
import com.SOFTBAR_F_A.data.Mesa;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.data.verifactu.Factura;
import com.SOFTBAR_F_A.data.verifactu.GeneradorQrVerifactu;
import com.SOFTBAR_F_A.data.verifactu.HashVerifactu;
import com.SOFTBAR_F_A.ui.comanda.ComandaActivity;
import com.SOFTBAR_F_A.ui.common.Header;
import com.SOFTBAR_F_A.ui.mesas.MesasActivity;
import com.SOFTBAR_F_A.ui.ticket.TicketActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CobroActivity extends AppCompatActivity {

    public static final String EXTRA_TOTAL = "total";

    private static final double TIPO_IVA = 0.10;
    private static final String NIF_EMISOR = "B12345678";

    private double total;
    private String comandaId;
    private String mesaId;
    private Button btnConfirmar;
    private boolean cobroEnCurso;

    private String metodoSeleccionado = "Efectivo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobro);

        total = getIntent().getDoubleExtra(EXTRA_TOTAL, 0.0);
        comandaId = getIntent().getStringExtra(ComandaActivity.EXTRA_COMANDA_ID);
        mesaId = getIntent().getStringExtra(MesasActivity.EXTRA_MESA_ID);

        Header.aplica(this, getString(R.string.cobro_title));

        TextView txtTotal = findViewById(R.id.txt_total_cobro);
        if (txtTotal != null) {
            txtTotal.setText(String.format(Locale.getDefault(), "%.2f EUR", total));
        }

        findViewById(R.id.btn_efectivo).setOnClickListener(v ->
                metodoSeleccionado = getString(R.string.cobro_efectivo));
        findViewById(R.id.btn_tarjeta).setOnClickListener(v ->
                metodoSeleccionado = getString(R.string.cobro_tarjeta));
        findViewById(R.id.btn_mixto).setOnClickListener(v ->
                metodoSeleccionado = getString(R.string.cobro_mixto));

        btnConfirmar = findViewById(R.id.btn_confirmar);
        btnConfirmar.setOnClickListener(v -> confirmarVenta());
    }

    private void confirmarVenta() {
        if (cobroEnCurso) return;
        cobroEnCurso = true;
        btnConfirmar.setEnabled(false);

        Date ahora = new Date();
        Timestamp ts = new Timestamp(ahora);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference contadorRef = db.collection(FirestoreSchema.Collections.CONTADORES)
                .document(FirestoreSchema.Documents.CONTADOR_FACTURAS);
        DocumentReference ventaRef = db.collection(FirestoreSchema.Collections.VENTAS).document();

        db.runTransaction(transaction -> {
                    DocumentSnapshot contador = transaction.get(contadorRef);
                    long ultimo = contador.exists() && contador.getLong(FirestoreSchema.Fields.ULTIMO) != null
                            ? contador.getLong(FirestoreSchema.Fields.ULTIMO)
                            : 0L;
                    String hashAnterior = contador.exists()
                            ? contador.getString(FirestoreSchema.Fields.HASH_ULTIMO)
                            : "";
                    if (hashAnterior == null) hashAnterior = "";
                    int siguiente = (int) ultimo + 1;

                    Factura factura = construirFactura(ts, ahora, siguiente, hashAnterior);
                    String facturaId = factura.getNumero().replace("/", "-");
                    DocumentReference facturaRef = db.collection(FirestoreSchema.Collections.FACTURAS)
                            .document(facturaId);

                    Venta venta = new Venta(ts, total, metodoSeleccionado);
                    venta.setFacturaId(facturaId);
                    venta.setComandaId(comandaId);
                    venta.setMesaId(mesaId);

                    Map<String, Object> contadorData = new HashMap<>();
                    contadorData.put(FirestoreSchema.Fields.ULTIMO, siguiente);
                    contadorData.put(FirestoreSchema.Fields.HASH_ULTIMO, factura.getHashActual());

                    transaction.set(contadorRef, contadorData);
                    transaction.set(ventaRef, venta);
                    transaction.set(facturaRef, factura);

                    if (comandaId != null) {
                        transaction.update(
                                db.collection(FirestoreSchema.Collections.COMANDAS).document(comandaId),
                                FirestoreSchema.Fields.ESTADO, Comanda.PAGADA);
                    }
                    if (mesaId != null) {
                        transaction.update(
                                db.collection(FirestoreSchema.Collections.MESAS).document(mesaId),
                                FirestoreSchema.Fields.ESTADO, Mesa.LIBRE,
                                FirestoreSchema.Fields.COMANDA_ACTIVA_ID, null);
                    }

                    return facturaId;
                })
                .addOnSuccessListener(this::irAlTicket)
                .addOnFailureListener(e -> {
                    cobroEnCurso = false;
                    btnConfirmar.setEnabled(true);
                    Toast.makeText(this,
                            e.getLocalizedMessage() != null
                                    ? e.getLocalizedMessage()
                                    : getString(R.string.cobro_error_guardar),
                            Toast.LENGTH_LONG).show();
                });
    }

    private Factura construirFactura(Timestamp ts, Date ahora, int siguiente, String hashAnterior) {
        SimpleDateFormat anyoFmt = new SimpleDateFormat("yyyy", Locale.ROOT);
        SimpleDateFormat fechaIso = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        SimpleDateFormat fechaQr = new SimpleDateFormat("dd-MM-yyyy", Locale.ROOT);

        String numero = String.format(Locale.ROOT, "%04d/%s",
                siguiente, anyoFmt.format(ahora));
        double cuotaIva = total - (total / (1 + TIPO_IVA));

        String hashActual = HashVerifactu.calcular(
                numero, fechaIso.format(ahora), NIF_EMISOR,
                total, cuotaIva, hashAnterior);

        String urlValidacion = GeneradorQrVerifactu.construirUrl(
                NIF_EMISOR, numero, fechaQr.format(ahora), total);

        return new Factura(numero, ts, NIF_EMISOR,
                total, cuotaIva, hashAnterior, hashActual, urlValidacion);
    }

    private void irAlTicket(String facturaId) {
        Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra(TicketActivity.EXTRA_FACTURA_ID, facturaId);
        startActivity(intent);
        finish();
    }
}
