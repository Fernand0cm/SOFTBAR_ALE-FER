package com.SOFTBAR_F_A.ui.caja;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
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

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HistorialCierresActivity extends AppCompatActivity {

    private LinearLayout listaHistorialCierres;
    private TextView txtCierresVacio;
    private ListenerRegistration suscripcion;

    private final SimpleDateFormat formatoFecha =
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

        cargarCierres();
    }

    private void cargarCierres() {
        suscripcion = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.TURNOS)
                .whereEqualTo(FirestoreSchema.Fields.ESTADO, Turno.CERRADO)
                .orderBy(FirestoreSchema.Fields.FECHA_CIERRE, Query.Direction.DESCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;

                    listaHistorialCierres.removeAllViews();

                    if (snap.isEmpty()) {
                        txtCierresVacio.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtCierresVacio.setVisibility(View.GONE);

                    for (Turno turno : snap.toObjects(Turno.class)) {
                        pintarCierre(turno);
                    }
                });
    }
    private void pintarCierre(Turno turno) {
        View item = LayoutInflater.from(this)
                .inflate(R.layout.item_cierre_caja, listaHistorialCierres, false);

        String fechaApertura = turno.getFechaApertura() != null
                ? formatoFecha.format(turno.getFechaApertura().toDate())
                : "-";

        String fechaCierre = turno.getFechaCierre() != null
                ? formatoFecha.format(turno.getFechaCierre().toDate())
                : "-";

        ((TextView) item.findViewById(R.id.txt_fecha_cierre))
                .setText("Cierre: " + fechaCierre);

        ((TextView) item.findViewById(R.id.txt_fecha_apertura))
                .setText("Apertura: " + fechaApertura);

        ((TextView) item.findViewById(R.id.txt_usuario))
                .setText("Usuario: " +
                        (turno.getUsuarioEmail() != null ? turno.getUsuarioEmail() : "-"));

        ((TextView) item.findViewById(R.id.txt_esperado))
                .setText(String.format(Locale.getDefault(),
                        "Esperado\n%.2f EUR", turno.getEfectivoEsperado()));

        ((TextView) item.findViewById(R.id.txt_contado))
                .setText(String.format(Locale.getDefault(),
                        "Contado\n%.2f EUR", turno.getEfectivoContado()));

        ((TextView) item.findViewById(R.id.txt_diferencia))
                .setText(String.format(Locale.getDefault(),
                        "Dif.\n%.2f EUR", turno.getDiferenciaCaja()));

        listaHistorialCierres.addView(item);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) {
            suscripcion.remove();
        }
    }
}