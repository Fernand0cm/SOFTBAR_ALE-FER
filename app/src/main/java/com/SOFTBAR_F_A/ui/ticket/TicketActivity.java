package com.SOFTBAR_F_A.ui.ticket;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.LineaComanda;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.data.verifactu.Factura;
import com.SOFTBAR_F_A.data.verifactu.GeneradorQrVerifactu;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Locale;

public class TicketActivity extends AppCompatActivity {

    public static final String EXTRA_VENTA_ID = "ventaId";
    public static final String EXTRA_FACTURA_ID = "facturaId";

    private TextView txtTicketNumero;
    private TextView txtTicketMesa;
    private LinearLayout listaTicketLineas;
    private TextView txtTicketTotal;
    private TextView txtTicketPago;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        Header.aplica(this, getString(R.string.ticket_title));

        Button btnImprimir = findViewById(R.id.btn_imprimir);
        btnImprimir.setOnClickListener(v ->
                Toast.makeText(this, R.string.ticket_pendiente, Toast.LENGTH_SHORT).show());

        Button btnEmail = findViewById(R.id.btn_email);
        btnEmail.setOnClickListener(v ->
                Toast.makeText(this, R.string.ticket_pendiente, Toast.LENGTH_SHORT).show());

        Button btnCerrar = findViewById(R.id.btn_cerrar);
        btnCerrar.setOnClickListener(v -> finish());

        txtTicketNumero = findViewById(R.id.txt_ticket_numero);
        txtTicketMesa = findViewById(R.id.txt_ticket_mesa);
        listaTicketLineas = findViewById(R.id.lista_ticket_lineas);
        txtTicketTotal = findViewById(R.id.txt_ticket_total);
        txtTicketPago = findViewById(R.id.txt_ticket_pago);

        cargarTicket();
    }

    private void cargarTicket() {
        String ventaId = getIntent().getStringExtra(EXTRA_VENTA_ID);
        String facturaId = getIntent().getStringExtra(EXTRA_FACTURA_ID);

        if (ventaId != null) {
            FirebaseFirestore.getInstance()
                    .collection(FirestoreSchema.Collections.VENTAS)
                    .document(ventaId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc == null || !doc.exists()) return;
                        Venta venta = doc.toObject(Venta.class);
                        if (venta == null) return;
                        pintarVenta(venta);
                        cargarFacturaVerifactu(
                                venta.getFacturaId() != null ? venta.getFacturaId() : facturaId);
                    });
            return;
        }

        cargarFacturaVerifactu(facturaId);
    }

    private void cargarFacturaVerifactu(String facturaId) {
        if (facturaId != null) {
            FirebaseFirestore.getInstance()
                    .collection(FirestoreSchema.Collections.FACTURAS)
                    .document(facturaId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc == null || !doc.exists()) return;
                        Factura f = doc.toObject(Factura.class);
                        if (f == null || f.getUrlValidacion() == null) return;
                        pintarFactura(f);
                        pintarVerifactu(f);
                    });
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.FACTURAS)
                .orderBy(FirestoreSchema.Fields.FECHA, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null || snap.isEmpty()) return;
                    Factura f = snap.getDocuments().get(0).toObject(Factura.class);
                    if (f == null || f.getUrlValidacion() == null) return;
                    pintarFactura(f);
                    pintarVerifactu(f);
                });
    }

    private void pintarVenta(Venta venta) {
        pintarDatosTicket(
                venta.getFacturaId(),
                venta.getMesaNumero(),
                venta.getLineas(),
                venta.getTotal(),
                venta.getMetodo());
    }

    private void pintarFactura(Factura factura) {
        pintarDatosTicket(
                factura.getNumero(),
                factura.getMesaNumero(),
                factura.getLineas(),
                factura.getTotal(),
                factura.getMetodo());
    }

    private void pintarDatosTicket(String numero, int mesaNumero,
                                   List<LineaComanda> lineas,
                                   double total, String metodo) {
        if (txtTicketNumero != null) {
            txtTicketNumero.setText(numero != null ? numero : "");
        }
        if (txtTicketMesa != null) {
            txtTicketMesa.setText(mesaNumero > 0
                    ? String.valueOf(mesaNumero)
                    : "-");
        }
        if (txtTicketTotal != null) {
            txtTicketTotal.setText(String.format(Locale.getDefault(),
                    "%.2f EUR", total));
        }
        if (txtTicketPago != null) {
            txtTicketPago.setText(metodo != null ? metodo : "");
        }
        pintarLineas(lineas);
    }

    private void pintarLineas(List<LineaComanda> lineas) {
        if (listaTicketLineas == null) return;
        listaTicketLineas.removeAllViews();
        if (lineas == null || lineas.isEmpty()) {
            TextView vacio = new TextView(this);
            vacio.setText(R.string.comanda_lineas_vacias);
            vacio.setTextColor(getColor(R.color.text_secondary));
            vacio.setTextSize(13);
            listaTicketLineas.addView(vacio);
            return;
        }

        for (LineaComanda linea : lineas) {
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setPadding(0, dp(3), 0, dp(3));

            TextView cantidad = new TextView(this);
            cantidad.setText(String.format(Locale.getDefault(),
                    "%dx", linea.getCantidad()));
            cantidad.setTextColor(getColor(R.color.brand_600));
            cantidad.setTextSize(13);
            cantidad.setTypeface(cantidad.getTypeface(), android.graphics.Typeface.BOLD);
            fila.addView(cantidad, new LinearLayout.LayoutParams(dp(32),
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView nombre = new TextView(this);
            nombre.setText(linea.getNombre());
            nombre.setTextColor(getColor(R.color.text_primary));
            nombre.setTextSize(13);
            fila.addView(nombre, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView subtotal = new TextView(this);
            subtotal.setText(String.format(Locale.getDefault(),
                    "%.2f", linea.subtotal()));
            subtotal.setTextColor(getColor(R.color.text_primary));
            subtotal.setTextSize(13);
            fila.addView(subtotal, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            listaTicketLineas.addView(fila);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void pintarVerifactu(Factura factura) {
        ImageView qr = findViewById(R.id.img_qr_verifactu);
        TextView numero = findViewById(R.id.txt_numero_factura);
        TextView hash = findViewById(R.id.txt_hash_factura);

        qr.setImageBitmap(GeneradorQrVerifactu.generarBitmap(
                factura.getUrlValidacion(), 480));

        numero.setText(getString(R.string.verifactu_factura, factura.getNumero()));

        String h = factura.getHashActual();
        if (h != null && h.length() > 16) h = h.substring(0, 16) + "...";
        hash.setText(getString(R.string.verifactu_hash, h));
    }
}
