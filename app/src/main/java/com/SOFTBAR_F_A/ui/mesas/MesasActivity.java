package com.SOFTBAR_F_A.ui.mesas;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.EstadoMesaColor;
import com.SOFTBAR_F_A.data.Mesa;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.comanda.ComandaActivity;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MesasActivity extends AppCompatActivity {

    public static final String EXTRA_MESA_ID = "mesaId";
    public static final String EXTRA_MESA_NUMERO = "mesaNumero";

    private static final int NUM_MESAS_INICIAL = 8;

    private GridLayout grid;
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);

        Header.aplica(this, getString(R.string.mesas_title), getString(R.string.mesas_subtitulo));

        grid = findViewById(R.id.grid_mesas);

        sembrarSiVacio();
        suscribirseAMesas();
    }

    private void sembrarSiVacio() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FirestoreSchema.Collections.MESAS).limit(1).get().addOnSuccessListener(snap -> {
            if (snap == null || !snap.isEmpty()) return;
            for (int i = 1; i <= NUM_MESAS_INICIAL; i++) {
                Mesa m = new Mesa(i, Mesa.LIBRE);
                db.collection(FirestoreSchema.Collections.MESAS).document(String.valueOf(i)).set(m);
            }
        });
    }

    private void suscribirseAMesas() {
        suscripcion = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.MESAS)
                .orderBy(FirestoreSchema.Fields.NUMERO, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    List<DocMesa> mesas = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Mesa m = doc.toObject(Mesa.class);
                        mesas.add(new DocMesa(doc.getId(), m));
                    }
                    pintarGrid(mesas);
                });
    }

    private void pintarGrid(List<DocMesa> mesas) {
        grid.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int margen = (int) (8 * density);
        int alto = (int) (110 * density);
        float radio = 16 * density;

        for (DocMesa dm : mesas) {
            Mesa m = dm.mesa;
            int color = ContextCompat.getColor(this, EstadoMesaColor.colorPara(m.getEstado()));

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setColor(color);
            bg.setCornerRadius(radio);

            TextView vista = new TextView(this);
            vista.setText(String.valueOf(m.getNumero()));
            vista.setTextSize(28);
            vista.setTextColor(ContextCompat.getColor(this, R.color.white));
            vista.setTypeface(vista.getTypeface(), android.graphics.Typeface.BOLD);
            vista.setGravity(Gravity.CENTER);
            vista.setBackground(bg);
            vista.setElevation(4 * density);
            vista.setClickable(true);
            vista.setFocusable(true);
            vista.setOnClickListener(v -> abrirMesa(dm));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = alto;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(margen, margen, margen, margen);
            vista.setLayoutParams(params);

            grid.addView(vista);
        }
    }

    private void abrirMesa(DocMesa dm) {
        if (Mesa.LIBRE.equals(dm.mesa.getEstado())) {
            FirebaseFirestore.getInstance()
                    .collection(FirestoreSchema.Collections.MESAS)
                    .document(dm.id)
                    .update(FirestoreSchema.Fields.ESTADO, Mesa.OCUPADA);
        }

        Intent intent = new Intent(this, ComandaActivity.class);
        intent.putExtra(EXTRA_MESA_ID, dm.id);
        intent.putExtra(EXTRA_MESA_NUMERO, dm.mesa.getNumero());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) suscripcion.remove();
    }

    private static class DocMesa {
        final String id;
        final Mesa mesa;

        DocMesa(String id, Mesa mesa) {
            this.id = id;
            this.mesa = mesa;
        }
    }
}