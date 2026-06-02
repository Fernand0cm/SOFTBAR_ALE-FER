package com.SOFTBAR_F_A.ui.stock;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Producto;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class StockActivity extends AppCompatActivity {

    private LinearLayout listaStock;
    private TextView txtStockVacio;
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        Header.aplica(this, getString(R.string.stock_title), getString(R.string.stock_catalogo));

        listaStock = findViewById(R.id.lista_stock);
        txtStockVacio = findViewById(R.id.txt_stock_vacio);

        suscribirseAProductos();
    }

    private void suscribirseAProductos() {
        suscripcion = FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.PRODUCTOS)
                .orderBy(FirestoreSchema.Fields.NOMBRE, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;

                    listaStock.removeAllViews();

                    if (snap.isEmpty()) {
                        txtStockVacio.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtStockVacio.setVisibility(View.GONE);

                    LayoutInflater inflater = LayoutInflater.from(this);

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                        Producto producto = doc.toObject(Producto.class);
                        pintarProducto(inflater, producto);
                    }
                });
    }

    private void pintarProducto(LayoutInflater inflater, Producto p) {
        View item = inflater.inflate(R.layout.item_stock_producto, listaStock, false);
        MaterialCardView card = item.findViewById(R.id.card_stock);

        ((TextView) item.findViewById(R.id.txt_nombre_stock))
                .setText(p.getNombre());

        ((TextView) item.findViewById(R.id.txt_codigo_stock))
                .setText(p.getCodigoBarras());

        ((TextView) item.findViewById(R.id.txt_categoria_stock))
                .setText(p.getCategoria() != null ? p.getCategoria() : "");

        ((TextView) item.findViewById(R.id.txt_stock_actual))
                .setText(getString(R.string.producto_stock, p.getStock()));

        ((TextView) item.findViewById(R.id.txt_stock_minimo))
                .setText(getString(R.string.producto_stock_minimo, p.getStockMinimo()));

        TextView txtStockBajo = item.findViewById(R.id.txt_stock_bajo);

        boolean stockBajo =
                p.getStockMinimo() > 0 &&
                        p.getStock() <= p.getStockMinimo();

        txtStockBajo.setVisibility(
                stockBajo ? View.VISIBLE : View.GONE
        );

        if (stockBajo) {
            card.setCardBackgroundColor(
                    Color.parseColor("#FFF0F0")
            );
        } else {
            card.setCardBackgroundColor(
                    Color.parseColor("#FFFFFF")
            );
        }

        Button btnAnadir = item.findViewById(R.id.btn_anadir_stock);
        btnAnadir.setOnClickListener(v -> mostrarDialogEntradaStock(p));
        Button btnCambiarMinimo = item.findViewById(R.id.btn_cambiar_minimo);
        btnCambiarMinimo.setOnClickListener(v -> mostrarDialogStockMinimo(p));

        listaStock.addView(item);

    }

    private void mostrarDialogEntradaStock(Producto producto) {
        EditText input = new EditText(this);
        input.setHint(R.string.stock_unidades_hint);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle(R.string.stock_dialog_titulo)
                .setView(input)
                .setPositiveButton(R.string.dialog_guardar, (dialog, which) -> {
                    String texto = input.getText().toString().trim();

                    if (TextUtils.isEmpty(texto)) {
                        Toast.makeText(this, R.string.stock_error_unidades,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int unidades;
                    try {
                        unidades = Integer.parseInt(texto);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.stock_error_unidades,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (unidades <= 0) {
                        Toast.makeText(this, R.string.stock_error_unidades,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    actualizarStock(producto, unidades);
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }

    private void actualizarStock(Producto producto, int unidades) {
        int nuevoStock = producto.getStock() + unidades;

        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.PRODUCTOS)
                .document(producto.getCodigoBarras())
                .update("stock", nuevoStock)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, R.string.stock_actualizado,
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show());
    }
    private void mostrarDialogStockMinimo(Producto producto) {
        EditText input = new EditText(this);
        input.setHint(R.string.stock_minimo_hint);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(producto.getStockMinimo()));

        new AlertDialog.Builder(this)
                .setTitle(R.string.stock_minimo_dialog_titulo)
                .setView(input)
                .setPositiveButton(R.string.dialog_guardar, (dialog, which) -> {
                    String texto = input.getText().toString().trim();

                    if (TextUtils.isEmpty(texto)) {
                        Toast.makeText(this, R.string.stock_error_unidades,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int stockMinimo;
                    try {
                        stockMinimo = Integer.parseInt(texto);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.stock_error_unidades,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (stockMinimo < 0) {
                        Toast.makeText(this, R.string.stock_error_unidades,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    actualizarStockMinimo(producto, stockMinimo);
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }
    private void actualizarStockMinimo(Producto producto, int stockMinimo) {
        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.PRODUCTOS)
                .document(producto.getCodigoBarras())
                .update("stockMinimo", stockMinimo)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, R.string.stock_minimo_actualizado,
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) {
            suscripcion.remove();
        }
    }
}