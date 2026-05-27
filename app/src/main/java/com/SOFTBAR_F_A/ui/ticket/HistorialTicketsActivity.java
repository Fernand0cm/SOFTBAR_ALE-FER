package com.SOFTBAR_F_A.ui.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Venta;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HistorialTicketsActivity extends AppCompatActivity {

    private LinearLayout listaHistorialTickets;
    private TextView txtHistorialVacio;
    private ListenerRegistration suscripcion;

    private final SimpleDateFormat fechaFmt =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_tickets);

        Header.aplica(this,
                getString(R.string.historial_tickets_title),
                getString(R.string.historial_tickets_subtitulo));

        listaHistorialTickets = findViewById(R.id.lista_historial_tickets);
        txtHistorialVacio = findViewById(R.id.txt_historial_vacio);

        suscribirseAVentas();
    }

    private void suscribirseAVentas() {
        suscripcion = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.VENTAS)
                .orderBy(FirestoreSchema.Fields.FECHA, Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;

                    listaHistorialTickets.removeAllViews();

                    if (snap.isEmpty()) {
                        txtHistorialVacio.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtHistorialVacio.setVisibility(View.GONE);

                    LayoutInflater inflater = LayoutInflater.from(this);

                    for (QueryDocumentSnapshot doc : snap) {
                        Venta venta = doc.toObject(Venta.class);
                        pintarVenta(inflater, doc.getId(), venta);
                    }
                });
    }

    private void pintarVenta(LayoutInflater inflater, String ventaId, Venta venta) {
        if (venta == null) return;

        View item = inflater.inflate(R.layout.item_ticket_historial,
                listaHistorialTickets, false);

        TextView txtNumero = item.findViewById(R.id.txt_numero_ticket);
        TextView txtFecha = item.findViewById(R.id.txt_fecha_ticket);
        TextView txtMesa = item.findViewById(R.id.txt_mesa_ticket);
        TextView txtTotal = item.findViewById(R.id.txt_total_ticket);
        TextView txtMetodo = item.findViewById(R.id.txt_metodo_ticket);

        String facturaId = venta.getFacturaId();

        txtNumero.setText(facturaId != null ? facturaId : ventaId);

        if (venta.getFecha() != null) {
            txtFecha.setText(fechaFmt.format(venta.getFecha().toDate()));
        } else {
            txtFecha.setText("");
        }

        if (venta.getMesaNumero() > 0) {
            txtMesa.setText(getString(R.string.historial_tickets_mesa,
                    venta.getMesaNumero()));
        } else {
            txtMesa.setText(R.string.historial_tickets_barra);
        }

        txtTotal.setText(String.format(Locale.getDefault(),
                "%.2f EUR", venta.getTotal()));

        txtMetodo.setText(venta.getMetodo() != null ? venta.getMetodo() : "");

        item.setOnClickListener(v -> abrirTicket(ventaId, facturaId));

        listaHistorialTickets.addView(item);
    }

    private void abrirTicket(String ventaId, String facturaId) {
        Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra(TicketActivity.EXTRA_VENTA_ID, ventaId);
        intent.putExtra(TicketActivity.EXTRA_FACTURA_ID, facturaId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) {
            suscripcion.remove();
        }
    }
}