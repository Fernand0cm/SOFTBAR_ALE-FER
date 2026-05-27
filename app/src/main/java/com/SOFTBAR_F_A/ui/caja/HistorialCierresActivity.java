package com.SOFTBAR_F_A.ui.caja;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Turno;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HistorialCierresActivity extends AppCompatActivity {

    private LinearLayout listaHistorialCierres;
    private TextView txtCierresVacio;
    private ListenerRegistration suscripcion;

    private final SimpleDateFormat fechaFmt =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_cierres);

        Header.aplica(this,
                getString(R.string.historial_cierres_title),
                getString(R.string.historial_cierres_subtitulo));

        listaHistorialCierres = findViewById(R.id.lista_historial_cierres);
        txtCierresVacio = findViewById(R.id.txt_cierres_vacio);

        suscribirseACierres();
    }

    private void suscribirseACierres() {
        suscripcion = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.TURNOS)
                .whereEqualTo(FirestoreSchema.Fields.ESTADO, Turno.CERRADO)
                .orderBy(FirestoreSchema.Fields.FECHA_CIERRE, Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;

                    listaHistorialCierres.removeAllViews();

                    if (snap.isEmpty()) {
                        txtCierresVacio.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtCierresVacio.setVisibility(View.GONE);

                    LayoutInflater inflater = LayoutInflater.from(this);

                    for (QueryDocumentSnapshot doc : snap) {
                        Turno turno = doc.toObject(Turno.class);
                        pintarCierre(inflater, turno);
                    }
                });
    }

    private void pintarCierre(LayoutInflater inflater, Turno turno) {
        if (turno == null) return;

        View item = inflater.inflate(R.layout.item_cierre_historial,
                listaHistorialCierres, false);

        TextView txtFecha = item.findViewById(R.id.txt_fecha_cierre);
        TextView txtUsuario = item.findViewById(R.id.txt_usuario_cierre);
        TextView txtEsperado = item.findViewById(R.id.txt_esperado_cierre);
        TextView txtContado = item.findViewById(R.id.txt_contado_cierre);
        TextView txtDiferencia = item.findViewById(R.id.txt_diferencia_cierre);

        if (turno.getFechaCierre() != null) {
            txtFecha.setText(fechaFmt.format(turno.getFechaCierre().toDate()));
        } else {
            txtFecha.setText("");
        }

        txtUsuario.setText(getString(R.string.historial_cierre_usuario,
                turno.getUsuarioEmail() != null ? turno.getUsuarioEmail() : ""));

        txtEsperado.setText(getString(R.string.historial_cierre_esperado,
                turno.getEfectivoEsperado()));

        txtContado.setText(getString(R.string.historial_cierre_contado,
                turno.getEfectivoContado()));

        double diferencia = turno.getDiferenciaCaja();

        txtDiferencia.setText(getString(R.string.historial_cierre_diferencia,
                diferencia));

        txtDiferencia.setTextColor(ContextCompat.getColor(this,
                diferencia < 0 ? R.color.warning : R.color.brand_600));

        listaHistorialCierres.addView(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) {
            suscripcion.remove();
        }
    }
}