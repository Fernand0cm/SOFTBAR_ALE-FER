package com.SOFTBAR_F_A.ui.config;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.SOFTBAR_F_A.R;
import com.SOFTBAR_F_A.data.Producto;
import com.SOFTBAR_F_A.data.firebase.FirestoreSchema;
import com.SOFTBAR_F_A.ui.common.Header;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class ConfigActivity extends AppCompatActivity {

    private LinearLayout listaProductos;
    private TextView txtListaVacia;
    private ListenerRegistration suscripcion;
    private Spinner spinnerFiltroCategoria;
    private String filtroCategoria = "Todas";
    private List<Producto> productosActuales = new ArrayList<>();
    private EditText inputBuscarProducto;
    private String textoBusqueda = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Header.aplica(this, getString(R.string.config_title), getString(R.string.config_productos_subtitulo));

        listaProductos = findViewById(R.id.lista_productos);
        txtListaVacia = findViewById(R.id.txt_lista_vacia);

        spinnerFiltroCategoria = findViewById(R.id.spinner_filtro_categoria);
        configurarFiltroCategoria();

        inputBuscarProducto = findViewById(R.id.input_buscar_producto);
        configurarBuscadorProducto();

        Button btnEscanear = findViewById(R.id.btn_escanear);
        btnEscanear.setOnClickListener(v -> escanear());

        Button btnAltaManual = findViewById(R.id.btn_alta_manual);
        btnAltaManual.setOnClickListener(v -> mostrarDialogProducto(null));

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
                .addOnCanceledListener(() -> { });
    }

    private void mostrarDialogProducto(String codigoEscaneado) {
        mostrarDialogProducto(codigoEscaneado, null);
    }

    private void mostrarDialogProducto(String codigoEscaneado, Producto productoEditar) {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_producto, null);

        TextView txtCodigo = vista.findViewById(R.id.txt_codigo_dialog);
        EditText inputCodigo = vista.findViewById(R.id.input_codigo);
        EditText inputNombre = vista.findViewById(R.id.input_nombre);
        EditText inputPrecio = vista.findViewById(R.id.input_precio);
        EditText inputStock = vista.findViewById(R.id.input_stock);
        EditText inputStockMinimo = vista.findViewById(R.id.input_stock_minimo);

        Spinner spinnerCategoria = vista.findViewById(R.id.spinner_categoria);

        ArrayAdapter<CharSequence> adapterCategorias =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.categorias_productos,
                        android.R.layout.simple_spinner_item
                );

        adapterCategorias.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCategoria.setAdapter(adapterCategorias);
        if (adapterCategorias.getCount() > 1) {
            spinnerCategoria.setSelection(1);
        }
        boolean editando = productoEditar != null;

        if (editando) {
            inputCodigo.setText(productoEditar.getCodigoBarras());
            inputCodigo.setEnabled(false);
            inputNombre.setText(productoEditar.getNombre());
            inputPrecio.setText(String.format(Locale.ROOT, "%.2f", productoEditar.getPrecio()));
            inputStock.setText(String.valueOf(productoEditar.getStock()));
            inputStockMinimo.setText(String.valueOf(productoEditar.getStockMinimo()));
            txtCodigo.setText(getString(R.string.dialog_codigo_prefijo, productoEditar.getCodigoBarras()));
            String categoriaEditar = productoEditar.getCategoria();

            if (categoriaEditar != null) {
                int posicion = adapterCategorias.getPosition(categoriaEditar);
                if (posicion >= 0) {
                    spinnerCategoria.setSelection(posicion);
                }
            }
        } else if (codigoEscaneado != null && !codigoEscaneado.trim().isEmpty()) {
            inputCodigo.setText(codigoEscaneado);
            inputCodigo.setEnabled(false);
            txtCodigo.setText(getString(R.string.dialog_codigo_prefijo, codigoEscaneado));
        } else {
            txtCodigo.setText(R.string.dialog_codigo_manual);
            inputCodigo.setEnabled(true);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_titulo)
                .setView(vista)
                .setPositiveButton(R.string.dialog_guardar, (d, w) -> {
                    String codigo = inputCodigo.getText().toString().trim();
                    String nombre = inputNombre.getText().toString().trim();
                    String precioTxt = inputPrecio.getText().toString().trim();
                    String stockTxt = inputStock.getText().toString().trim();
                    String stockMinimoTxt = inputStockMinimo.getText().toString().trim();
                    String categoria = spinnerCategoria.getSelectedItem().toString();

                    if (TextUtils.isEmpty(codigo)) {
                        Toast.makeText(this, R.string.dialog_error_codigo,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

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
                    int stock = 0;
                    int stockMinimo = 0;

                    try {
                        if (!TextUtils.isEmpty(stockTxt)) {
                            stock = Integer.parseInt(stockTxt);
                        }
                        if (!TextUtils.isEmpty(stockMinimoTxt)) {
                            stockMinimo = Integer.parseInt(stockMinimoTxt);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.dialog_error_stock,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Producto producto = new Producto(codigo, nombre, precio);
                    producto.setCategoria(categoria);
                    producto.setStock(stock);
                    producto.setStockMinimo(stockMinimo);
                    if (editando) {
                        producto.setActivo(productoEditar.isActivo());
                    }

                    guardarProducto(producto);
                })
                .setNegativeButton(R.string.dialog_cancelar, null)
                .show();
    }

    private void guardarProducto(Producto producto) {
        FirebaseFirestore.getInstance()
                .collection(FirestoreSchema.Collections.PRODUCTOS)
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
                .collection(FirestoreSchema.Collections.PRODUCTOS)
                .orderBy(FirestoreSchema.Fields.NOMBRE, Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;

                    listaProductos.removeAllViews();

                    if (snap.isEmpty()) {
                        txtListaVacia.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtListaVacia.setVisibility(View.GONE);

                    productosActuales = snap.toObjects(Producto.class);
                    pintarProductos();
                });
    }
    private void configurarFiltroCategoria() {
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.categorias_productos,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroCategoria.setAdapter(adapter);

        spinnerFiltroCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtroCategoria = parent.getItemAtPosition(position).toString();
                pintarProductos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    private void configurarBuscadorProducto() {
        inputBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusqueda = s.toString().trim().toLowerCase();
                pintarProductos();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
    private void pintarProductos() {
        listaProductos.removeAllViews();

        List<Producto> filtrados = new ArrayList<>();

        for (Producto p : productosActuales) {
            if (!"Todas".equals(filtroCategoria)) {
                if (p.getCategoria() == null ||
                        !p.getCategoria().equalsIgnoreCase(filtroCategoria)) {
                    continue;
                }
            }

            if (!textoBusqueda.isEmpty()) {
                String nombre = p.getNombre() != null ? p.getNombre().toLowerCase() : "";
                String codigo = p.getCodigoBarras() != null ? p.getCodigoBarras().toLowerCase() : "";

                if (!nombre.contains(textoBusqueda) && !codigo.contains(textoBusqueda)) {
                    continue;
                }
            }

            filtrados.add(p);
        }

        if (filtrados.isEmpty()) {

            if (!textoBusqueda.isEmpty()
                    || !"Todas".equals(filtroCategoria)) {

                txtListaVacia.setText(R.string.config_lista_sin_resultados);

            } else {

                txtListaVacia.setText(R.string.config_lista_vacia);
            }

            txtListaVacia.setVisibility(View.VISIBLE);
            return;
        }

        txtListaVacia.setVisibility(View.GONE);
        txtListaVacia.setText(R.string.config_lista_vacia);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Producto p : filtrados) {
            View item = inflater.inflate(R.layout.item_producto, listaProductos, false);

            ((TextView) item.findViewById(R.id.txt_nombre))
                    .setText(p.getNombre());

            ((TextView) item.findViewById(R.id.txt_codigo))
                    .setText(p.getCodigoBarras());

            ((TextView) item.findViewById(R.id.txt_categoria))
                    .setText(p.getCategoria() != null ? p.getCategoria() : "");
            ((TextView) item.findViewById(R.id.txt_stock))
                    .setText(getString(R.string.producto_stock, p.getStock()));

            ((TextView) item.findViewById(R.id.txt_stock_minimo))
                    .setText(getString(R.string.producto_stock_minimo, p.getStockMinimo()));

            TextView txtStockBajo = item.findViewById(R.id.txt_stock_bajo);
            txtStockBajo.setVisibility(
                    p.getStockMinimo() > 0 && p.getStock() <= p.getStockMinimo()
                            ? View.VISIBLE
                            : View.GONE
            );
            ((TextView) item.findViewById(R.id.txt_precio))
                    .setText(String.format(Locale.getDefault(),
                            "%.2f EUR", p.getPrecio()));

            TextView txtEstado = item.findViewById(R.id.txt_estado);
            Button btnEditar = item.findViewById(R.id.btn_editar);
            Button btnToggleActivo = item.findViewById(R.id.btn_toggle_activo);

            boolean activo = p.isActivo();

            txtEstado.setVisibility(activo ? View.GONE : View.VISIBLE);

            btnToggleActivo.setText(activo
                    ? R.string.producto_desactivar
                    : R.string.producto_activar);

            btnEditar.setOnClickListener(v -> mostrarDialogProducto(null, p));

            btnToggleActivo.setOnClickListener(v -> {
                p.setActivo(!p.isActivo());
                guardarProducto(p);
            });

            listaProductos.addView(item);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suscripcion != null) {
            suscripcion.remove();
        }
    }
}