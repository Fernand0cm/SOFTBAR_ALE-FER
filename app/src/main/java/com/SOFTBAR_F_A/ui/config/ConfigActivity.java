package com.SOFTBAR_F_A.ui.config;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Producto;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class ConfigActivity extends AppCompatActivity {

    private static final String COLECCION = "productos";

    private LinearLayout listaProductos;
    private TextView txtListaVacia;
    private ListenerRegistration suscripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        listaProductos = findViewById(R.id.lista_productos);
        txtListaVacia = findViewById(R.id.txt_lista_vacia);

        Button btnEscanear = findViewById(R.id.btn_escanear);
        btnEscanear.setOnClickListener(v -> escanear());

        Button btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());

        suscribirseAProductos();
    }

    private void escanear() {
        GmsBarcodeScannerOptions opciones = new GmsBarcodeScannerOptions.Builder()
                .build();
        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, opciones);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String codigo = barcode.getRawValue();
                    if (codigo != null) {
                        mostrarDialogProducto(codigo);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show())
                .addOnCanceledListener(() -> { /* el usuario cancelo, sin accion */ });
    }

    private void mostrarDialogProducto(String codigo) {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_producto, null);
        TextView txtCodigo = vista.findViewById(R.id.txt_codigo_dialog);
        EditText inputNombre = vista.findViewById(R.id.input_nombre);
        EditText inputPrecio = vista.findViewById(R.id.input_precio);
        txtCodigo.setText(getString(R.string.dialog_codigo_prefijo, codigo));

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_titulo)
                .setView(vista)
                .setPositiveButton(R.string.dialog_guardar, (d, w) -> {
                    String nombre = inputNombre.getText().toString().trim();
                    String precioTxt = inputPrecio.getText().toString().trim();

                    if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(precioTxt)) {
                        Toast.makeText(this, R.string.dialog_error_vacio,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double precio;
                    try {
                        precio = Double.parseDouble(precioTxt.replace(',', '.'));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.dialog_error_precio,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    guardarProducto(new Producto(codigo, nombre, precio));
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }

    private void guardarProducto(Producto producto) {
        FirebaseFirestore.getInstance()
                .collection(COLECCION)
                .document(producto.getCodigoBarras())
                .set(producto)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, R.string.config_guardado,
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void suscribirseAProductos() {
        suscripcion = FirebaseFirestore.getInstance()
                .collection(COLECCION)
                .orderBy("nombre", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;

                    listaProductos.removeAllViews();
                    if (snap.isEmpty()) {
                        txtListaVacia.setVisibility(View.VISIBLE);
                        return;
                    }
                    txtListaVacia.setVisibility(View.GONE);

                    LayoutInflater inflater = LayoutInflater.from(this);
                    for (Producto p : snap.toObjects(Producto.class)) {
                        View item = inflater.inflate(R.layout.item_producto,
                                listaProductos, false);
                        ((TextView) item.findViewById(R.id.txt_nombre))
                                .setText(p.getNombre());
                        ((TextView) item.findViewById(R.id.txt_codigo))
                                .setText(p.getCodigoBarras());
                        ((TextView) item.findViewById(R.id.txt_precio))
                                .setText(String.format(Locale.getDefault(),
                                        "%.2f EUR", p.getPrecio()));
                        listaProductos.addView(item);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) {
            suscripcion.remove();
        }
    }
}
