package com.SOFTBAR_F_A.ui.cobro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.verifactu.Factura;
import com.SOFTBAR_F_A.data.verifactu.GeneradorQrVerifactu;
import com.SOFTBAR_F_A.data.verifactu.HashVerifactu;
import com.SOFTBAR_F_A.ui.common.Header;
import com.SOFTBAR_F_A.ui.ticket.TicketActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CobroActivity extends AppCompatActivity {

    // Mock: total y NIF se rellenarian desde la comanda real / configuracion del negocio
    private static final double TOTAL_MOCK = 14.50;
    private static final double TIPO_IVA = 0.10;
    private static final String NIF_EMISOR = "B12345678";

    private String metodoSeleccionado = "Efectivo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobro);

        Header.aplica(this, getString(R.string.cobro_title));

        findViewById(R.id.btn_efectivo).setOnClickListener(v ->
                metodoSeleccionado = getString(R.string.cobro_efectivo));
        findViewById(R.id.btn_tarjeta).setOnClickListener(v ->
                metodoSeleccionado = getString(R.string.cobro_tarjeta));
        findViewById(R.id.btn_mixto).setOnClickListener(v ->
                metodoSeleccionado = getString(R.string.cobro_mixto));

        Button btnConfirmar = findViewById(R.id.btn_confirmar);
        btnConfirmar.setOnClickListener(v -> confirmarVenta());
    }

    private void confirmarVenta() {
        Date ahora = new Date();
        Timestamp ts = new Timestamp(ahora);

        // 1. Guardar la venta basica
        Venta venta = new Venta(ts, TOTAL_MOCK, metodoSeleccionado);
        FirebaseFirestore.getInstance().collection("ventas").add(venta);

        // 2. Generar la factura Verifactu encadenada con la anterior
        FirebaseFirestore.getInstance()
                .collection("facturas")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    String hashAnterior = "";
                    int siguiente = 1;
                    if (task.isSuccessful() && task.getResult() != null
                            && !task.getResult().isEmpty()) {
                        Factura previa = task.getResult().getDocuments().get(0)
                                .toObject(Factura.class);
                        if (previa != null) {
                            hashAnterior = previa.getHashActual();
                            try {
                                String n = previa.getNumero().split("/")[0];
                                siguiente = Integer.parseInt(n) + 1;
                            } catch (Exception ignored) { }
                        }
                    }
                    guardarFactura(ts, ahora, hashAnterior, siguiente);
                    irAlTicket();
                });
    }

    private void guardarFactura(Timestamp ts, Date ahora, String hashAnterior, int siguiente) {
        SimpleDateFormat anyoFmt = new SimpleDateFormat("yyyy", Locale.ROOT);
        SimpleDateFormat fechaIso = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        SimpleDateFormat fechaQr = new SimpleDateFormat("dd-MM-yyyy", Locale.ROOT);

        String numero = String.format(Locale.ROOT, "%04d/%s",
                siguiente, anyoFmt.format(ahora));
        double cuotaIva = TOTAL_MOCK - (TOTAL_MOCK / (1 + TIPO_IVA));

        String hashActual = HashVerifactu.calcular(
                numero, fechaIso.format(ahora), NIF_EMISOR,
                TOTAL_MOCK, cuotaIva, hashAnterior);

        String urlValidacion = GeneradorQrVerifactu.construirUrl(
                NIF_EMISOR, numero, fechaQr.format(ahora), TOTAL_MOCK);

        Factura factura = new Factura(numero, ts, NIF_EMISOR,
                TOTAL_MOCK, cuotaIva, hashAnterior, hashActual, urlValidacion);

        FirebaseFirestore.getInstance().collection("facturas")
                .document(numero.replace("/", "-"))
                .set(factura);
    }

    private void irAlTicket() {
        startActivity(new Intent(this, TicketActivity.class));
        finish();
    }
}
