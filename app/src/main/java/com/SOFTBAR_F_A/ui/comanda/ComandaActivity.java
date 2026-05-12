package com.SOFTBAR_F_A.ui.comanda;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.CalculoTotalComanda;
import com.SOFTBAR_F_A.data.Comanda;
import com.SOFTBAR_F_A.data.LineaComanda;
import com.SOFTBAR_F_A.data.Mesa;
import com.SOFTBAR_F_A.data.Producto;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.cobro.CobroActivity;
import com.SOFTBAR_F_A.ui.common.Header;
import com.SOFTBAR_F_A.ui.mesas.MesasActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComandaActivity extends AppCompatActivity {

    public static final String EXTRA_COMANDA_ID = "comandaId";
    public static final String EXTRA_TOTAL = "total";

    private String mesaId;
    private int mesaNumero;
    private String comandaId;
    private List<LineaComanda> lineas = new ArrayList<>();

    private GridLayout gridCatalogo;
    private LinearLayout listaLineas;
    private TextView txtTotal;
    private TextView txtCatalogoVacio;
    private TextView txtLineasVacias;

    private ListenerRegistration suscripcionProductos;
    private ListenerRegistration suscripcionComanda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comanda);

        mesaId = getIntent().getStringExtra(MesasActivity.EXTRA_MESA_ID);
        mesaNumero = getIntent().getIntExtra(MesasActivity.EXTRA_MESA_NUMERO, 0);

        String subtitulo = mesaNumero > 0
                ? getString(R.string.comanda_mesa_numero, mesaNumero)
                : null;
        Header.aplica(this, getString(R.string.comanda_title), subtitulo);

        gridCatalogo = findViewById(R.id.grid_catalogo);
        listaLineas = findViewById(R.id.lista_lineas);
        txtTotal = findViewById(R.id.txt_total);
        txtCatalogoVacio = findViewById(R.id.txt_catalogo_vacio);
        txtLineasVacias = findViewById(R.id.txt_lineas_vacias);

        Button btnCobrar = findViewById(R.id.btn_cobrar);
        btnCobrar.setOnClickListener(v -> irACobro());

        cargarOAbrirComanda();
        suscribirseAProductos();
    }

    private void cargarOAbrirComanda() {
        if (mesaId == null) return;

        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.COMANDAS)
                .whereEqualTo(FirestoreSchema.Fields.MESA_ID, mesaId)
                .whereEqualTo(FirestoreSchema.Fields.ESTADO, Comanda.ABIERTA)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap != null && !snap.isEmpty()) {
                        comandaId = snap.getDocuments().get(0).getId();
                        suscribirseAComanda();
                    } else {
                        crearComandaNueva();
                    }
                });
    }

    private void crearComandaNueva() {
        Comanda nueva = new Comanda(mesaId, mesaNumero);
        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.COMANDAS)
                .add(nueva)
                .addOnSuccessListener(ref -> {
                    comandaId = ref.getId();
                    enlazarMesaConComanda();
                    suscribirseAComanda();
                });
    }

    private void enlazarMesaConComanda() {
        if (mesaId == null || comandaId == null) return;
        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.MESAS)
                .document(mesaId)
                .update(
                        FirestoreSchema.Fields.ESTADO, Mesa.OCUPADA,
                        FirestoreSchema.Fields.COMANDA_ACTIVA_ID, comandaId);
    }

    private void suscribirseAComanda() {
        if (comandaId == null) return;
        suscripcionComanda = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.COMANDAS)
                .document(comandaId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) return;
                    Comanda c = doc.toObject(Comanda.class);
                    if (c == null) return;
                    lineas = c.getLineas();
                    pintarLineas();
                });
    }

    private void suscribirseAProductos() {
        suscripcionProductos = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.PRODUCTOS)
                .orderBy(FirestoreSchema.Fields.NOMBRE, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    List<Producto> productos = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        productos.add(doc.toObject(Producto.class));
                    }
                    pintarCatalogo(productos);
                });
    }

    private void pintarCatalogo(List<Producto> productos) {
        gridCatalogo.removeAllViews();
        if (productos.isEmpty()) {
            txtCatalogoVacio.setVisibility(View.VISIBLE);
            return;
        }
        txtCatalogoVacio.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        float density = getResources().getDisplayMetrics().density;
        int margen = (int) (6 * density);

        for (Producto p : productos) {
            View item = inflater.inflate(R.layout.item_catalogo, gridCatalogo, false);
            ((TextView) item.findViewById(R.id.txt_nombre_producto))
                    .setText(p.getNombre());
            ((TextView) item.findViewById(R.id.txt_precio_producto))
                    .setText(String.format(Locale.getDefault(), "%.2f EUR", p.getPrecio()));
            item.setOnClickListener(v -> anadirProducto(p));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(margen, margen, margen, margen);
            item.setLayoutParams(params);

            gridCatalogo.addView(item);
        }
    }

    private void anadirProducto(Producto p) {
        if (comandaId == null) {
            Toast.makeText(this, R.string.comanda_no_lista, Toast.LENGTH_SHORT).show();
            return;
        }

        // Si ya hay una linea con ese producto, solo aumentamos cantidad
        boolean encontrado = false;
        for (LineaComanda l : lineas) {
            if (l.getCodigoBarras() != null
                    && l.getCodigoBarras().equals(p.getCodigoBarras())) {
                l.setCantidad(l.getCantidad() + 1);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            lineas.add(new LineaComanda(
                    p.getCodigoBarras(), p.getNombre(), p.getPrecio(), 1));
        }
        guardarLineas();
    }

    private void quitarLinea(LineaComanda linea) {
        if (linea.getCantidad() > 1) {
            linea.setCantidad(linea.getCantidad() - 1);
        } else {
            lineas.remove(linea);
        }
        guardarLineas();
    }

    private void guardarLineas() {
        if (comandaId == null) return;
        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.COMANDAS)
                .document(comandaId)
                .update(FirestoreSchema.Fields.LINEAS, lineas);
    }

    private void pintarLineas() {
        listaLineas.removeAllViews();

        if (lineas.isEmpty()) {
            txtLineasVacias.setVisibility(View.VISIBLE);
        } else {
            txtLineasVacias.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(this);
            for (LineaComanda l : lineas) {
                View item = inflater.inflate(R.layout.item_linea_comanda, listaLineas, false);
                ((TextView) item.findViewById(R.id.txt_cantidad))
                        .setText(String.format(Locale.getDefault(), "%dx", l.getCantidad()));
                ((TextView) item.findViewById(R.id.txt_nombre)).setText(l.getNombre());
                ((TextView) item.findViewById(R.id.txt_subtotal))
                        .setText(String.format(Locale.getDefault(), "%.2f EUR", l.subtotal()));
                item.findViewById(R.id.btn_quitar).setOnClickListener(v -> quitarLinea(l));
                listaLineas.addView(item);
            }
        }

        double total = CalculoTotalComanda.total(lineas);
        txtTotal.setText(String.format(Locale.getDefault(), "%.2f EUR", total));
    }

    private void irACobro() {
        if (lineas.isEmpty()) {
            Toast.makeText(this, R.string.comanda_sin_lineas, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CobroActivity.class);
        intent.putExtra(EXTRA_COMANDA_ID, comandaId);
        intent.putExtra(EXTRA_TOTAL, CalculoTotalComanda.total(lineas));
        intent.putExtra(MesasActivity.EXTRA_MESA_ID, mesaId);
        intent.putExtra(MesasActivity.EXTRA_MESA_NUMERO, mesaNumero);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcionProductos != null) suscripcionProductos.remove();
        if (suscripcionComanda != null) suscripcionComanda.remove();
    }
}
