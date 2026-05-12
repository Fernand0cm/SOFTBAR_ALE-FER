package com.SOFTBAR_F_A.ui.ticket;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.data.verifactu.Factura;
import com.SOFTBAR_F_A.data.verifactu.GeneradorQrVerifactu;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class TicketActivity extends AppCompatActivity {

    public static final String EXTRA_FACTURA_ID = "facturaId";

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

        cargarFacturaVerifactu();
    }

    private void cargarFacturaVerifactu() {
        String facturaId = getIntent().getStringExtra(EXTRA_FACTURA_ID);
        if (facturaId != null) {
            FirebaseFirestore.getInstance()
                    .collection(FirestoreSchema.Collections.FACTURAS)
                    .document(facturaId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc == null || !doc.exists()) return;
                        Factura f = doc.toObject(Factura.class);
                        if (f == null || f.getUrlValidacion() == null) return;
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
                    pintarVerifactu(f);
                });
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
